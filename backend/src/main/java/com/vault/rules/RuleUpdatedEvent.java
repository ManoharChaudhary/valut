package com.vault.rules;

/**
 * Published after a rule (or its versions) changes so caches and other subscribers can refresh.
 */
public record RuleUpdatedEvent(String featureKey, Long ruleId) {}
