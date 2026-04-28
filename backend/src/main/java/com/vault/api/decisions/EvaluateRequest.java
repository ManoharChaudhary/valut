package com.vault.api.decisions;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Provide {@code featureId} (UUID of {@code feature_definitions.public_id}) or {@code featureKey} (non-blank).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EvaluateRequest(UUID featureId, String featureKey, Map<String, Object> context) {
	public EvaluateRequest {
		if (context == null) {
			context = Map.of();
		}
	}

	public boolean hasLocator() {
		return featureId != null || (featureKey != null && !featureKey.isBlank());
	}
}
