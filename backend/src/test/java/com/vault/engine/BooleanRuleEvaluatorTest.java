package com.vault.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vault.rules.HierarchyLevel;
import com.vault.rules.Rule;
import com.vault.rules.RuleType;
import com.vault.rules.RuleVersion;

class BooleanRuleEvaluatorTest {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void allow_true_returns_allow() throws Exception {
		Rule rule = new Rule();
		rule.setRuleType(RuleType.BOOLEAN);
		rule.setHierarchyLevel(HierarchyLevel.GLOBAL);

		RuleVersion v = new RuleVersion();
		v.setConditions(objectMapper.readTree("{\"allow\": true}"));

		BooleanRuleEvaluator evaluator = new BooleanRuleEvaluator();
		EvaluationResult result = evaluator.evaluate(rule, v, Map.of());

		assertThat(result.decision()).isEqualTo(Decision.ALLOW);
	}

	@Test
	void allow_false_returns_deny() throws Exception {
		Rule rule = new Rule();
		rule.setRuleType(RuleType.BOOLEAN);
		rule.setHierarchyLevel(HierarchyLevel.GLOBAL);

		RuleVersion v = new RuleVersion();
		v.setConditions(objectMapper.readTree("{\"allow\": false}"));

		BooleanRuleEvaluator evaluator = new BooleanRuleEvaluator();
		EvaluationResult result = evaluator.evaluate(rule, v, Map.of());

		assertThat(result.decision()).isEqualTo(Decision.DENY);
	}
}

