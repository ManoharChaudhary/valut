package com.vault.api.decisions;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vault.engine.Decision;
import com.vault.engine.DecisionEngineService;
import com.vault.engine.DecisionTrace;
import com.vault.engine.DecisionTraceSummary;
import com.vault.engine.EngineResult;

@ExtendWith(MockitoExtension.class)
class DecisionControllerTest {
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private DecisionEngineService decisionEngineService;

	@InjectMocks
	private DecisionController decisionController;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(decisionController).build();
	}

	@Test
	void evaluateReturnsDecisionAndTrace() throws Exception {
		when(decisionEngineService.evaluate(anyString(), anyMap()))
				.thenReturn(new EngineResult(
						Decision.ALLOW,
						List.of("ok"),
						new DecisionTrace(List.of()),
						DecisionTraceSummary.preEvaluation("stub summary")
				));

		var body = Map.of(
				"featureKey", "test.feature",
				"context", Map.of("tenant_id", "t1")
		);

		mockMvc.perform(post("/api/v1/decisions/evaluate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.decision").value("ALLOW"))
				.andExpect(jsonPath("$.summary").value("stub summary"))
				.andExpect(jsonPath("$.reasons[0]").value("ok"));
	}

	@Test
	void evaluateGetReturnsMethodNotAllowed() throws Exception {
		mockMvc.perform(get("/api/v1/decisions/evaluate"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(header().string("Allow", "POST"));
	}

	@Test
	void evaluateRejectsBlankFeatureKey() throws Exception {
		var body = Map.of("featureKey", "  ", "context", Map.of());
		mockMvc.perform(post("/api/v1/decisions/evaluate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}
}
