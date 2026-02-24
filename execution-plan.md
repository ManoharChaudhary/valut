Let's build "Vault – Enterprise Decision & Entitlement Engine". I am an expert in Frontend but new to Spring Boot and enterprise backend patterns. I need you to guide me phase-by-phase, explaining the backend concepts clearly and telling me how to verify the work locally.

Also, I need the git history to look like I built this organically over the last 10 weeks (roughly 2-3 months). For every step we complete, provide the exact `git commit` command with a realistic backdated timestamp, starting from roughly 10 weeks ago.

Here is our Agile Execution Plan. Let's start with **Phase 1, Task 1.1**. Do not move to the next task until I say "Next".

### Phase 1: Foundation & Data Modeling (Simulated Time: Weeks 1-2)
- [ ] **Task 1.1:** Initialize the Spring Boot project (Web, JPA, PostgreSQL, Validation). Explain the project structure to me.
- [ ] **Task 1.2:** Create the `Tenant` and `FeatureDefinition` entities. The `FeatureDefinition` must include a `contextSchema` field (JSONB) to enforce what context keys are required for this feature.
- [ ] **Task 1.3:** Create the `Rule` and `RuleVersion` entities. Ensure `RuleVersion` maps conditions to Postgres JSONB. Explain how Spring Data JPA works here.

### Phase 2: The Core Rule Engine (Simulated Time: Weeks 3-4)
- [ ] **Task 2.1:** Implement the Strategy Pattern for rule evaluation. Create the interface and the `BooleanRuleEvaluator`. Explain how Spring manages these beans.
- [ ] **Task 2.2:** Implement the `RolloutRuleEvaluator`. Use a deterministic hashing strategy (like MurmurHash3) on `feature_key + context.id` so rollouts are sticky.
- [ ] **Task 2.3:** Implement the core `DecisionEngineService` that orchestrates the evaluation hierarchy (Global -> Sector -> Tenant). Implement the explicit fallback: if no rules match, return DENY.

### Phase 3: Schema Validation & The API (Simulated Time: Weeks 5-6)
- [ ] **Task 3.1:** Implement the pre-evaluation Context Validator. Before the engine runs, it must check the incoming context against the `FeatureDefinition.contextSchema`.
- [ ] **Task 3.2:** Build the `DecisionController` with the `POST /api/v1/decisions/evaluate` endpoint. 
- [ ] **Task 3.3:** Implement the `DecisionTrace` object so the API response shows exactly which rules passed/failed and why. Provide `curl` commands so I can test this.

### Phase 4: Performance & Caching (Simulated Time: Weeks 7-8)
- [ ] **Task 4.1:** Integrate Caffeine Cache for the evaluation endpoint. Explain how `@Cacheable` works and how we generate a unique cache key from the JSON context.
- [ ] **Task 4.2:** Implement Spring Application Events. When a `Rule` is updated, publish an event that evicts the specific cache entry. Explain how Spring Events decouple the code.

### Phase 5: The Frontend Control Plane (Simulated Time: Weeks 9-10)
- [ ] **Task 5.1:** Set up the Next.js (App Router) project in a `/frontend` folder. 
- [ ] **Task 5.2:** Build the "Rule Simulator" UI page where a user can enter a JSON context, hit the Spring Boot API, and see a visual timeline of the `DecisionTrace`. (I will take the lead on making this look good, just set up the wiring and React Query).