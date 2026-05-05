# Phase 5 — Frontend control plane (Weeks 9–10)

## Status
**Not implemented in this repo yet** — backend is ready to be called from a Next.js app.

## Planned scope (from execution plan)
- `/frontend` — Next.js App Router.
- “Rule / decision simulator” page: JSON `context` editor, `featureKey`, call `POST /api/v1/decisions/evaluate`, render `summary`, `evaluationPath`, and `trace` (timeline UI).

## Integration notes for when you build it
- Backend expects **POST** with `Content-Type: application/json`.
- Response includes `decision`, `summary`, `reasons`, `evaluationPath`, `matchedRuleId`, `matchedRuleVersionId`, `trace`.
- Configure `NEXT_PUBLIC_API_BASE_URL` (or dev rewrites) to `http://localhost:8080` during local dev.
- CORS: if the browser calls a different origin, add a Spring `WebMvcConfigurer` CORS mapping or proxy via Next.js `rewrites`.
