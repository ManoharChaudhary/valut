# Phase 4 — Performance & caching (Weeks 7–8)

## Task 4.1 — Caffeine L1 cache on evaluation (done)

### What we added
- Dependencies: `spring-boot-starter-cache`, `com.github.ben-manes.caffeine` in `backend/pom.xml`.
- `backend/src/main/java/com/vault/config/CacheConfig.java` — `@EnableCaching`.
- `backend/src/main/java/com/vault/cache/DecisionEvaluationKeyGenerator.java` — Spring `KeyGenerator` bean named `decisionEvaluationKeyGenerator`.
- `@Cacheable` on `DecisionEngineService.evaluate(...)` with cache name **`decisions`**.
- `application.yml`:
  - `spring.cache.type=caffeine`
  - `spring.cache.cache-names=decisions`
  - `spring.cache.caffeine.spec=maximumSize=10000,expireAfterWrite=5m`

### Why this design
- **L1 in-process cache** avoids repeated DB + JSON Schema + rule evaluation for identical `(featureKey, context)` pairs.
- **Deterministic key**: `featureKey` + canonical JSON for `context` (Jackson `ORDER_MAP_ENTRIES_BY_KEYS`) + MurmurHash3 — stable across JVM restarts for the same inputs (hash int folded into key string).
- **TTL (`expireAfterWrite`)**: balances freshness vs latency; tune per environment.

### Mentoring: how `@Cacheable` works (frontend analogy)
Spring wraps your `@Service` bean in a **proxy**. When a caller invokes `evaluate`, the proxy checks the cache first; on miss it runs the real method and stores the return value.

Rough analogy: a **memoized** pure function wrapper around your server handler — but backed by Caffeine instead of React `useMemo` state.

**Caveat:** `@Cacheable` only applies to calls **through the Spring proxy** (external bean → service). Internal `this.evaluate(...)` calls would bypass cache (not an issue here).

### Verification
1. Run the app; call `POST /evaluate` twice with the same body; second call should be faster (hard to see without metrics — optional future: log cache hit).
2. Run unit test: `DecisionEvaluationKeyGeneratorTest` proves stable keys for different `Map` insertion orders.

### Next (Task 4.2)
- Publish `RuleUpdatedEvent` on rule mutations and `@CacheListener` / `@CacheEvict` to drop stale `decisions` entries (feature-scoped eviction first).
