# Backend foundations & request flow (for frontend engineers)

This guide explains how a typical **HTTP request** moves through a Spring Boot app like Vault, what each layer is for, and how it maps to concepts you already know from React/Next.js. The goal is to make you comfortable reading stack traces, logs, and “where does this code run?” questions—without memorizing every annotation.

**Backend status:** Phases **1–4** are implemented (data model, engine, public evaluate API, caching, and event-driven cache eviction). Use this document as the **single end-to-end review** before moving to **Phase 5** (frontend). The [mentoring README](README.md) has a short delivery summary table.

---

## 1. The one-sentence story

When the frontend calls `POST /api/v1/decisions/evaluate`, **Tomcat** receives bytes on a socket, **Spring MVC** picks the right controller method, **validation** checks the JSON shape, a **cache proxy** may return a prior result, otherwise your **service** runs business logic (schema → rules → trace + summary), **repositories** talk to Postgres via **JPA/Hibernate**, and the response is serialized back to JSON. When rules change via **`RuleAdminService`**, an **event** clears the decision cache so results stay fresh.

Think of it as: **HTTP transport → router → handler → (optional cache) → domain logic → data access → response**, plus **mutation → event → cache invalidation**.

---

## 2. Big-picture architecture (Vault as built — Phases 1–4)

This diagram is the **review map**: read it once top-to-bottom, then walk the sequence in section 3.

```mermaid
flowchart TB
  subgraph client [Client]
    FE[Frontend_or_API_Client]
  end

  subgraph http [HTTP_entry]
    TC[Tomcat]
    DS[DispatcherServlet]
  end

  subgraph mvc [Spring_Web_MVC]
    DC[DecisionController]
    BV[Bean_Validation_DTO]
  end

  subgraph cache_layer [Spring_Cache_Caffeine]
    PROXY[Cache_proxy_around_DecisionEngineService]
    CAF[(Cache_decisions)]
  end

  subgraph engine [Decision_pipeline]
    DES[DecisionEngineService]
    CSV[ContextSchemaValidator]
    FDR[FeatureDefinitionRepository]
    RR[RuleRepository]
    RVR[RuleVersionRepository]
    STR[RuleEvaluatorStrategies]
    RES[DecisionResolver_and_DecisionTraceSummary]
  end

  subgraph mutations [Rule_writes_Phase4]
    RAS[RuleAdminService]
    PUB[ApplicationEventPublisher]
    LST[RuleCacheEvictionListener]
  end

  subgraph data [Persistence]
    JPA[JPA_Hibernate]
    PG[(PostgreSQL)]
  end

  FE -->|POST_JSON| TC
  TC --> DS
  DS --> DC
  DC --> BV
  DC --> PROXY
  PROXY -->|cache_miss| DES
  PROXY <-->|read_write| CAF
  DES --> CSV
  DES --> FDR
  DES --> RR
  DES --> RVR
  DES --> STR
  DES --> RES
  FDR --> JPA
  RR --> JPA
  RVR --> JPA
  JPA --> PG
  RAS --> RVR
  RAS --> PUB
  PUB --> LST
  LST -->|clear| CAF
```

**Read path (evaluate):** Client → MVC → **`@Cacheable` proxy** → on miss, `DecisionEngineService` runs schema check, loads rules/versions, runs strategies, resolves DENY/ALLOW, builds trace + summary → result stored in Caffeine until TTL.

**Write path (rule append):** `RuleAdminService` persists a new `RuleVersion` → publishes **`RuleUpdatedEvent`** → listener **`clear()`** on `decisions` cache so the next evaluate cannot return a stale decision (MVP: full cache clear).

**Frontend analogy:** Tomcat/DispatcherServlet ≈ **Next.js server runtime**. Controller ≈ **route handler**. Cache proxy ≈ **memoized fetch** for identical `(featureKey, context)`. `RuleAdminService` + events ≈ **mutating server state** then **invalidating** related query caches (React Query `invalidateQueries`).

---

