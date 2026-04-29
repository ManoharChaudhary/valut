Let's build **Vault – Enterprise Decision & Entitlement Engine**. I am an expert in Frontend but new to Spring Boot and enterprise backend patterns. I need you to guide me phase-by-phase, explaining the backend concepts clearly and telling me how to verify the work locally.

I also need **git history** to look like organic work over roughly **2–3 months**. For each completed slice, use a realistic **backdated** commit, for example:

`GIT_COMMITTER_DATE="2026-04-15 14:00:00" git commit --date="2026-04-15 14:00:00" -m "feat: …"`

Space commits **1–3 per week**, **weekdays only**, unless you intentionally want a weekend spike.

---

## Baseline (Phases 1–5) — **delivered in repo**

Original mentoring track; treat as **done** unless we explicitly reopen something.

- [x] **Phase 1 — Foundation & data modeling:** Spring Boot, JPA, `Tenant`, `FeatureDefinition` (+ `context_schema` JSONB), `Rule`, `RuleVersion`.
- [x] **Phase 2 — Core engine:** Strategy evaluators (`Boolean`, `Rollout` + deterministic hash), `DecisionEngineService`, hierarchy ordering, **default deny** if nothing allows.
- [x] **Phase 3 — Schema & public API:** `ContextSchemaValidator`, `POST /api/v1/decisions/evaluate`, `DecisionTrace` / summary in JSON.
- [x] **Phase 4 — Performance:** Caffeine + `@Cacheable`, domain events + cache eviction on rule writes.
- [x] **Phase 5 — Frontend MVP:** Next.js **App Router** (repo: **Next 15**), TanStack Query, `/vault-api` rewrite, decision simulator.

---

## Locked decisions (enterprise upgrade) — **as implemented**

| Topic | Decision |
|--------|-----------|
| **Identifiers** | **`public_id` (UUID)** on tenants, features, sectors, roles; **`bigserial`** PKs retained. |
| **Multi-feature rules** | **`rule_features`** M:N between `rules` and `feature_definitions`; legacy `rules.feature_key` removed after backfill. |
| **Sectors / roles** | Tables **`sectors`** (code), **`roles`** (name); scopes link to numeric FKs; evaluation context uses **`tenant_id`** (tenant UUID string or legacy id match), **`sector`** (code), **`role_id`** (role UUID) and optional **`role_name`**. |
| **Scopes** | **`rule_tenant_scopes`**, **`rule_sector_scopes`**, **`rule_role_scopes`**; **empty lists = wildcard** on that axis. |
| **Global default rule** | **Exactly one** row with **`is_default = true`** in **`rules`** (partial unique index). Linked to features via **`rule_features`**. If no non-default rule **matches** scopes for the request, engine evaluates that default **only when** it is linked to the resolved feature; else **DENY** if no scoped match. |
| **Conflict policy** | **`DecisionResolver`**: **DENY wins** across evaluated trace entries (unchanged). |
| **Evaluate API** | Request body supports **`featureId`** (UUID, `feature_definitions.public_id`) **or** **`featureKey`**; **`context`** still validated per feature JSON Schema. |
| **API vocabulary** | **`ALLOW` / `DENY`** in JSON; UI may label **Permit / Deny**. |
| **Admin security** | **MVP: no auth** on `/api/v1/admin/**` (internal-only). |
| **Frontend** | **Slate-950** shell, primary **`#3B82F6`**, **emerald / rose** for permit/deny; modular **`RuleCard`**, **`DecisionBreadcrumbs`**; admin list pages + simulator with **`featureId`**. |

---

## Phase 6 — PostgreSQL expansion & seeds — **delivered**

- [x] **Task 6.1–6.5:** Flyway **V5** — catalogs, UUID columns, `rule_features`, `rule_name`, `is_default`, scope tables, migrate `feature_key` → junction; **V6** seeds sectors, roles, tenants, three core features with **`context_schema` requiring `tenant_id`, `sector`, `role_id`**; **V7** optional global default boolean rule + version linked to all features.

**Verify:** `./mvnw flyway:migrate` (with Postgres); `SELECT` counts on new tables.

---

## Phase 7 — Scoped matching + default fallback — **delivered**

- [x] **`RuleScopeMatcher`**, **`DecisionEngineService`** partition scoped vs default, filter scoped by context, fallback to global default rule with feature link, **`RuleEvaluatorStrategy`** extended with **`evaluationFeatureKey`** for rollout hashing.
- [x] **`DecisionEvaluationKeyGenerator`** resolves **`FeatureDefinition`** for cache key.
- [x] **`RuleUpdatedEvent`** carries **`List<String> affectedFeatureKeys`**.
- [x] **`// TODO: Add audit logging for rule changes`** on **`RuleManagementService`** mutations.

**Verify:** `./mvnw test`; manual `curl` with **`featureId`** + seeded UUIDs for tenant/role.

---

## Phase 8 — Admin HTTP API — **delivered**

- [x] **`/api/v1/admin/tenants`**, **`/sectors`**, **`/roles`**, **`/features`**, **`/rules`** CRUD (JSON); **`POST /api/v1/admin/rules/{id}/versions`** appends versions via **`RuleAdminService`**; **`RuleManagementService`** clears all default flags before promoting a new default.

**Verify:** `curl` CRUD smoke; second concurrent default prevented by DB + service clear.

---

## Phase 9–11 — Frontend shell, admin lists, simulator UX — **delivered (MVP)**

- [x] **Design system:** `clsx` + `tailwind-merge`, **`cn()`**, reusable **`Button`**, **`Card`**, **`Input`**, **`Label`**, **`Badge`**, **`AppShell`** nav.
- [x] **`/admin/features`**, **`/admin/rules`** list views calling admin APIs.
- [x] **Simulator:** **`featureId`** selector + scope fields + merge JSON; **`DecisionBreadcrumbs`** waterfall readout + improved contrast on slate.

**Verify:** `cd frontend && npm run build`; run backend + `npm run dev`, exercise `/simulator` and admin routes.

---

## Optional follow-ups (not required for MVP)

- [ ] Full **rule create/edit form** in UI (currently API-first + list review).
- [ ] **Per-tenant** audit log implementation (replace TODO).
- [ ] **Feature-scoped** cache eviction instead of full **`decisions`** clear.

---

## What to say next

For **new** mentoring slices (docs-only, tests, or follow-ups), say **"Next"** and name the optional follow-up line you want to tackle.
