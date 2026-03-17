# Phase 3 — Schema Validation & API (Weeks 5–6)

## Task 3.1 — Pre-evaluation context validator (done)

### What we added
- NetworkNT JSON Schema validator dependency in `backend/pom.xml`
- Validator classes:
  - `backend/src/main/java/com/vault/validation/ContextSchemaValidator.java`
  - `backend/src/main/java/com/vault/validation/ContextValidationResult.java`
- Engine wiring:
  - `backend/src/main/java/com/vault/engine/DecisionEngineService.java`
  - now loads `FeatureDefinition` by `featureKey`
  - validates incoming context against `FeatureDefinition.contextSchema`
  - **fails closed** (returns DENY) if feature definition missing, schema missing/invalid, or context violates schema

### Why this matters
Without this guard, rule evaluation might run with malformed context and produce misleading results.
This validator creates a strict contract between product configuration (`contextSchema`) and runtime requests (`context`).

Frontend analogy: this is similar to validating request payload shape with Zod/Yup before executing business logic, except done server-side as a hard safety boundary.

### Important implementation note
We intentionally used NetworkNT `1.5.9` right now because newer `3.x` changed APIs/packages significantly.
`1.5.9` is stable with our current Jackson/Spring setup and keeps implementation straightforward.
// TODO: revisit 3.x migration when we harden the validation module.

### Tests added
- `backend/src/test/java/com/vault/validation/ContextSchemaValidatorTest.java`
  - valid context passes
  - invalid context fails with errors

