package com.vault.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vault.rules.Rule;
import com.vault.rules.RuleType;
import com.vault.rules.RuleVersion;

class RolloutRuleEvaluatorTest {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void sameSubjectAlwaysMapsToSameBucket() throws Exception {
		Rule rule = new Rule();
		rule.setFeatureKey("new_dashboard");
		rule.setRuleType(RuleType.ROLLOUT);

		RuleVersion v = new RuleVersion();
		v.setConditions(objectMapper.readTree("{\"percentage\": 50}"));

		RolloutRuleEvaluator evaluator = new RolloutRuleEvaluator();

		var r1 = evaluator.evaluate(rule, v, Map.of("tenant_id", "t-123"));
		var r2 = evaluator.evaluate(rule, v, Map.of("tenant_id", "t-123"));

		assertThat(r1.reasons()).isEqualTo(r2.reasons());
		assertThat(r1.decision()).isEqualTo(r2.decision());
	}

	@Test
	void percentage_0_is_always_deny_and_100_is_always_allow() throws Exception {
		Rule rule = new Rule();
		rule.setFeatureKey("new_dashboard");
		rule.setRuleType(RuleType.ROLLOUT);

		RolloutRuleEvaluator evaluator = new RolloutRuleEvaluator();

		RuleVersion v0 = new RuleVersion();
		v0.setConditions(objectMapper.readTree("{\"percentage\": 0}"));
		assertThat(evaluator.evaluate(rule, v0, Map.of("tenant_id", "t-123")).decision()).isEqualTo(Decision.DENY);

		RuleVersion v100 = new RuleVersion();
		v100.setConditions(objectMapper.readTree("{\"percentage\": 100}"));
		assertThat(evaluator.evaluate(rule, v100, Map.of("tenant_id", "t-123")).decision()).isEqualTo(Decision.ALLOW);
	}
}

