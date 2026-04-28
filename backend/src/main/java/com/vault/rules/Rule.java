package com.vault.rules;

import java.util.HashSet;
import java.util.Set;

import com.vault.features.FeatureDefinition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "rules")
public class Rule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rule_name", nullable = false)
	private String ruleName;

	@Column(name = "is_default", nullable = false)
	private boolean defaultRuleFlag;

	@Enumerated(EnumType.STRING)
	@Column(name = "hierarchy_level", nullable = false)
	private HierarchyLevel hierarchyLevel;

	@Enumerated(EnumType.STRING)
	@Column(name = "rule_type", nullable = false)
	private RuleType ruleType;

	@Column(nullable = false)
	private int priority;

	@Column(nullable = false)
	private boolean active;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(
			name = "rule_features",
			joinColumns = @JoinColumn(name = "rule_id"),
			inverseJoinColumns = @JoinColumn(name = "feature_definition_id")
	)
	private Set<FeatureDefinition> features = new HashSet<>();

	@OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RuleTenantScope> tenantScopes = new HashSet<>();

	@OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RuleSectorScope> sectorScopes = new HashSet<>();

	@OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RuleRoleScope> roleScopes = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public boolean isDefaultRule() {
		return defaultRuleFlag;
	}

	public void setDefaultRule(boolean defaultRuleFlag) {
		this.defaultRuleFlag = defaultRuleFlag;
	}

	public HierarchyLevel getHierarchyLevel() {
		return hierarchyLevel;
	}

	public void setHierarchyLevel(HierarchyLevel hierarchyLevel) {
		this.hierarchyLevel = hierarchyLevel;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Set<FeatureDefinition> getFeatures() {
		return features;
	}

	public void setFeatures(Set<FeatureDefinition> features) {
		this.features = features;
	}

	public Set<RuleTenantScope> getTenantScopes() {
		return tenantScopes;
	}

	public void setTenantScopes(Set<RuleTenantScope> tenantScopes) {
		this.tenantScopes = tenantScopes;
	}

	public Set<RuleSectorScope> getSectorScopes() {
		return sectorScopes;
	}

	public void setSectorScopes(Set<RuleSectorScope> sectorScopes) {
		this.sectorScopes = sectorScopes;
	}

	public Set<RuleRoleScope> getRoleScopes() {
		return roleScopes;
	}

	public void setRoleScopes(Set<RuleRoleScope> roleScopes) {
		this.roleScopes = roleScopes;
	}
}
