package com.vault.engine;

import java.util.Map;

import com.vault.rules.Rule;
import com.vault.rules.RuleVersion;

/**
 * Strategy pattern for evaluating a rule version against a context.
 *
 * Think of this like a “pluggable hook” — we can add new rule types (geo-fencing, device checks)
 * without changing the engine orchestrator.
 */
public interface RuleEvaluatorStrategy {
	boolean supports(Rule rule);

	EvaluationResult evaluate(Rule rule, RuleVersion ruleVersion, Map<String, Object> context);
}