## 3. Sequence: `POST /api/v1/decisions/evaluate`

This is the **happy path** through the layers we built. Numbers are chronological.

```mermaid
sequenceDiagram
  participant Browser as Client
  participant Tomcat as Tomcat
  participant MVC as DispatcherServlet
  participant Ctrl as DecisionController
  participant HV as Bean_Validation
  participant Cache as Cacheable_proxy_Caffeine
  participant Eng as DecisionEngineService
  participant FD as FeatureDefinitionRepository
  participant Val as ContextSchemaValidator
  participant RR as RuleRepository
  participant RVR as RuleVersionRepository
  participant PG as PostgreSQL

  Browser->>Tomcat: POST /api/v1/decisions/evaluate JSON body
  Tomcat->>MVC: dispatch to Spring MVC
  MVC->>Ctrl: map path + method to evaluate(...)
  Ctrl->>HV: validate EvaluateRequest (e.g. featureKey not blank)
  alt validation fails
    HV-->>Browser: 400 Bad Request
  end
  Ctrl->>Cache: evaluate(featureKey, context)
  alt cache_hit
    Cache-->>Ctrl: cached EngineResult
  end
  alt cache_miss
    Cache->>Eng: invoke real method
    Eng->>FD: findByFeatureKey(featureKey)
    FD->>PG: SQL SELECT feature_definitions
    PG-->>FD: row or empty
    alt no feature definition
      Eng-->>Cache: EngineResult DENY plus traceSummary
    end
    Eng->>Val: validate(contextSchema, context)
    alt schema invalid or context fails schema
      Eng-->>Cache: EngineResult DENY plus errors
    end
    Eng->>RR: find active rules for feature
    RR->>PG: SQL SELECT rules
    loop each rule
      Eng->>RVR: latest RuleVersion
      RVR->>PG: SQL SELECT rule_versions
      Eng->>Eng: pick strategy, evaluate, append trace
    end
    Eng->>Eng: DecisionResolver plus DecisionTraceSummary
    Eng-->>Cache: EngineResult(decision, reasons, trace, traceSummary)
    Cache-->>Ctrl: same
  end
  Ctrl-->>Browser: 200 JSON EvaluateResponse
```

**What to notice as a learner**

1. **Controller is thin** — it should not embed policy. It translates HTTP ↔ Java objects.
2. **`@Cacheable` sits on the service** — Spring’s proxy intercepts **before** the heavy work; identical `(featureKey, context)` can return without hitting Postgres (until TTL or eviction).
3. **Service owns orchestration** — “first validate schema, then load rules, then evaluate,” plus trace/summary assembly.
4. **Repositories are narrow** — mostly “load/save by id/key,” not business rules.
5. **Database is the source of truth for config** (features, rules, versions), not hard-coded constants.

---

## 3b. Sequence: rule append → event → cache eviction (Phase 4)

When you append a rule version through **`RuleAdminService`** (not by calling `RuleVersionRepository` alone), the system notifies listeners so **evaluate** cannot serve stale cache entries.

```mermaid
sequenceDiagram
  participant Caller as Admin_API_or_job
  participant RAS as RuleAdminService
  participant RVR as RuleVersionRepository
  participant PG as PostgreSQL
  participant PUB as ApplicationEventPublisher
  participant LST as RuleCacheEvictionListener
  participant CM as CacheManager

  Caller->>RAS: appendRuleVersion(ruleId, conditions, ...)
  RAS->>RVR: save(new RuleVersion)
  RVR->>PG: INSERT rule_versions
  RAS->>PUB: publish(RuleUpdatedEvent)
  PUB->>LST: onRuleUpdated(event)
  LST->>CM: getCache(decisions).clear()
```

---

## 4. Terminology cheat sheet (with frontend analogies)

