package com.vault.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.vault.rules.RuleUpdatedEvent;

/**
 * Evicts decision evaluation cache when rules change.
 * // TODO: feature-scoped eviction (only keys for event.featureKey) once we index keys by feature.
 */
@Component
public class RuleCacheEvictionListener {
	private final CacheManager cacheManager;

	public RuleCacheEvictionListener(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@EventListener
	public void onRuleUpdated(RuleUpdatedEvent event) {
		Cache cache = cacheManager.getCache("decisions");
		if (cache != null) {
			cache.clear();
		}
	}
}
