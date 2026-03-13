package com.vault.engine;

import java.util.List;

/**
 * Conflict resolution rules (Vault):
 * - Default deny (no matches -> DENY)
 * - DENY overrides ALLOW (any deny anywhere => DENY)
 */
public final class DecisionResolver {
	private DecisionResolver() {}

	public static Decision resolve(List<DecisionTraceEntry> entries) {
		if (entries == null || entries.isEmpty()) {
			return Decision.DENY;
		}

		boolean sawAllow = false;
		for (DecisionTraceEntry entry : entries) {
			if (entry == null) {
				continue;
			}
			if (entry.decision() == Decision.DENY) {
				return Decision.DENY;
			}
			if (entry.decision() == Decision.ALLOW) {
				sawAllow = true;
			}
		}

		return sawAllow ? Decision.ALLOW : Decision.DENY;
	}
}

