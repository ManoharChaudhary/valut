package com.vault.admin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vault.catalog.RoleEntity;
import com.vault.catalog.RoleEntityRepository;
import com.vault.catalog.Sector;
import com.vault.catalog.SectorRepository;
import com.vault.features.FeatureDefinition;
import com.vault.features.FeatureDefinitionRepository;
import com.vault.rules.HierarchyLevel;
import com.vault.rules.Rule;
import com.vault.rules.RuleRepository;
import com.vault.rules.RuleRoleScope;
import com.vault.rules.RuleSectorScope;
import com.vault.rules.RuleTenantScope;
import com.vault.rules.RuleType;
import com.vault.rules.RuleUpdatedEvent;
import com.vault.rules.RuleVersion;
import com.vault.rules.RuleVersionRepository;
import com.vault.tenancy.Tenant;
import com.vault.tenancy.TenantRepository;

import org.springframework.http.HttpStatus;

@Service
public class RuleManagementService {
	private final RuleRepository ruleRepository;
	private final RuleVersionRepository ruleVersionRepository;
	private final FeatureDefinitionRepository featureDefinitionRepository;
	private final TenantRepository tenantRepository;
	private final SectorRepository sectorRepository;
	private final RoleEntityRepository roleEntityRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final ObjectMapper objectMapper;

	public RuleManagementService(
			RuleRepository ruleRepository,
			RuleVersionRepository ruleVersionRepository,
			FeatureDefinitionRepository featureDefinitionRepository,
			TenantRepository tenantRepository,
			SectorRepository sectorRepository,
			RoleEntityRepository roleEntityRepository,
			ApplicationEventPublisher eventPublisher,
			ObjectMapper objectMapper
	) {
		this.ruleRepository = ruleRepository;
		this.ruleVersionRepository = ruleVersionRepository;
		this.featureDefinitionRepository = featureDefinitionRepository;
		this.tenantRepository = tenantRepository;
		this.sectorRepository = sectorRepository;
		this.roleEntityRepository = roleEntityRepository;
		this.eventPublisher = eventPublisher;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public List<RuleResponse> listRuleResponses() {
		Map<Long, Rule> byId = new LinkedHashMap<>();
		for (Rule r : ruleRepository.findAllWithFeatures()) {
			byId.putIfAbsent(r.getId(), r);
		}
		return byId.values().stream().map(RuleResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public RuleResponse getRuleResponse(long id) {
		Rule r = ruleRepository.findByIdWithAssociations(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "rule not found"));
		return RuleResponse.from(r);
	}

	@Transactional
	public RuleResponse createRule(RuleWriteRequest request) {
		// TODO: Add audit logging for rule changes
		if (request.featureDefinitionIds() == null || request.featureDefinitionIds().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one featureDefinitionId required");
		}
		if (request.defaultRule()) {
			ruleRepository.clearAllDefaultFlags();
			ruleRepository.flush();
		}

		Rule rule = new Rule();
		applyRuleFields(rule, request);
		wireFeatures(rule, request.featureDefinitionIds());
		wireScopes(rule, request.tenantIds(), request.sectorIds(), request.roleIds());
		Rule saved = ruleRepository.save(rule);

		JsonNode conditions = readJson(request.conditions());
		RuleVersion v = new RuleVersion();
		v.setRule(saved);
		v.setConditions(conditions);
		v.setVariantValue(readJsonNullable(request.variantValue()));
		v.setCreatedBy(request.createdBy() == null ? "admin" : request.createdBy());
		v.setCreatedAt(Instant.now());
		ruleVersionRepository.save(v);

		publishUpdated(saved.getId());
		Rule loaded = ruleRepository.findByIdWithAssociations(saved.getId()).orElse(saved);
		return RuleResponse.from(loaded);
	}

	@Transactional
	public RuleResponse updateRule(long id, RuleWriteRequest request) {
		// TODO: Add audit logging for rule changes
		Rule rule = ruleRepository.findByIdWithAssociations(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "rule not found"));

		if (request.defaultRule()) {
			ruleRepository.clearAllDefaultFlags();
			ruleRepository.flush();
		}

		applyRuleFields(rule, request);
		if (request.featureDefinitionIds() != null && !request.featureDefinitionIds().isEmpty()) {
			rule.getFeatures().clear();
			wireFeatures(rule, request.featureDefinitionIds());
		}
		rule.getTenantScopes().clear();
		rule.getSectorScopes().clear();
		rule.getRoleScopes().clear();
		wireScopes(rule, request.tenantIds(), request.sectorIds(), request.roleIds());

		Rule saved = ruleRepository.save(rule);
		publishUpdated(saved.getId());
		Rule loaded = ruleRepository.findByIdWithAssociations(saved.getId()).orElse(saved);
		return RuleResponse.from(loaded);
	}

	@Transactional
	public void deleteRule(long id) {
		// TODO: Add audit logging for rule changes
		Rule rule = ruleRepository.findByIdWithFeatures(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "rule not found"));
		List<String> keys = rule.getFeatures().stream().map(FeatureDefinition::getFeatureKey).toList();
		ruleRepository.delete(rule);
		eventPublisher.publishEvent(new RuleUpdatedEvent(keys, id));
	}

