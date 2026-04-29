# Phase 5 — Frontend control plane (Weeks 9–10) + enterprise UI (Phases 9–11)

## Status

**Delivered** under `frontend/`: Next.js 15 (App Router), TanStack Query, dev proxy rewrite, **slate-950** shell with **blue** primary and **emerald / rose** permit/deny accents, modular UI primitives (`Button`, `Card`, `Input`, `Label`, `Badge`), **`AppShell`** navigation.

## Scope in repo

- **`/`** — Home with links to simulator, admin features, admin rules.
- **`/simulator`** — Select **`featureId`** (UUID) from loaded features or paste UUID; fields **`tenant_id`**, **`sector`**, **`role_id`** plus optional extra JSON merged into `context`; **Evaluate** calls `POST /api/v1/decisions/evaluate` via **`/vault-api/...`**. Renders summary, reasons, evaluation path, **`DecisionBreadcrumbs`** (waterfall-style ordering from trace), and **`trace`** timeline.
- **`/admin/features`** — Lists features from **`GET /api/v1/admin/features`** (id, `publicId`, keys, display name).
- **`/admin/rules`** — Lists rules from **`GET /api/v1/admin/rules`** using **`RuleCard`** (scopes, default flag, linked feature ids).
- **`src/lib/decisions-api.ts`** — Sends **`featureId`** and/or **`featureKey`** in evaluate payload; **`admin-api.ts`** wraps admin REST.

## Run locally

```bash
cd frontend && npm install && npm run dev
```

Default rewrite target: `http://localhost:8080` (override with `BACKEND_URL` in `.env.local`; see `frontend/.env.example`).

## Backend contract

- **POST** evaluate with **`featureId`** (UUID string) or **`featureKey`**; response unchanged (`decision`, `summary`, `reasons`, `evaluationPath`, matched ids, `trace`).
- **Admin** JSON APIs under **`/api/v1/admin/**`** (no CORS needed when using `/vault-api` rewrite).

## Handoff from backend work

Use **`docs/BACKEND_HANDOFF_PROMPT.md`** as a paste-ready context block for a new chat focused on UI and integration.
