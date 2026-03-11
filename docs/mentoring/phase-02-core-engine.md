# Phase 2 — Core Rule Engine (Weeks 3–4)

## Goal (what “done” means)
By the end of Phase 2 we want:
- A strategy-based evaluation engine where adding a new rule type does **not** require changing the engine core
- Deterministic rollouts (sticky buckets) with **no randomness**
- The core hierarchy resolution (GLOBAL → … → ROLE) and conflict handling (DENY overrides ALLOW)

This phase is where “Vault” becomes an actual decision engine, not just data modeling.

---

## Task 2.1 — Strategy pattern baseline (done)

### What we added
- `backend/src/main/java/com/vault/engine/RuleEvaluatorStrategy.java`
  - A contract for “rule evaluators” (strategies).
- `backend/src/main/java/com/vault/engine/BooleanRuleEvaluator.java`
  - First evaluator implementation.

### Why we did it this way
This is the Strategy Pattern:
- The **engine** is the orchestrator.
- Each **evaluator** knows how to handle one rule type.
- New rule types become “add a class” instead of “edit a giant if/else”.

Spring DI wiring:
- `@Component` makes the evaluator discoverable as a bean.
- The engine can inject `List<RuleEvaluatorStrategy>` and treat it as a plugin registry.

Frontend analogy:
- Similar to a list of “handlers” (like middleware) that each declare `supports(...)` and then run if applicable.

---

## Task 2.2 — Deterministic RolloutRuleEvaluator (done)

### What we added
- `backend/src/main/java/com/vault/engine/MurmurHash3.java`
  - Deterministic MurmurHash3 32-bit hash (seed=0).
- `backend/src/main/java/com/vault/engine/RolloutRuleEvaluator.java`
  - RuleType `ROLLOUT` evaluator.
  - Computes stable bucket \(0..99\) from `featureKey + ":" + subjectId`.

### Rule shape (current minimal contract)
`RuleVersion.conditions` expects:

```json
{
  "percentage": 25
}
```

Context expects one of:
- `tenant_id`
- `user_id`

If missing/invalid, we **fail closed** with a DENY result.

### Why no Math.random()
Using randomness would make the decision flip across requests, which is a bad rollout UX.
Hashing guarantees:
- same inputs → same bucket
- consistent enablement/disablement for a subject over time

### Tests
- `backend/src/test/java/com/vault/engine/RolloutRuleEvaluatorTest.java`
  - Proves the same subject maps to the same bucket and decision
  - Proves percentage 0 is always deny and 100 is always allow

---

## Next up (Task 2.3)
- Implement hierarchy ordering + priority sorting
- Implement conflict rules:
  - **DENY overrides ALLOW**
  - **more specific overrides more general**
- Start producing a real `DecisionTrace` (we’ll carry it forward into Phase 3 API responses)

