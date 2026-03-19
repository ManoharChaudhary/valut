package com.vault.api.decisions;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

public record EvaluateRequest(
		@NotBlank String featureKey,
		Map<String, Object> context
) {
	public EvaluateRequest {
		if (context == null) {
			context = Map.of();
		}
	}
}
