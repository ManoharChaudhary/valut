package com.vault.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

@Component
public class ContextSchemaValidator {
	private final ObjectMapper objectMapper;
	private final JsonSchemaFactory schemaFactory;

	public ContextSchemaValidator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
	}

	public ContextValidationResult validate(JsonNode contextSchema, Map<String, Object> context) {
		if (contextSchema == null || contextSchema.isNull()) {
			return ContextValidationResult.invalid(List.of("feature context schema is missing"));
		}

		try {
			JsonSchema schema = schemaFactory.getSchema(contextSchema);
			Map<String, Object> safeContext = context == null ? Map.of() : context;
			JsonNode contextNode = objectMapper.valueToTree(safeContext);
			Set<ValidationMessage> errors = schema.validate(contextNode);
			if (errors.isEmpty()) {
				return ContextValidationResult.ok();
			}

			List<String> messages = errors.stream().map(ValidationMessage::getMessage).sorted().toList();
			return ContextValidationResult.invalid(messages);
		} catch (Exception ex) {
			return ContextValidationResult.invalid(List.of("schema validation failed: " + ex.getMessage()));
		}
	}
}

