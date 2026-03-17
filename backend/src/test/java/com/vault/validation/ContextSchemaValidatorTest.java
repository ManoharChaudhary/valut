package com.vault.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ContextSchemaValidatorTest {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ContextSchemaValidator validator = new ContextSchemaValidator(objectMapper);

	@Test
	void validContextPassesSchema() throws Exception {
		var schema = objectMapper.readTree("""
				{
				  "type": "object",
				  "required": ["tenant_id"],
				  "properties": {
				    "tenant_id": { "type": "string" }
				  }
				}
				""");

		var result = validator.validate(schema, Map.of("tenant_id", "t-123"));
		assertThat(result.valid()).isTrue();
	}

	@Test
	void invalidContextFailsSchema() throws Exception {
		var schema = objectMapper.readTree("""
				{
				  "type": "object",
				  "required": ["tenant_id"],
				  "properties": {
				    "tenant_id": { "type": "string" }
				  }
				}
				""");

		var result = validator.validate(schema, Map.of("tenant_id", 42));
		assertThat(result.valid()).isFalse();
		assertThat(result.errors()).isNotEmpty();
	}
}

