package com.vault.engine;

import java.util.List;

/**
 * Minimal “engine internal” result. We'll grow this later to include variants and rich trace.
 */
public record EvaluationResult(Decision decision, List<String> reasons) {
	public static EvaluationResult allow(String reason) {
		return new EvaluationResult(Decision.ALLOW, List.of(reason));
	}

	public static EvaluationResult deny(String reason) {
		return new EvaluationResult(Decision.DENY, List.of(reason));
	}
}

