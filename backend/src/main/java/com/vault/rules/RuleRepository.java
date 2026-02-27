package com.vault.rules;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleRepository extends JpaRepository<Rule, Long> {
	List<Rule> findByFeatureKeyAndActiveTrue(String featureKey);
}

