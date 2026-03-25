package com.vault.api.decisions;

import java.util.List;

import com.vault.engine.DecisionTraceEntry;
import com.vault.engine.EngineResult;

public record EvaluateResponse(
		String decision,
		String summary,
		List<String> reasons,
		List<String> evaluationPath,
		Long matchedRuleId,
		Long matchedRuleVersionId,
		List<DecisionTraceEntryResponse> trace
) {
	public static EvaluateResponse from(EngineResult result) {
		List<DecisionTraceEntryResponse> traceRows = result.trace().entries().stream()
				.map(DecisionTraceEntryResponse::from)
				.toList();
		var ts = result.traceSummary();
		return new EvaluateResponse(
				result.decision().name(),
				ts.summary(),
				result.reasons(),
				ts.evaluationPath(),
				ts.matchedRuleId(),
				ts.matchedRuleVersionId(),
				traceRows
		);
	}

	public record DecisionTraceEntryResponse(
			String hierarchyLevel,
			String ruleType,
			Long ruleId,
			Long ruleVersionId,
			String decision,
			String reason
	) {
		static DecisionTraceEntryResponse from(DecisionTraceEntry e) {
			return new DecisionTraceEntryResponse(
					e.hierarchyLevel() != null ? e.hierarchyLevel().name() : null,
					e.ruleType() != null ? e.ruleType().name() : null,
					e.ruleId(),
					e.ruleVersionId(),
					e.decision() != null ? e.decision().name() : null,
					e.reason()
			);
		}
	}
}
