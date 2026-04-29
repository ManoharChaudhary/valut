# How to run and verify Vault (local)

Use this as a copy-paste checklist whenever you change backend code or DB schema.

## Prerequisites
- **JDK 21** on `PATH` (or set `JAVA_HOME` to Temurin 21).
- **PostgreSQL** running locally.
- Database `vault`, user `vault`, password `vault` (matches `application-local.yml`).

## Start the API

```bash
cd backend
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

If **port 8080 is in use**:

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
lsof -ti tcp:8080 | xargs kill -9
```

Or run on another port:

```bash
SPRING_PROFILES_ACTIVE=local SERVER_PORT=8081 ./mvnw spring-boot:run
```

## Smoke checks

```bash
curl -s http://localhost:8080/actuator/health
```

Evaluate (POST only). Use **`featureKey`** or **`featureId`** (feature `public_id` UUID); seeded core features require **`tenant_id`**, **`sector`**, and **`role_id`** in `context` (see Flyway **V6**).

```bash
# By featureKey (when that row exists)
curl -s -X POST http://localhost:8080/api/v1/decisions/evaluate \
  -H "Content-Type: application/json" \
  -d '{"featureKey":"core.user_management","context":{"tenant_id":"c1111111-1111-4111-8111-111111111111","sector":"BIGSEG","role_id":"b1111111-1111-4111-8111-111111111111"}}'

# By featureId (UUID from GET /api/v1/admin/features)
curl -s -X POST http://localhost:8080/api/v1/decisions/evaluate \
  -H "Content-Type: application/json" \
  -d '{"featureId":"d1111111-1111-4111-8111-111111111111","context":{"tenant_id":"c1111111-1111-4111-8111-111111111111","sector":"BIGSEG","role_id":"b1111111-1111-4111-8111-111111111111"}}'
```

## Unit tests (no DB required for most tests)

```bash
cd backend
./mvnw test
```

## Flyway / schema sanity (psql)

```sql
select version, description, success
from flyway_schema_history
order by installed_rank;

\dt
```

## Caching (Phase 4)
In-memory **Caffeine** cache (`decisions`) wraps `DecisionEngineService.evaluate` with a **5 minute** write TTL (see `application.yml`). After **`RuleAdminService.appendRuleVersion`** or **`RuleManagementService`** mutations, a **`RuleUpdatedEvent`** clears the `decisions` cache so clients are not stuck until TTL (see Phase 4 Task 4.2).

## Related reading
- [backend-foundations-and-request-flow.md](backend-foundations-and-request-flow.md)
- [troubleshooting.md](troubleshooting.md)
- [phase-04-caching-and-events.md](phase-04-caching-and-events.md)