	private void applyRuleFields(Rule rule, RuleWriteRequest request) {
		rule.setRuleName(request.ruleName() == null || request.ruleName().isBlank() ? "Unnamed rule" : request.ruleName());
		rule.setDefaultRule(request.defaultRule());
		rule.setHierarchyLevel(request.hierarchyLevel());
		rule.setRuleType(request.ruleType());
		rule.setPriority(request.priority());
		rule.setActive(request.active());
	}

	private void wireFeatures(Rule rule, List<Long> featureDefinitionIds) {
		List<FeatureDefinition> defs = featureDefinitionRepository.findAllById(featureDefinitionIds);
		if (defs.size() != featureDefinitionIds.size()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown featureDefinitionId");
		}
		rule.getFeatures().addAll(defs);
	}

	private void wireScopes(Rule rule, List<Long> tenantIds, List<Long> sectorIds, List<Long> roleIds) {
		if (tenantIds != null) {
			for (Long tid : new HashSet<>(tenantIds)) {
				Tenant t = tenantRepository.findById(tid).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown tenant id"));
				RuleTenantScope s = new RuleTenantScope();
				s.setRule(rule);
				s.setTenant(t);
				rule.getTenantScopes().add(s);
			}
		}
		if (sectorIds != null) {
			for (Long sid : new HashSet<>(sectorIds)) {
				Sector s = sectorRepository.findById(sid).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown sector id"));
				RuleSectorScope rs = new RuleSectorScope();
				rs.setRule(rule);
				rs.setSector(s);
				rule.getSectorScopes().add(rs);
			}
		}
		if (roleIds != null) {
			for (Long rid : new HashSet<>(roleIds)) {
				RoleEntity r = roleEntityRepository.findById(rid).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown role id"));
				RuleRoleScope rr = new RuleRoleScope();
				rr.setRule(rule);
				rr.setRole(r);
				rule.getRoleScopes().add(rr);
			}
		}
	}

	private JsonNode readJson(Object raw) {
		if (raw == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conditions required");
		}
		try {
			return objectMapper.valueToTree(raw);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid conditions json");
		}
	}

	private JsonNode readJsonNullable(Object raw) {
		if (raw == null) {
			return null;
		}
		return objectMapper.valueToTree(raw);
	}

	private void publishUpdated(long ruleId) {
		Rule r = ruleRepository.findByIdWithFeatures(ruleId).orElse(null);
		if (r == null) {
			return;
		}
		List<String> keys = new ArrayList<>(r.getFeatures().stream().map(FeatureDefinition::getFeatureKey).toList());
		eventPublisher.publishEvent(new RuleUpdatedEvent(keys, ruleId));
	}

	public record RuleWriteRequest(
			String ruleName,
			boolean defaultRule,
			HierarchyLevel hierarchyLevel,
			RuleType ruleType,
			int priority,
			boolean active,
			List<Long> featureDefinitionIds,
			List<Long> tenantIds,
			List<Long> sectorIds,
			List<Long> roleIds,
			Object conditions,
			Object variantValue,
			String createdBy
	) {}
}