| Backend term | What it is (plain English) | Frontend analogy |
|--------------|-----------------------------|------------------|
| **Servlet container / Tomcat** | HTTP server that runs your Java web app | Node HTTP server that runs Next.js in production |
| **DispatcherServlet** | Spring’s central “router” that finds which controller method matches URL + HTTP method | Next.js router + method dispatch for API routes |
| **Controller (`@RestController`)** | Class whose methods handle HTTP; return values become JSON/XML/etc. | `export async function POST(request)` in App Router |
| **DTO (Data Transfer Object)** | A shape of JSON for requests/responses (`EvaluateRequest`, `EvaluateResponse`) | Zod-inferred type or TypeScript interface for API payloads |
| **Bean Validation (`@Valid`, `@NotBlank`)** | Declarative rules on fields; invalid input → 400 | Zod `safeParse` on the server before running logic |
| **Service (`@Service`)** | Application logic orchestrator; transactional boundaries often start here | A `lib/server/decisions.ts` module called only from server |
| **Repository (`JpaRepository`)** | Interface for CRUD + query methods; Spring generates implementation | Prisma `db.featureDefinition.findUnique(...)` |
| **Entity (`@Entity`)** | Java class mapped to a **table row** | Prisma model / DB row type (not the same as a DTO) |
| **JPA / Hibernate** | ORM: maps entities ↔ SQL, tracks changes, generates queries | Prisma/Drizzle/TypeORM (conceptually) |
| **Flyway migration** | Versioned SQL files that evolve schema (`V1__...sql`) | Prisma migrations / Drizzle migrations |
| **JSONB column** | Postgres column storing JSON with binary indexing options | JSON column in Postgres; flexible schema for rules/schemas |
| **`contextSchema` (`FeatureDefinition`)** | JSON Schema document stored in DB describing allowed evaluation `context` | A shared Zod schema stored in CMS/DB that both UI and API enforce |
| **`ContextSchemaValidator`** | Validates runtime `context` map against that schema **before** rules run | `contextSchema.safeParse(body)` before feature flags logic |
| **Dependency Injection (DI)** | Spring constructs objects and injects collaborators via constructors | Less like Context; more like a framework **wiring** every import graph once at startup |
| **`List<RuleEvaluatorStrategy>` injection** | Spring finds all `@Component` strategies and injects as a list | Plugin array: `[booleanEvaluator, rolloutEvaluator, ...]` |
| **`@Cacheable` + Caffeine** | Method result memoized in-process by a generated proxy; key from `KeyGenerator` | `useMemo` / deduped server fetch keyed by stable hash |
| **`KeyGenerator` / deterministic key** | Stable cache key from `featureKey` + canonical JSON context | Stable React Query `queryKey` |
| **`ApplicationEventPublisher` / `@EventListener`** | In-process pub/sub after domain changes | Emitting a custom event bus message after mutation |
| **`RuleUpdatedEvent` + listener** | Rule writes trigger cache eviction (and future audit hooks) | `invalidateQueries` after a successful mutation |

---

## 5. MVC in Spring (what “MVC” means here)

Classic MVC: Model–View–Controller.

- **Controller**: handles HTTP.
- **Model**: often your domain objects / DTOs / entities (the word is overloaded).
- **View**: in a REST API, the “view” is usually **JSON** (not HTML templates).

In Vault, **`DecisionController` is the C**. The **JSON response** is the V. The **M** is spread across DTOs + entities + service results.

**Frontend analogy:** It’s closer to **JSON API + server modules** than to React “components.” Don’t look for a literal `.jsx` view—think “response serialization.”

---

## 6. JPA & repositories (mental model)

### Entity

An `@Entity` class (e.g. `FeatureDefinition`) maps to a table. Fields map to columns. Some fields are enums (`@Enumerated`) or JSON (`@JdbcTypeCode(JSON)` + `jsonb`).

### Repository

`interface FeatureDefinitionRepository extends JpaRepository<FeatureDefinition, Long>` gives you:

- `save`, `findById`, `delete`, …
- derived query methods like `findByFeatureKey(...)` from the method name.

Spring Data JPA implements the interface at runtime (JDK proxy / generated implementation).

### What Hibernate does at runtime

