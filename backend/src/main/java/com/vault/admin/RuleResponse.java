package com.vault.admin;

import java.util.List;

import com.vault.features.FeatureDefinition;
import com.vault.rules.HierarchyLevel;
import com.vault.rules.Rule;
import com.vault.rules.RuleType;

public record RuleResponse(
		long id,
		String ruleName,
		boolean defaultRule,
		HierarchyLevel hierarchyLevel,
		RuleType ruleType,
		int priority,
		boolean active,
		List<Long> featureDefinitionIds,
		List<Long> tenantIds,
		List<Long> sectorIds,
		List<Long> roleIds
) {
	public static RuleResponse from(Rule r) {
		List<Long> fids = r.getFeatures().stream().map(FeatureDefinition::getId).sorted().toList();
		List<Long> tids = r.getTenantScopes().stream().map(ts -> ts.getTenant().getId()).sorted().toList();
		List<Long> sids = r.getSectorScopes().stream().map(ss -> ss.getSector().getId()).sorted().toList();
		List<Long> rids = r.getRoleScopes().stream().map(rr -> rr.getRole().getId()).sorted().toList();
		return new RuleResponse(
				r.getId(),
				r.getRuleName(),
				r.isDefaultRule(),
				r.getHierarchyLevel(),
				r.getRuleType(),
				r.getPriority(),
				r.isActive(),
				fids,
				tids,
				sids,
				rids
		);
	}
}
