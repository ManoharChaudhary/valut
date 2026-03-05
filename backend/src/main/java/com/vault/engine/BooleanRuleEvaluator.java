package com.vault.engine;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.vault.rules.Rule;
import com.vault.rules.RuleType;
import com.vault.rules.RuleVersion;

@Component
public class BooleanRuleEvaluator implements RuleEvaluatorStrategy {
	@Override
	public boolean supports(Rule rule) {
		return rule.getRuleType() == RuleType.BOOLEAN;
	}

	@Override
	public EvaluationResult evaluate(Rule rule, RuleVersion ruleVersion, Map<String, Object> context) {
		JsonNode conditions = ruleVersion.getConditions();
		if (conditions == null) {
			return EvaluationResult.deny("boolean rule is missing conditions");
		}

		// Minimal shape:
		// { "allow": true } OR { "allow": false }
		JsonNode allowNode = conditions.get("allow");
		if (allowNode == null || !allowNode.isBoolean()) {
			return EvaluationResult.deny("boolean rule requires conditions.allow boolean");
		}

		boolean allow = allowNode.asBoolean();
		return allow ? EvaluationResult.allow("boolean allow=true") : EvaluationResult.deny("boolean allow=false");
	}
}

