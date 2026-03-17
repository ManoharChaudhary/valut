package com.vault.validation;

import java.util.List;

public record ContextValidationResult(boolean valid, List<String> errors) {
	public static ContextValidationResult ok() {
		return new ContextValidationResult(true, List.of());
	}

	public static ContextValidationResult invalid(List<String> errors) {
		return new ContextValidationResult(false, errors);
	}
}

