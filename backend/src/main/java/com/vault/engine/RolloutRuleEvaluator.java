package com.vault.engine;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.vault.rules.Rule;
import com.vault.rules.RuleType;
import com.vault.rules.RuleVersion;

@Component
public class RolloutRuleEvaluator implements RuleEvaluatorStrategy {
	@Override
	public boolean supports(Rule rule) {
		return rule.getRuleType() == RuleType.ROLLOUT;
	}

	@Override
	public EvaluationResult evaluate(Rule rule, RuleVersion ruleVersion, Map<String, Object> context) {
		JsonNode conditions = ruleVersion.getConditions();
		if (conditions == null) {
			return EvaluationResult.deny("rollout rule is missing conditions");
		}

		int percentage = readInt(conditions, "percentage");
		if (percentage < 0 || percentage > 100) {
			return EvaluationResult.deny("rollout rule requires 0..100 conditions.percentage");
		}

		String subjectId = extractSubjectId(context);
		if (subjectId == null || subjectId.isBlank()) {
			return EvaluationResult.deny("rollout requires context.tenant_id or context.user_id");
		}

		String key = rule.getFeatureKey() + ":" + subjectId;
		int hash = MurmurHash3.murmur3_32(key);
		int bucket = Math.floorMod(hash, 100); // 0..99 stable bucket

		boolean enabled = bucket < percentage;
		String reason = "rollout bucket=" + bucket + " percentage=" + percentage;
		return enabled ? EvaluationResult.allow(reason) : EvaluationResult.deny(reason);
	}

	private static int readInt(JsonNode obj, String field) {
		JsonNode n = obj.get(field);
		if (n == null || !n.canConvertToInt()) {
			return -1;
		}
		return n.asInt();
	}

	private static String extractSubjectId(Map<String, Object> context) {
		Object tenantId = context.get("tenant_id");
		if (tenantId != null) {
			return Objects.toString(tenantId, null);
		}
		Object userId = context.get("user_id");
		if (userId != null) {
			return Objects.toString(userId, null);
		}
		return null;
	}
}

