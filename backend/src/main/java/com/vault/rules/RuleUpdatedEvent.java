package com.vault.rules;

import java.util.List;

/**
 * Published after a rule (or its versions) changes so caches and other subscribers can refresh.
 */
public record RuleUpdatedEvent(List<String> affectedFeatureKeys, Long ruleId) {}
