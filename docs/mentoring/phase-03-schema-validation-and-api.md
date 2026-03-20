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

---

## Task 3.2 — Decision REST endpoint (done)

### What we added
- `backend/src/main/java/com/vault/api/decisions/DecisionController.java`
  - `POST /api/v1/decisions/evaluate`
- Request / response DTOs:
  - `EvaluateRequest` — `featureKey` (required), `context` (optional object, defaults to empty)
  - `EvaluateResponse` — `decision` (ALLOW|DENY), `reasons`, `trace` (list of step entries)
- `backend/src/test/java/com/vault/api/decisions/DecisionControllerTest.java`
  - standalone `MockMvc` + Mockito (no full Spring context; avoids Boot 4 slice package moves)

### Why controller is thin
`@RestController` only maps HTTP to the engine service and shapes JSON.
Business rules stay in `DecisionEngineService` (validation → evaluate → trace).

### Verify with curl (local profile, port 8080)
Requires rows in `feature_definitions` and matching rules for a meaningful ALLOW; otherwise you still get a structured DENY.

**Use POST.** A browser address bar issues **GET**, which does not hit the evaluate handler (you may see **404** or **405** depending on Spring version; we map GET to **405** with `Allow: POST` for clarity).

```bash
curl -s -X POST http://localhost:8080/api/v1/decisions/evaluate \
  -H "Content-Type: application/json" \
  -d '{"featureKey":"your.feature","context":{"tenant_id":"t-1"}}'
```

Invalid body (blank `featureKey`) returns **400** from Bean Validation.

