package com.vault.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vault.rules.HierarchyLevel;
import com.vault.rules.Rule;
import com.vault.rules.RuleRepository;
import com.vault.rules.RuleVersion;
import com.vault.rules.RuleVersionRepository;

@Service
public class DecisionEngineService {
	private final RuleRepository ruleRepository;
	private final RuleVersionRepository ruleVersionRepository;
	private final List<RuleEvaluatorStrategy> evaluators;

	public DecisionEngineService(
			RuleRepository ruleRepository,
			RuleVersionRepository ruleVersionRepository,
			List<RuleEvaluatorStrategy> evaluators
	) {
		this.ruleRepository = ruleRepository;
		this.ruleVersionRepository = ruleVersionRepository;
		this.evaluators = evaluators;
	}

	public EngineResult evaluate(String featureKey, Map<String, Object> context) {
		if (featureKey == null || featureKey.isBlank()) {
			return new EngineResult(Decision.DENY, List.of("missing featureKey"), new DecisionTrace(List.of()));
		}

		List<Rule> rules = ruleRepository.findByFeatureKeyAndActiveTrue(featureKey);
		if (rules.isEmpty()) {
			return new EngineResult(Decision.DENY, List.of("no active rules for feature"), new DecisionTrace(List.of()));
		}

		List<DecisionTraceEntry> traceEntries = new ArrayList<>();

		// Deterministic ordering:
		// 1) Hierarchy level (GLOBAL -> ... -> ROLE)
		// 2) Priority (higher first)
		// 3) Stable tie-breaker (id)
		List<Rule> ordered = rules.stream()
				.sorted(
						Comparator
								.comparingInt((Rule r) -> hierarchyRank(r.getHierarchyLevel()))
								.thenComparing(Comparator.comparingInt(Rule::getPriority).reversed())
								.thenComparing(r -> r.getId() == null ? Long.MAX_VALUE : r.getId())
				)
				.toList();

		for (Rule rule : rules) {
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

			EvaluationResult result = evaluator.evaluate(rule, latest, context);
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

		Decision finalDecision = DecisionResolver.resolve(traceEntries);
		List<String> reasons = traceEntries.stream().map(DecisionTraceEntry::reason).toList();
		return new EngineResult(finalDecision, reasons, new DecisionTrace(traceEntries));
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

