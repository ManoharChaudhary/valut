package com.vault.api.decisions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.vault.engine.DecisionEngineService;
import com.vault.features.FeatureDefinition;
import com.vault.features.FeatureDefinitionRepository;

@RestController
@RequestMapping("/api/v1/decisions")
@Validated
public class DecisionController {
	private final DecisionEngineService decisionEngineService;
	private final FeatureDefinitionRepository featureDefinitionRepository;

	public DecisionController(
			DecisionEngineService decisionEngineService,
			FeatureDefinitionRepository featureDefinitionRepository
	) {
		this.decisionEngineService = decisionEngineService;
		this.featureDefinitionRepository = featureDefinitionRepository;
	}

	/**
	 * GET here would otherwise look like a missing route (404). Evaluation is POST-only.
	 */
	@GetMapping("/evaluate")
	public ResponseEntity<Void> evaluateMethodNotAllowed() {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
				.header(HttpHeaders.ALLOW, HttpMethod.POST.name())
				.build();
	}

	@PostMapping(path = "/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EvaluateResponse evaluate(@RequestBody EvaluateRequest request) {
		if (!request.hasLocator()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "featureId or featureKey is required");
		}
		FeatureDefinition feature = resolveFeature(request);
		var result = decisionEngineService.evaluate(feature, request.context());
		return EvaluateResponse.from(result);
	}

	private FeatureDefinition resolveFeature(EvaluateRequest request) {
		if (request.featureId() != null) {
			return featureDefinitionRepository.findByPublicId(request.featureId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "feature not found for featureId"));
		}
		String key = request.featureKey().trim();
		return featureDefinitionRepository.findByFeatureKey(key)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "feature not found for featureKey"));
	}
}
