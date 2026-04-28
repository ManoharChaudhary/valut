package com.vault.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vault.features.FeatureDefinition;
import com.vault.rules.HierarchyLevel;
import com.vault.rules.Rule;
import com.vault.rules.RuleRepository;
import com.vault.rules.RuleVersion;
import com.vault.rules.RuleVersionRepository;
import com.vault.validation.ContextSchemaValidator;
import com.vault.validation.ContextValidationResult;

@Service
public class DecisionEngineService {
	private final RuleRepository ruleRepository;
	private final RuleVersionRepository ruleVersionRepository;
	private final ContextSchemaValidator contextSchemaValidator;
	private final List<RuleEvaluatorStrategy> evaluators;
	private final RuleScopeMatcher ruleScopeMatcher;

	public DecisionEngineService(
			RuleRepository ruleRepository,
			RuleVersionRepository ruleVersionRepository,
			ContextSchemaValidator contextSchemaValidator,
			List<RuleEvaluatorStrategy> evaluators,
			RuleScopeMatcher ruleScopeMatcher
	) {
		this.ruleRepository = ruleRepository;
		this.ruleVersionRepository = ruleVersionRepository;
		this.contextSchemaValidator = contextSchemaValidator;
		this.evaluators = evaluators;
		this.ruleScopeMatcher = ruleScopeMatcher;
	}

	@Cacheable(cacheNames = "decisions", keyGenerator = "decisionEvaluationKeyGenerator")
	public EngineResult evaluate(FeatureDefinition featureDefinition, Map<String, Object> context) {
		if (featureDefinition == null) {
			return new EngineResult(
					Decision.DENY,
					List.of("missing feature"),
					new DecisionTrace(List.of()),
					DecisionTraceSummary.preEvaluation("No feature resolved; default deny.")
			);
		}

		ContextValidationResult validation = contextSchemaValidator.validate(featureDefinition.getContextSchema(), context);
		if (!validation.valid()) {
			return new EngineResult(
					Decision.DENY,
					validation.errors(),
					new DecisionTrace(List.of()),
					DecisionTraceSummary.preEvaluation("Context failed JSON Schema validation; rules were not evaluated.")
			);
		}

		List<Rule> rulesForFeature = ruleRepository.findActiveWithScopesForFeature(featureDefinition.getId());
		if (rulesForFeature.isEmpty()) {
			return new EngineResult(
					Decision.DENY,
					List.of("no active rules for feature"),
					new DecisionTrace(List.of()),
					DecisionTraceSummary.preEvaluation("No active rules linked to this feature; default deny.")
			);
		}

		List<Rule> defaultRules = rulesForFeature.stream().filter(Rule::isDefaultRule).collect(Collectors.toList());
		List<Rule> scopedPool = rulesForFeature.stream().filter(r -> !r.isDefaultRule()).collect(Collectors.toList());

		if (defaultRules.size() > 1) {
			return new EngineResult(
					Decision.DENY,
					List.of("configuration error: multiple default rules linked to this feature"),
					new DecisionTrace(List.of()),
					DecisionTraceSummary.preEvaluation("Data integrity violation; refusing to evaluate.")
			);
		}

		Rule defaultForFeature = defaultRules.isEmpty() ? null : defaultRules.getFirst();

		List<Rule> scopedMatches = scopedPool.stream()
				.filter(r -> ruleScopeMatcher.matches(r, context))
				.sorted(
						Comparator
								.comparingInt((Rule r) -> hierarchyRank(r.getHierarchyLevel()))
								.thenComparing(Comparator.comparingInt(Rule::getPriority).reversed())
								.thenComparing(r -> r.getId() == null ? Long.MAX_VALUE : r.getId())
				)
				.toList();

		String evaluationFeatureKey = featureDefinition.getFeatureKey();
		List<DecisionTraceEntry> traceEntries = new ArrayList<>();

		if (!scopedMatches.isEmpty()) {
			evaluateRuleList(scopedMatches, context, traceEntries, evaluationFeatureKey);
		} else if (defaultForFeature != null) {
			evaluateRuleList(List.of(defaultForFeature), context, traceEntries, evaluationFeatureKey);
		} else {
			OptionalGlobalDefault optionalGlobal = resolveOptionalGlobalDefault(featureDefinition.getId());
			if (optionalGlobal.rule() != null) {
				evaluateRuleList(List.of(optionalGlobal.rule()), context, traceEntries, evaluationFeatureKey);
			}
		}

		if (traceEntries.isEmpty()) {
			return new EngineResult(
					Decision.DENY,
					List.of("no matching scoped rules and no applicable default"),
					new DecisionTrace(List.of()),
					DecisionTraceSummary.preEvaluation(
							"No non-default rule matched tenant/sector/role scopes, and no default rule applies for this feature."
					)
			);
		}

		Decision finalDecision = DecisionResolver.resolve(traceEntries);
		List<String> reasons = traceEntries.stream().map(DecisionTraceEntry::reason).toList();
		DecisionTraceSummary traceSummary = DecisionTraceSummary.fromTrace(traceEntries, finalDecision);
		return new EngineResult(finalDecision, reasons, new DecisionTrace(traceEntries), traceSummary);
	}

	private OptionalGlobalDefault resolveOptionalGlobalDefault(Long featureDefinitionId) {
		return ruleRepository.findGlobalDefaultRule()
				.filter(Rule::isActive)
				.filter(r -> r.getFeatures().stream().anyMatch(f -> f.getId().equals(featureDefinitionId)))
				.map(OptionalGlobalDefault::new)
				.orElse(new OptionalGlobalDefault(null));
	}

	private record OptionalGlobalDefault(Rule rule) {}

	private void evaluateRuleList(
			List<Rule> ordered,
			Map<String, Object> context,
			List<DecisionTraceEntry> traceEntries,
			String evaluationFeatureKey
	) {
		for (Rule rule : ordered) {
			RuleVersion latest = ruleVersionRepository.findByRuleIdOrderByIdDesc(rule.getId()).stream().findFirst().orElse(null);
			if (latest == null) {
				continue;
			}

			RuleEvaluatorStrategy evaluator = evaluators.stream().filter(e -> e.supports(rule)).findFirst().orElse(null);
			if (evaluator == null) {
				traceEntries.add(
						new DecisionTraceEntry(
								rule.getHierarchyLevel(),
								rule.getRuleType(),
								rule.getId(),
								latest.getId(),
								Decision.DENY,
								"no evaluator for rule type: " + rule.getRuleType()
						)
				);
				continue;
			}

			EvaluationResult result = evaluator.evaluate(rule, latest, context, evaluationFeatureKey);
			String reason = result.reasons().isEmpty() ? "no reason" : result.reasons().getFirst();
			traceEntries.add(
					new DecisionTraceEntry(
							rule.getHierarchyLevel(),
							rule.getRuleType(),
							rule.getId(),
							latest.getId(),
							result.decision(),
							reason
					)
			);
		}
	}

	static int hierarchyRank(HierarchyLevel level) {
		if (level == null) {
			return Integer.MAX_VALUE;
		}
		return switch (level) {
			case GLOBAL -> 0;
			case SECTOR -> 1;
			case TENANT -> 2;
			case PLAN -> 3;
			case ROLE -> 4;
		};
	}
}
