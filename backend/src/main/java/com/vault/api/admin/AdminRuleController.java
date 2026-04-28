package com.vault.api.admin;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vault.admin.RuleManagementService;
import com.vault.admin.RuleManagementService.RuleWriteRequest;
import com.vault.admin.RuleResponse;
import com.vault.rules.RuleAdminService;

@RestController
@RequestMapping("/api/v1/admin/rules")
public class AdminRuleController {
	private final RuleManagementService ruleManagementService;
	private final RuleAdminService ruleAdminService;
	private final ObjectMapper objectMapper;

	public AdminRuleController(
			RuleManagementService ruleManagementService,
			RuleAdminService ruleAdminService,
			ObjectMapper objectMapper
	) {
		this.ruleManagementService = ruleManagementService;
		this.ruleAdminService = ruleAdminService;
		this.objectMapper = objectMapper;
	}

	@GetMapping
	public java.util.List<RuleResponse> list() {
		return ruleManagementService.listRuleResponses();
	}

	@GetMapping("/{id}")
	public RuleResponse get(@PathVariable long id) {
		return ruleManagementService.getRuleResponse(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RuleResponse create(@RequestBody RuleWriteRequest request) {
		return ruleManagementService.createRule(request);
	}

	@PutMapping("/{id}")
	public RuleResponse update(@PathVariable long id, @RequestBody RuleWriteRequest request) {
		return ruleManagementService.updateRule(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		ruleManagementService.deleteRule(id);
	}

	@PostMapping("/{id}/versions")
	@ResponseStatus(HttpStatus.CREATED)
	public Map<String, Long> appendVersion(@PathVariable long id, @RequestBody Map<String, Object> body) {
		Object cond = body.get("conditions");
		if (cond == null) {
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "conditions required");
		}
		JsonNode conditions = objectMapper.valueToTree(cond);
		JsonNode variant = body.containsKey("variantValue") ? objectMapper.valueToTree(body.get("variantValue")) : null;
		String createdBy = body.get("createdBy") instanceof String s ? s : "admin";
		var saved = ruleAdminService.appendRuleVersion(id, conditions, variant, createdBy);
		return Map.of("id", saved.getId());
	}
}
