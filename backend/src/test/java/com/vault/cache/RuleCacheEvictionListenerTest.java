package com.vault.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.vault.rules.RuleUpdatedEvent;

class RuleCacheEvictionListenerTest {

	@Test
	void clearsDecisionsCacheOnEvent() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("decisions");
		Cache cache = cacheManager.getCache("decisions");
		cache.put("decision:demo:abc123", "payload");

		RuleCacheEvictionListener listener = new RuleCacheEvictionListener(cacheManager);
		listener.onRuleUpdated(new RuleUpdatedEvent(List.of("demo.feature"), 99L));

		assertThat(cache.get("decision:demo:abc123")).isNull();
	}
}
