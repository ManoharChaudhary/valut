# Troubleshooting (Vault backend)

Symptoms-first guide. Pair with [how-to-run-and-verify.md](how-to-run-and-verify.md).

## Port 8080 already in use
**Symptom:** `Web server failed to start. Port 8080 was already in use.`  
**Cause:** Another Spring Boot (or any) process still bound to 8080.  
**Fix:** Kill the listener or change `SERVER_PORT`. See run guide.

## 404 on `/api/v1/decisions/evaluate`
**Symptom:** JSON error `Not Found` for that path.  
**Common causes:**
1. **GET instead of POST** — browser bar uses GET. Use `curl -X POST` or your HTTP client with POST.
2. **Old build** — controller not on classpath. Run `./mvnw clean compile` and restart.
3. **Wrong host/port** — hitting a different service.

**Note:** Vault maps GET `/evaluate` to **405** with `Allow: POST` for clarity; some setups still show 404 if code is outdated.

## 400 on evaluate
**Symptom:** `400` with validation errors.  
**Cause:** Bean Validation on `EvaluateRequest` (e.g. blank `featureKey`).  
**Fix:** Send JSON with non-blank `featureKey`.

## 415 Unsupported Media Type
**Symptom:** `415` on POST.  
**Cause:** Missing or wrong `Content-Type`.  
**Fix:** `Content-Type: application/json`.

## 200 but `decision` is always `DENY`
**Symptom:** API works but outcome is deny.  
**Likely correct behavior (fail closed):**
- No row in `feature_definitions` for that `featureKey`
- `context` fails JSON Schema in `context_schema`
- No active `rules` for that feature
- Rule conditions fail (boolean false, rollout bucket out of range)

**Fix:** Inspect DB rows and schema; read `reasons` and `trace` / `summary` in the response.

## `ObjectMapper` bean not found (startup)
**Symptom:** `ContextSchemaValidator` required `ObjectMapper` bean.  
**Status:** Fixed by constructing `ObjectMapper` inside the validator (see git history). If you reintroduce constructor injection, ensure Jackson auto-config is present or provide a `@Bean`.

## Maven cannot write `~/.m2` (sandbox / permissions)
**Symptom:** `mkdir: .../.m2: Operation not permitted`.  
**Cause:** Restricted environment blocking home-dir cache.  
**Fix:** Run Maven outside restricted sandbox or with normal user permissions.

## Postgres connection refused
**Symptom:** JDBC errors, Flyway cannot connect.  
**Fix:** Start Postgres; verify URL/user/password in `application-local.yml`.

## Git / Xcode command line tools
**Symptom:** `xcode-select` errors when running `git`.  
**Fix:** Install Apple Command Line Tools or Xcode.
