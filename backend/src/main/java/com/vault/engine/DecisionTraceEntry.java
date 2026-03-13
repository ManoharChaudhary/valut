package com.vault.engine;

import com.vault.rules.HierarchyLevel;
import com.vault.rules.RuleType;

public record DecisionTraceEntry(
		HierarchyLevel hierarchyLevel,
		RuleType ruleType,
		Long ruleId,
		Long ruleVersionId,
		Decision decision,
		String reason
) {}

