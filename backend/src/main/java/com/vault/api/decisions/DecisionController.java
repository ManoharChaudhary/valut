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

import com.vault.engine.DecisionEngineService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/decisions")
@Validated
public class DecisionController {
	private final DecisionEngineService decisionEngineService;

	public DecisionController(DecisionEngineService decisionEngineService) {
		this.decisionEngineService = decisionEngineService;
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
	public EvaluateResponse evaluate(@Valid @RequestBody EvaluateRequest request) {
		var result = decisionEngineService.evaluate(request.featureKey(), request.context());
		return EvaluateResponse.from(result);
	}
}
