package com.vault.cache;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vault.engine.MurmurHash3;
import com.vault.features.FeatureDefinition;

/**
 * Deterministic cache key: featureKey + stable JSON for context map (ordered keys).
 * Avoids randomness; same logical request hits the same bucket.
 */
@Component("decisionEvaluationKeyGenerator")
public class DecisionEvaluationKeyGenerator implements KeyGenerator {
	private final ObjectMapper sortedJson = new ObjectMapper()
			.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

	@Override
	public Object generate(Object target, Method method, Object... params) {
		if (params.length < 2) {
			return "invalid";
		}
		String featureKey;
		if (params[0] instanceof FeatureDefinition fd) {
			featureKey = fd.getFeatureKey();
		} else if (params[0] instanceof String s) {
			featureKey = s;
		} else {
			featureKey = String.valueOf(params[0]);
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> context = params[1] instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();

		String canonicalJson;
		try {
			canonicalJson = sortedJson.writeValueAsString(context == null ? Map.of() : context);
		} catch (JsonProcessingException e) {
			// TODO: if context contains non-JSON-serializable values, fall back to string hash of toString()
			canonicalJson = String.valueOf(context);
		}

		int h = MurmurHash3.murmur3_32(featureKey + "\u0000" + canonicalJson);
		return "decision:" + featureKey + ":" + Integer.toUnsignedString(h);
	}
}
