package com.vault.api.decisions;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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

	@PostMapping(path = "/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EvaluateResponse evaluate(@Valid @RequestBody EvaluateRequest request) {
		var result = decisionEngineService.evaluate(request.featureKey(), request.context());
		return EvaluateResponse.from(result);
	}
}
