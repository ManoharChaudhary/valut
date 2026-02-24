1. Requirements & Design Document (RDD)
Business Objective
To provide a centralized, highly performant, and fully auditable decision engine that evaluates access rights, feature rollouts, and entitlements across multiple tenants and organizational hierarchies.

Non-Functional Requirements (NFRs)
Latency: Core evaluation API must respond in < 50ms (P95) via aggressive caching.

Auditability: 100% of rule mutations must be versioned and appended. Decisions must yield a fully traversable trace.

Extensibility: New rule types (e.g., Geo-fencing) must be addable without altering core engine logic (Open/Closed Principle).

Availability: Rule evaluation must degrade gracefully if the database is unreachable (rely on L1/L2 caches).

Data Model (PostgreSQL)
tenants: id, name, status, created_at.

rules: id, feature_key, hierarchy_level (GLOBAL, SECTOR, TENANT, PLAN, ROLE), rule_type (BOOLEAN, VARIANT, ROLLOUT, TIME), priority, active.

rule_versions: id, rule_id, conditions (JSONB), variant_value (JSONB), created_by, created_at. (Append-only).

2. Architecture Flow
The Decision Evaluation Flow:

Client Request: A service requests evaluation via POST /api/v1/decisions/evaluate.

Cache Check (L1/L2): The system generates a cache key based on a deterministic hash of the feature_key + context payload.

Hit: Return cached decision immediately.

Miss: Proceed to DB/Memory rule retrieval.

Rule Retrieval: Fetch all active rules for the requested feature_key.

Hierarchy Resolution: Group rules by hierarchy level.

Engine Evaluation (Strategy Pattern):

Evaluate from top (GLOBAL) to bottom (USER ROLE).

Compile/Execute JSONB conditions (e.g., using SpEL).

If a rule matches, record it in the evaluation_trace.

Apply conflict resolution (DENY overrides ALLOW, Specific overrides General).

Response Generation: Construct the payload with the final decision, variant data, and the execution trace.

Cache Write: Store the result asynchronously to minimize request latency.

3. Epics & User Stories
Epic 1: Core Engine & Rule Management

Story 1.1: As an Admin, I want to create rules with specific conditions (SpEL/JSON) so that I can control access to features.

AC: Supports Boolean, Variant, and Time-based rule types. Data is stored in Postgres JSONB.

Story 1.2: As an Auditor, I want every rule change to create a new version, so I can track the history of policy changes.

AC: rules table updates trigger an append-only insert to rule_versions.

Epic 2: Evaluation API & Tracing

Story 2.1: As a downstream service, I want to query the /evaluate endpoint with my context to get a decision.

AC: Endpoint accepts JSON payload, applies the Hierarchy Resolution strategy, and returns ALLOW/DENY.

Story 2.2: As a developer debugging an issue, I want the evaluation response to include a decision trace.

AC: Response includes matched_rule_id, evaluation_path, and human-readable reason.

Epic 3: Performance & Caching

Story 3.1: As a system architect, I want rule evaluations to be cached using Caffeine, so that identical context requests don't hit the DB.

AC: L1 cache implemented with configurable TTL.

Story 3.2: As a rule manager, I want the cache to invalidate automatically when I update a rule, so that clients get fresh decisions.

AC: Spring Application Events trigger cache eviction on rule mutation.

Epic 4: Frontend Control Plane

Story 4.1: As a product manager, I want a UI to simulate a rule evaluation, so I can test my rules before deploying them.

AC: Next.js page takes manual JSON context input, calls the API, and visualizes the evaluation trace.

4. Cursor Execution Plan (Step-by-Step Prompts)
To use this effectively, copy and paste these phases sequentially into Cursor. Do not give it everything at once; verify each step before moving on.

Phase 1: Foundation (Entities & DB)

Cursor Prompt: "Execute Step 1 & 2 from the master plan. Initialize a Spring Boot project with Web, JPA, PostgreSQL, and Validation. Create the JPA entities for Tenant, Rule, and RuleVersion. Ensure RuleVersion uses a custom Hibernate type or Hypersistence library to map conditions to PostgreSQL JSONB. Create the corresponding Spring Data JPA Repositories."

Phase 2: The Core Engine (Domain Logic)

Cursor Prompt: "Execute Step 3. Implement the RuleEngineService. Create an interface RuleEvaluatorStrategy with a method evaluate(RuleVersion rule, Map<String, Object> context). Implement concrete strategies for BooleanRuleEvaluator, TimeBasedRuleEvaluator, and SpelConditionEvaluator. Implement the hierarchy resolution logic (Global -> Sector -> Tenant) where DENY overrides ALLOW."

Phase 3: The API & Tracing

Cursor Prompt: "Execute Step 4. Create the DecisionController with POST /api/v1/decisions/evaluate. Create the request/response DTOs. Ensure the response object includes an EvaluationTrace object that records which levels passed, which rule was matched, and the final decision."

Phase 4: Caching & Events (Performance)

Cursor Prompt: "Execute Step 5 & 6. Configure a Caffeine cache manager in Spring. Add @Cacheable to the evaluation method using a custom key generator that hashes the feature name and context map. Create a RuleUpdatedEvent. Implement an @EventListener that automatically evicts the relevant cache entries when a rule is modified."

Phase 5: Frontend Simulator (Next.js)

Cursor Prompt: "Execute Steps 7 & 8. In a new Next.js App Router project, create a 'Decision Simulator' page. Build a form to input context variables (tenant_id, sector, etc.). Wire it to the Spring Boot backend using React Query. Display the resulting Decision Trace in a visually appealing timeline/stepper UI."

