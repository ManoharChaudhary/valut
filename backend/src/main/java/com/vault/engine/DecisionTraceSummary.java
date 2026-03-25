package com.vault.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Human-oriented view of how we reached {@code finalDecision}: ordered path, matched rule/version, summary line.
 */
public record DecisionTraceSummary(
		String summary,
		List<String> evaluationPath,
		Long matchedRuleId,
		Long matchedRuleVersionId
) {
	public static DecisionTraceSummary preEvaluation(String summary) {
		return new DecisionTraceSummary(summary, List.of(), null, null);
	}

	public static DecisionTraceSummary fromTrace(List<DecisionTraceEntry> entries, Decision finalDecision) {
		if (entries == null || entries.isEmpty()) {
			String s = finalDecision == Decision.DENY
					? "Default deny: no rule evaluation steps ran (check feature definition, schema, or active rules)."
					: "Allow with no trace rows (unexpected).";
			return new DecisionTraceSummary(s, List.of(), null, null);
		}

		List<String> path = new ArrayList<>();
		for (DecisionTraceEntry e : entries) {
			path.add(formatStep(e));
		}

		DecisionTraceEntry matched = pickMatched(entries, finalDecision);
		Long ruleId = matched != null ? matched.ruleId() : null;
		Long verId = matched != null ? matched.ruleVersionId() : null;
		String summary = buildSummary(finalDecision, matched, entries.size());
		return new DecisionTraceSummary(summary, List.copyOf(path), ruleId, verId);
	}

	private static String formatStep(DecisionTraceEntry e) {
		String level = e.hierarchyLevel() != null ? e.hierarchyLevel().name() : "?";
		String type = e.ruleType() != null ? e.ruleType().name() : "?";
		return "[%s | %s] ruleId=%s versionId=%s -> %s — %s".formatted(
				level,
				type,
				e.ruleId(),
				e.ruleVersionId(),
				e.decision(),
				e.reason()
		);
	}

	/**
	 * If final outcome is DENY, attribute to the first DENY step in evaluation order (deterministic).
	 * If ALLOW, attribute to the last ALLOW step (broad-to-narrow evaluation: last is most specific).
	 */
	private static DecisionTraceEntry pickMatched(List<DecisionTraceEntry> entries, Decision finalDecision) {
		if (finalDecision == Decision.DENY) {
			for (DecisionTraceEntry e : entries) {
				if (e.decision() == Decision.DENY) {
					return e;
				}
			}
			return null;
		}
		for (int i = entries.size() - 1; i >= 0; i--) {
			if (entries.get(i).decision() == Decision.ALLOW) {
				return entries.get(i);
			}
		}
		return null;
	}

	private static String buildSummary(Decision finalDecision, DecisionTraceEntry matched, int stepCount) {
		if (matched == null) {
			return finalDecision.name() + ": no single matched rule (evaluated " + stepCount + " step(s)).";
		}
		String level = matched.hierarchyLevel() != null ? matched.hierarchyLevel().name() : "?";
		return "%s after %d rule step(s); primary signal from [%s] ruleId=%s versionId=%s — %s".formatted(
				finalDecision.name(),
				stepCount,
				level,
				matched.ruleId(),
				matched.ruleVersionId(),
				matched.reason()
		);
	}
}
