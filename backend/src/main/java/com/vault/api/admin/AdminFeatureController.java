package com.vault.api.admin;

import java.util.List;
import java.util.UUID;

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
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vault.features.FeatureDefinition;
import com.vault.features.FeatureDefinitionRepository;

@RestController
@RequestMapping("/api/v1/admin/features")
public class AdminFeatureController {
	private final FeatureDefinitionRepository featureDefinitionRepository;
	private final ObjectMapper objectMapper;

	public AdminFeatureController(FeatureDefinitionRepository featureDefinitionRepository, ObjectMapper objectMapper) {
		this.featureDefinitionRepository = featureDefinitionRepository;
		this.objectMapper = objectMapper;
	}

	@GetMapping
	public List<FeatureDefinitionResponse> list() {
		return featureDefinitionRepository.findAll().stream().map(FeatureDefinitionResponse::from).toList();
	}

	@GetMapping("/{id}")
	public FeatureDefinitionResponse get(@PathVariable long id) {
		return featureDefinitionRepository.findById(id)
				.map(FeatureDefinitionResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FeatureDefinitionResponse create(@RequestBody FeatureWriteRequest body) {
		if (body.featureKey() == null || body.featureKey().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "featureKey required");
		}
		if (featureDefinitionRepository.findByFeatureKey(body.featureKey().trim()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "featureKey already exists");
		}
		FeatureDefinition f = new FeatureDefinition();
		f.setFeatureKey(body.featureKey().trim());
		f.setDisplayName(body.displayName());
		f.setContextSchema(toJson(body.contextSchema()));
		FeatureDefinition saved = featureDefinitionRepository.save(f);
		return FeatureDefinitionResponse.from(saved);
	}

	@PutMapping("/{id}")
	public FeatureDefinitionResponse update(@PathVariable long id, @RequestBody FeatureWriteRequest body) {
		FeatureDefinition f = featureDefinitionRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (body.displayName() != null) {
			f.setDisplayName(body.displayName());
		}
		if (body.contextSchema() != null) {
			f.setContextSchema(toJson(body.contextSchema()));
		}
		return FeatureDefinitionResponse.from(featureDefinitionRepository.save(f));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		if (!featureDefinitionRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		featureDefinitionRepository.deleteById(id);
	}

	private JsonNode toJson(Object raw) {
		if (raw == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contextSchema required");
		}
		return objectMapper.valueToTree(raw);
	}

	public record FeatureWriteRequest(String featureKey, String displayName, Object contextSchema) {}

	public record FeatureDefinitionResponse(
			long id,
			UUID publicId,
			String featureKey,
			String displayName,
			JsonNode contextSchema
	) {
		static FeatureDefinitionResponse from(FeatureDefinition f) {
			return new FeatureDefinitionResponse(
					f.getId(),
					f.getPublicId(),
					f.getFeatureKey(),
					f.getDisplayName(),
					f.getContextSchema()
			);
		}
	}
}
