# Vault — backend handoff prompt (paste into a new chat for frontend / full-stack work)

You are working in the **Vault** monorepo: a feature-flag / rules evaluation service with a **Spring Boot** backend and a **Next.js** frontend under `frontend/`.

## Backend stack (implemented)

- **Java 21**, **Spring Boot 4**, **Maven** — project root: `backend/`
- **PostgreSQL** + **Flyway** migrations: `V1`–`V4` baseline; **V5** multi-feature rules (`rule_features`, scopes, `public_id` on tenants/features, sectors/roles tables); **V6** seed catalogs + core features; **V7** global default boolean rule linked to all seeded features
- **JPA** entities and repositories for tenants, sectors, roles, features, rules (many-to-many features), rule versions, scope link rows
- **Caffeine** caching (`spring.cache.*`), `@Cacheable` on `DecisionEngineService.evaluate(FeatureDefinition, Map)`, `DecisionEvaluationKeyGenerator` keys off resolved feature + canonical JSON context
- **Domain events**: `RuleUpdatedEvent(List<String> affectedFeatureKeys, Long ruleId)` after rule version append and rule CRUD; `RuleCacheEvictionListener` clears the `decisions` cache (MVP: full clear)
- **JSON Schema** validation of evaluation `context` via **NetworkNT json-schema-validator** (`ContextSchemaValidator`) — invalid context → **DENY** before any rule runs
- **Rule evaluators** (strategy pattern): `BooleanRuleEvaluator`, `RolloutRuleEvaluator` using **deterministic MurmurHash3**; `RuleEvaluatorStrategy.evaluate(..., String evaluationFeatureKey)` for rollout hashing
- **`RuleScopeMatcher`**: tenant / sector / role scope match (empty scope set on an axis = wildcard)
- **Decision policy**: **default DENY**; **DENY wins** over ALLOW across evaluated trace (`DecisionResolver`); scoped rules evaluated first, then **single global default** rule if no scoped rule matches and default is linked to the feature via `rule_features`
- **Primary `ObjectMapper` bean**: `com.vault.config.JacksonConfig` (Spring Boot 4 does not always expose one for injection)

## HTTP API — decisions

- **POST** `/api/v1/decisions/evaluate`  
  - `Content-Type: application/json`  
  - Body: `{ "featureId": "<uuid>" }` **or** `{ "featureKey": "<non-blank string>" }` — exactly one locator required; **`context`** object (may be `{}`); keys such as **`tenant_id`**, **`sector`**, **`role_id`** / **`role_name`** must satisfy the feature’s `context_schema`.
- **GET** `/api/v1/decisions/evaluate` → **405** with `Allow: POST` (intentional guardrail).

**Response JSON** (`EvaluateResponse`): `decision` (`ALLOW` | `DENY`), `summary`, `reasons`, `evaluationPath`, `matchedRuleId`, `matchedRuleVersionId`, `trace` (array of `{ hierarchyLevel, ruleType, ruleId, ruleVersionId, decision, reason }`).

## HTTP API — admin (MVP, no auth)

Base path: **`/api/v1/admin/`** — **tenants**, **sectors**, **roles**, **features**, **rules** CRUD; **`POST .../rules/{id}/versions`** to append a rule version (`RuleAdminService`). Intended **internal-only** until auth is added.

## Local backend

- Default port **8080** (`application.yml`). Run from `backend/`: `./mvnw spring-boot:run` (or `mvn`).
- DB: configure via `application-local.yml` / env as documented in `docs/mentoring/how-to-run-and-verify.md`.
- Actuator: health, info, metrics exposed.

## Frontend integration expectations

- Dev **same-origin proxy**: Next.js rewrites **`/vault-api/*`** → `BACKEND_URL` (see `frontend/next.config.ts` and `frontend/.env.example`).
- **Simulator** uses **`featureId`** (feature `public_id` UUID) plus scope fields; **admin** pages call **`/vault-api/api/v1/admin/...`**.

## Product rules (non-negotiable)

- **Default deny** if feature missing, schema invalid, or no matching allow path after resolver.
- **Deterministic** rollout bucketing (hash-based).
- **Validate** evaluation context against the feature’s schema before evaluating rules.

Use `docs/mentoring/` for deeper diagrams and phase notes; `execution-plan.md` for the full enterprise delivery checklist.
