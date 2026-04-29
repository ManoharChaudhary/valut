# Vault Mentoring Notes

This folder is the long-lived “engineering journal” for the Vault project.

## Backend delivery summary (for full-flow review)

The **Spring Boot backend** and **Next.js frontend** are in a **complete, reviewable state** through **enterprise scope** (multi-feature rules, scoped matching, global default, admin API, control plane UI):

| Phase | Theme | Status |
|-------|--------|--------|
| 1 | Foundation — Postgres, Flyway, JPA entities (`Tenant`, `FeatureDefinition`, `Rule`, `RuleVersion`) | Done |
| 2 | Core engine — strategies, deterministic rollout, hierarchy order, deny-wins resolver, trace | Done |
| 3 | Schema + API — JSON Schema pre-check, `POST /api/v1/decisions/evaluate`, rich trace/summary in JSON | Done |
| 4 | Performance — Caffeine cache on evaluate, `RuleUpdatedEvent` + cache eviction on rule append | Done |
| 5 | Frontend control plane — Next.js simulator (`frontend/`) | Done |
| 6–8 | Enterprise — Flyway V5–V7, `rule_features`, scopes, `public_id`, admin REST, `RuleManagementService` | Done |
| 9–11 | UI — `AppShell`, admin lists, simulator `featureId` + `DecisionBreadcrumbs` | Done (MVP) |

**Single doc for end-to-end flow (diagrams + sequence + terminology):**  
[backend-foundations-and-request-flow.md](backend-foundations-and-request-flow.md) — read sections **2**, **3**, **3b**, and **13–14** for the full picture.

**Phase-by-phase implementation detail:**  
[phase-01](phase-01-foundation.md) → [phase-02](phase-02-core-engine.md) → [phase-03](phase-03-schema-validation-and-api.md) → [phase-04](phase-04-caching-and-events.md) → [phase-05](phase-05-frontend-control-plane.md).

**Backend → frontend chat handoff:** [../BACKEND_HANDOFF_PROMPT.md](../BACKEND_HANDOFF_PROMPT.md)

---

## Start here (frontend → backend bridge)
- [backend-foundations-and-request-flow.md](backend-foundations-and-request-flow.md) — HTTP → MVC → validation → services → cache → DB → events; diagrams, analogies, debugging.

## Companion guides
- [how-to-run-and-verify.md](how-to-run-and-verify.md) — commands and smoke checks.
- [troubleshooting.md](troubleshooting.md) — common failures we already hit in this repo.
- [spring-and-jpa-primer.md](spring-and-jpa-primer.md) — annotation / concept cheat sheet.

## How to use
- Read **phase notes** in order. Each phase doc contains:
  - What we built
  - Why we built it that way (trade-offs)
  - How to run/verify
  - Blockers we hit and how we resolved them
- Treat this like a “handoff doc” you could give to another engineer joining mid-stream.

## Phase notes
- [phase-01-foundation.md](phase-01-foundation.md)
- [phase-02-core-engine.md](phase-02-core-engine.md)
- [phase-03-schema-validation-and-api.md](phase-03-schema-validation-and-api.md)
- [phase-04-caching-and-events.md](phase-04-caching-and-events.md)
- [phase-05-frontend-control-plane.md](phase-05-frontend-control-plane.md)
