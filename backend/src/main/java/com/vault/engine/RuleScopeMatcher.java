package com.vault.engine;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vault.rules.Rule;
import com.vault.rules.RuleRoleScope;
import com.vault.rules.RuleSectorScope;
import com.vault.rules.RuleTenantScope;

@Component
public class RuleScopeMatcher {

	public boolean matches(Rule rule, Map<String, Object> context) {
		if (!matchesTenants(rule, context)) {
			return false;
		}
		if (!matchesSectors(rule, context)) {
			return false;
		}
		return matchesRoles(rule, context);
	}

	private static boolean matchesTenants(Rule rule, Map<String, Object> context) {
		if (rule.getTenantScopes() == null || rule.getTenantScopes().isEmpty()) {
			return true;
		}
		String tenantKey = asString(context.get("tenant_id"));
		if (tenantKey == null || tenantKey.isBlank()) {
			return false;
		}
		UUID tenantUuid = tryParseUuid(tenantKey);
		for (RuleTenantScope ts : rule.getTenantScopes()) {
			if (ts.getTenant() == null) {
				continue;
			}
			if (tenantUuid != null && tenantUuid.equals(ts.getTenant().getPublicId())) {
				return true;
			}
			if (tenantKey.equalsIgnoreCase(String.valueOf(ts.getTenant().getId()))) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesSectors(Rule rule, Map<String, Object> context) {
		if (rule.getSectorScopes() == null || rule.getSectorScopes().isEmpty()) {
			return true;
		}
		String sectorCode = asString(context.get("sector"));
		if (sectorCode == null || sectorCode.isBlank()) {
			return false;
		}
		String normalized = sectorCode.trim().toUpperCase(Locale.ROOT);
		for (RuleSectorScope ss : rule.getSectorScopes()) {
			if (ss.getSector() != null && normalized.equalsIgnoreCase(ss.getSector().getCode())) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesRoles(Rule rule, Map<String, Object> context) {
		if (rule.getRoleScopes() == null || rule.getRoleScopes().isEmpty()) {
			return true;
		}
		String roleIdStr = asString(context.get("role_id"));
		String roleName = asString(context.get("role_name"));
		UUID roleUuid = tryParseUuid(roleIdStr);
		for (RuleRoleScope rs : rule.getRoleScopes()) {
			if (rs.getRole() == null) {
				continue;
			}
			if (roleUuid != null && roleUuid.equals(rs.getRole().getPublicId())) {
				return true;
			}
			if (roleName != null && !roleName.isBlank()
					&& roleName.trim().equalsIgnoreCase(rs.getRole().getName())) {
				return true;
			}
		}
		return false;
	}

	private static String asString(Object o) {
		return o == null ? null : Objects.toString(o, null);
	}

	private static UUID tryParseUuid(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(s.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
