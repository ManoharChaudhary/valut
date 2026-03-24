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

Evaluate (POST only):

```bash
curl -s -X POST http://localhost:8080/api/v1/decisions/evaluate \
  -H "Content-Type: application/json" \
  -d '{"featureKey":"demo.feature","context":{"tenant_id":"t-1"}}'
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

## Related reading
- [backend-foundations-and-request-flow.md](backend-foundations-and-request-flow.md)
- [troubleshooting.md](troubleshooting.md)
