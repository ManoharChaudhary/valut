package com.vault.features;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureDefinitionRepository extends JpaRepository<FeatureDefinition, Long> {
	Optional<FeatureDefinition> findByFeatureKey(String featureKey);
}

