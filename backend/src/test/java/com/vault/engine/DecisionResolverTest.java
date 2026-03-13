package com.vault.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vault.rules.HierarchyLevel;
import com.vault.rules.RuleType;

class DecisionResolverTest {
	@Test
	void emptyIsDefaultDeny() {
		assertThat(DecisionResolver.resolve(List.of())).isEqualTo(Decision.DENY);
	}

	@Test
	void anyDenyOverridesAllow() {
		var entries = List.of(
				new DecisionTraceEntry(HierarchyLevel.GLOBAL, RuleType.BOOLEAN, 1L, 11L, Decision.ALLOW, "global allow"),
				new DecisionTraceEntry(HierarchyLevel.TENANT, RuleType.ROLLOUT, 2L, 22L, Decision.DENY, "tenant deny")
		);
		assertThat(DecisionResolver.resolve(entries)).isEqualTo(Decision.DENY);
	}

	@Test
	void allowWhenNoDeniesAndAtLeastOneAllow() {
		var entries = List.of(
				new DecisionTraceEntry(HierarchyLevel.GLOBAL, RuleType.BOOLEAN, 1L, 11L, Decision.ALLOW, "global allow")
		);
		assertThat(DecisionResolver.resolve(entries)).isEqualTo(Decision.ALLOW);
	}
}

