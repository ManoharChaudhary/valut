package com.vault.engine;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

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

	public EvaluationResult evaluate(String featureKey, Map<String, Object> context) {
		if (featureKey == null || featureKey.isBlank()) {
			return EvaluationResult.deny("missing featureKey");
		}

		List<Rule> rules = ruleRepository.findByFeatureKeyAndActiveTrue(featureKey);
		if (rules.isEmpty()) {
			return EvaluationResult.deny("no active rules for feature");
		}

		// TODO: implement full hierarchy ordering + priority sorting.
		for (Rule rule : rules) {
			RuleVersion latest = ruleVersionRepository.findByRuleIdOrderByIdDesc(rule.getId()).stream().findFirst().orElse(null);
			if (latest == null) {
				continue;
			}

			RuleEvaluatorStrategy evaluator = evaluators.stream().filter(e -> e.supports(rule)).findFirst().orElse(null);
			if (evaluator == null) {
				// Default deny is safer than "best effort"
				return EvaluationResult.deny("no evaluator for rule type: " + rule.getRuleType());
			}

			EvaluationResult result = evaluator.evaluate(rule, latest, context);
			// For now, we short-circuit on first evaluated rule.
			// We'll replace this with conflict resolution + trace in Phase 2.3/3.3.
			return result;
		}

		return EvaluationResult.deny("no rule versions found");
	}
}

