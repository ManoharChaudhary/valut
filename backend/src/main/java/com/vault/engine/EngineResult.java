package com.vault.engine;

import java.util.List;

public record EngineResult(Decision decision, List<String> reasons, DecisionTrace trace) {}

