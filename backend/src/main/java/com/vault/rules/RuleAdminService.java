package com.vault.rules;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Append-only rule mutations and domain events.
 * Future REST controllers should call this instead of repositories directly.
 */
@Service
public class RuleAdminService {
	private final RuleRepository ruleRepository;
	private final RuleVersionRepository ruleVersionRepository;
	private final ApplicationEventPublisher eventPublisher;

	public RuleAdminService(
			RuleRepository ruleRepository,
			RuleVersionRepository ruleVersionRepository,
			ApplicationEventPublisher eventPublisher
	) {
		this.ruleRepository = ruleRepository;
		this.ruleVersionRepository = ruleVersionRepository;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public RuleVersion appendRuleVersion(
			long ruleId,
			JsonNode conditions,
			JsonNode variantValue,
			String createdBy
	) {
		Rule rule = ruleRepository.findById(ruleId).orElseThrow(() -> new IllegalArgumentException("rule not found: " + ruleId));

		RuleVersion version = new RuleVersion();
		version.setRule(rule);
		version.setConditions(conditions);
		version.setVariantValue(variantValue);
		version.setCreatedBy(createdBy);
		version.setCreatedAt(Instant.now());

		RuleVersion saved = ruleVersionRepository.save(version);
		eventPublisher.publishEvent(new RuleUpdatedEvent(rule.getFeatureKey(), rule.getId()));
		return saved;
	}
}
