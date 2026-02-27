package com.vault.rules;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleVersionRepository extends JpaRepository<RuleVersion, Long> {
	List<RuleVersion> findByRuleIdOrderByIdDesc(Long ruleId);
}