When you call `findByFeatureKey`, Hibernate generates SQL, runs it, hydrates an entity object graph.

**Frontend analogy:** Repository ≈ **thin data access module**. Entity ≈ **DB row representation**. Don’t pass entities directly to the world as API JSON unless you intend to—DTOs keep your HTTP contract stable even if tables change.

---

## 7. `contextSchema` end-to-end (why it exists)

Vault’s product rule: **never evaluate rules on unvalidated context**.

1. Admin/product defines a feature row: `feature_definitions.feature_key` + `context_schema` (JSON Schema in JSONB).
2. Client sends `context` in the evaluate request.
3. `DecisionEngineService` loads the feature definition.
4. `ContextSchemaValidator` validates `context` against `context_schema`.
5. Only if valid do we query rules and run strategies.

**Frontend analogy:** Same as defining a **shared schema** for “evaluation payload” and refusing to run business logic if `parse` fails—except this is **authoritative on the server** (clients can lie; server cannot).

---

## 8. Validation: two different layers (common confusion)

You will see **two validations** in Vault:

1. **HTTP/DTO validation** (`@NotBlank` on `featureKey`): “Is the request structurally acceptable?”
2. **Domain validation** (JSON Schema against `context`): “Does this context match the feature contract?”

They complement each other. DTO validation is cheap and generic. JSON Schema is feature-specific and stored in DB.

**Debugging tip:** If you get **400**, it’s often layer (1). If you get **200** with `decision: DENY` and reasons mentioning schema, it’s layer (2).

---

## 9. How Spring “wires” beans (why constructors work)

Classes annotated with `@Service`, `@RestController`, `@Component` are **beans**. Spring’s DI container:

1. Scans packages under your `@SpringBootApplication` class (`com.vault` by default).
2. Creates singletons (by default) for beans.
3. Injects them via constructor parameters.

**Frontend analogy:** Not exactly React Context. It’s closer to a **composition root** that builds the object graph once: “create `DecisionEngineService` with these repositories and strategies.”

---

## 10. Debugging playbook (when something breaks)

### A. Classify the failure by HTTP status

| Status | Common meaning | Where to look first |
|--------|----------------|---------------------|
| **404** | Route not found, or wrong method (GET vs POST) | Controller mapping, typo in path, browser using GET |
| **400** | Invalid JSON body / bean validation | DTO annotations, request JSON shape |
| **415** | Wrong `Content-Type` | Add `Content-Type: application/json` |
| **500** | Unhandled exception | Server logs stack trace |
| **200 + DENY** | Business rule / fail-closed | Engine reasons, DB data (feature missing, schema mismatch) |

### B. Reproduce with `curl` (eliminates frontend variables)

```bash
curl -v -X POST http://localhost:8080/api/v1/decisions/evaluate \
  -H "Content-Type: application/json" \
  -d '{"featureKey":"demo.feature","context":{"tenant_id":"t-1"}}'
```

`-v` shows status, headers, redirects.

### C. Read logs with intent

Spring Boot logs usually show:

- **Startup**: port, active profile, Flyway migrations, JPA init.
- **Request handling**: exceptions with root cause at the bottom.

**Rule:** scroll to the **Caused by:** chain—fix the deepest meaningful line.

### D. Confirm data, not just code

Many “DENY” outcomes are correct behavior:

- No `feature_definitions` row for `featureKey`
- `context` fails JSON Schema
- No active `rules` for that feature

Use SQL:

```sql
select * from feature_definitions where feature_key = 'demo.feature';
select * from rules where feature_key = 'demo.feature' and active = true;
```

### E. Port conflicts

If logs say **port 8080 in use**, you have another Java process still running.

### F. IDE debugging (local)

Set breakpoints in:

1. `DecisionController.evaluate`
2. `DecisionEngineService.evaluate`
3. `ContextSchemaValidator.validate`

Run `Spring Boot` with **Debug** (not plain Run). Step through line-by-line.

**Frontend analogy:** Same as debugging a Next.js route handler—break at the entry, then step into imported server modules.

