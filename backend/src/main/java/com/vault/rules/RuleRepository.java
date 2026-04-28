package com.vault.rules;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RuleRepository extends JpaRepository<Rule, Long> {

	@EntityGraph(attributePaths = {
			"features",
			"tenantScopes",
			"tenantScopes.tenant",
			"sectorScopes",
			"sectorScopes.sector",
			"roleScopes",
			"roleScopes.role"
	})
	@Query("select distinct r from Rule r join r.features f where f.id = :featureId and r.active = true")
	List<Rule> findActiveWithScopesForFeature(@Param("featureId") Long featureId);

	@EntityGraph(attributePaths = { "features" })
	@Query("select r from Rule r where r.defaultRuleFlag = true")
	Optional<Rule> findGlobalDefaultRule();

	@EntityGraph(attributePaths = { "features" })
	@Query("select r from Rule r where r.id = :id")
	Optional<Rule> findByIdWithFeatures(@Param("id") Long id);

	@EntityGraph(attributePaths = {
			"features",
			"tenantScopes",
			"tenantScopes.tenant",
			"sectorScopes",
			"sectorScopes.sector",
			"roleScopes",
			"roleScopes.role"
	})
	@Query("select r from Rule r where r.id = :id")
	Optional<Rule> findByIdWithAssociations(@Param("id") Long id);

	@Modifying
	@Query("update Rule r set r.defaultRuleFlag = false")
	void clearAllDefaultFlags();

	@Query("select distinct r from Rule r left join fetch r.features")
	List<Rule> findAllWithFeatures();
}
