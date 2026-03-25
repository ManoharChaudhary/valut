package com.vault.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vault.rules.HierarchyLevel;
import com.vault.rules.RuleType;

class DecisionTraceSummaryTest {

	@Test
	void denyPicksFirstDenyStep() {
		var entries = List.of(
				new DecisionTraceEntry(HierarchyLevel.GLOBAL, RuleType.BOOLEAN, 1L, 10L, Decision.ALLOW, "allow"),
				new DecisionTraceEntry(HierarchyLevel.TENANT, RuleType.ROLLOUT, 2L, 20L, Decision.DENY, "rollout off")
		);
		var s = DecisionTraceSummary.fromTrace(entries, Decision.DENY);
		assertThat(s.matchedRuleId()).isEqualTo(2L);
		assertThat(s.matchedRuleVersionId()).isEqualTo(20L);
		assertThat(s.evaluationPath()).hasSize(2);
		assertThat(s.summary()).contains("DENY");
	}

	@Test
	void allowPicksLastAllowStep() {
		var entries = List.of(
				new DecisionTraceEntry(HierarchyLevel.GLOBAL, RuleType.BOOLEAN, 1L, 10L, Decision.ALLOW, "a"),
				new DecisionTraceEntry(HierarchyLevel.TENANT, RuleType.BOOLEAN, 2L, 20L, Decision.ALLOW, "b")
		);
		var s = DecisionTraceSummary.fromTrace(entries, Decision.ALLOW);
		assertThat(s.matchedRuleId()).isEqualTo(2L);
		assertThat(s.summary()).contains("ALLOW");
	}
}