### G. Actuator health (is the app alive?)

```bash
curl -s http://localhost:8080/actuator/health
```

---

## 11. “Proficient lead engineer” habits (backend)

- **Separate transport from domain:** controllers skinny; services own workflows.
- **Fail closed for security-ish systems:** default deny, explicit allow.
- **Make decisions explainable:** traces, reasons, audit tables.
- **Schema contracts:** validate inputs early; store schemas in DB when they evolve per feature.
- **Observability mindset:** logs + metrics + health; reproduce with curl.
- **Schema migrations:** never “just change prod DB”; Flyway version history is part of the story.

---

## 12. How this doc ties to Vault’s phase notes

- **Phase 1:** entities + Flyway + Postgres JSONB (`FeatureDefinition.contextSchema`). See [phase-01-foundation.md](phase-01-foundation.md).
- **Phase 2:** engine strategies + hierarchy + deny-wins resolver + trace entries. See [phase-02-core-engine.md](phase-02-core-engine.md).
- **Phase 3:** JSON Schema pre-check + `POST /evaluate` + response trace/summary fields. See [phase-03-schema-validation-and-api.md](phase-03-schema-validation-and-api.md).
- **Phase 4:** Caffeine cache + deterministic keys + `RuleUpdatedEvent` eviction. See [phase-04-caching-and-events.md](phase-04-caching-and-events.md).
- **Phase 5 (next):** Next.js control plane — see [phase-05-frontend-control-plane.md](phase-05-frontend-control-plane.md).

---

## 13. Backend as-built — review checklist (entire flow)

Use this table when you **code-review** or explain Vault to someone else. Package names are under `backend/src/main/java/com/vault/`.

| Step | Responsibility | Main types |
|------|----------------|------------|
| 1 | HTTP + JSON binding | `api.decisions.DecisionController`, `EvaluateRequest`, `EvaluateResponse` |
| 2 | DTO validation | Bean Validation on `EvaluateRequest` |
| 3 | Cache lookup | `@Cacheable` on `engine.DecisionEngineService.evaluate` |
| 4 | Feature contract | `features.FeatureDefinition` + `FeatureDefinitionRepository` |
| 5 | Context JSON Schema | `validation.ContextSchemaValidator` |
| 6 | Rule load + order | `rules.RuleRepository`, `RuleVersionRepository` |
| 7 | Per-type evaluation | `engine.*RuleEvaluator`, `RuleEvaluatorStrategy` |
| 8 | Merge + explain | `DecisionResolver`, `DecisionTraceSummary`, `EngineResult` |
| 9 | Rule append + freshness | `rules.RuleAdminService`, `RuleUpdatedEvent`, `cache.RuleCacheEvictionListener` |
| 10 | Schema migrations | `resources/db/migration/V*.sql` (Flyway) |

**Default-deny reminder:** missing feature, failed schema, no rules, or internal gaps → **DENY** with reasons in the JSON body (not always HTTP 4xx).

---

## 14. Glossary (quick)

- **ORM:** maps objects ↔ relational tables; generates SQL.
- **Migration:** versioned DDL changes applied in order (Flyway).
- **DTO:** API contract shape; decouples HTTP from DB entities.
- **Entity:** persisted domain object mapped to a table.
- **Repository:** persistence access abstraction.
- **Service:** orchestrates use-cases (“evaluate decision”).
- **Strategy pattern:** pluggable rule evaluators without editing the orchestrator.
- **Bean:** object managed by Spring’s container.
- **Profile (`local`)**: configuration variant for dev machines (`application-local.yml`).

**What’s next for the product:** implement **Phase 5** (Next.js simulator + CORS/proxy) — see [phase-05-frontend-control-plane.md](phase-05-frontend-control-plane.md).

**Optional follow-up docs** (add when you need them): **`@Transactional` boundaries**, **lazy loading pitfalls**, **integration vs slice tests**, **production hardening** (auth, rate limits, metrics on cache hit rate).
