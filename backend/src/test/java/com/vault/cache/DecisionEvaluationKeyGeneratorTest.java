package com.vault.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DecisionEvaluationKeyGeneratorTest {

	private final DecisionEvaluationKeyGenerator generator = new DecisionEvaluationKeyGenerator();

	@Test
	void sameFeatureAndContextDifferentInsertionOrderProducesSameKey() throws Exception {
		var a = new LinkedHashMap<String, Object>();
		a.put("z", 1);
		a.put("a", 2);
		var b = new LinkedHashMap<String, Object>();
		b.put("a", 2);
		b.put("z", 1);

		Object k1 = generator.generate(null, null, "my.feature", a);
		Object k2 = generator.generate(null, null, "my.feature", b);
		assertThat(k1).isEqualTo(k2);
	}

	@Test
	void differentFeatureProducesDifferentKey() {
		Map<String, Object> ctx = Map.of("tenant_id", "t1");
		Object k1 = generator.generate(null, null, "a", ctx);
		Object k2 = generator.generate(null, null, "b", ctx);
		assertThat(k1).isNotEqualTo(k2);
	}
}
