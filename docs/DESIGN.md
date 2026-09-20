# DESIGN.md — grab-a-sip-api API & domain design

How endpoints, DTOs, persistence and errors are shaped in this service. "Design"
here is API + domain design (there is no UI). Follow these so the surface stays
predictable for the web and mobile clients.

---

## 1. Layering

```
web/ (controllers)  →  service/ (business logic)  →  repo/ (persistence)
                                  ↑ domain/ (entities)
```

- Controllers are thin: validate input, call a service, map to a response.
  **No business logic or repository calls straight from a controller** beyond
  trivial reads.
- Services own the rules (serviceability, delivery generation, pause limits).
- Entities live in `domain/`; repositories in `repo/`.

## 2. REST conventions

- Base path `/api`. Resource-oriented, plural nouns: `/api/customers`,
  `/api/subscriptions`, `/api/deliveries`.
- Persona-scoped trees: `/api/admin/*` (admin), `/api/rider/*` (rider),
  `/api/customer/*` (customer self-service), `/api/me` (identity/role probe).
- Verbs = HTTP methods (GET/POST/PUT/DELETE). Return the resource or a small DTO,
  not the raw entity graph.
- JSON field names are `camelCase` and match what the TS clients already expect
  (e.g. `planName`, `pauseDaysUsed`, `deliveredThisMonth`). Changing a name is a
  breaking change — update both clients in lockstep.

## 3. DTOs & entities

- Don't leak lazy JPA associations into JSON (`open-in-view=false` is set).
  Return purpose-built response shapes for anything non-trivial.
- Money is integer rupees (₹, no decimals) — matches the catalog.
- Timestamps stored UTC (`hibernate.jdbc.time_zone=UTC`); business scheduling is
  IST — keep that distinction explicit.

## 4. Errors

- Validation via `spring-boot-starter-validation` (`@Valid` + constraints).
- Use appropriate status codes: 400 (bad input), 401 (no/'bad token), 403
  (authenticated but not allowed), 404 (missing), 409 (conflict).
- **Authz failures fail closed** (403), never silently return empty success.
- Don't put secrets, tokens or SQL in error bodies.

## 5. Auth & scoping

- Admin endpoints: `AdminGuard` (legacy `ADMIN_TOKEN` OR an admin-role JWT).
- Rider/customer endpoints: resolve the caller from the JWT `sub`
  (`CurrentUserService`) and **scope every query to that user**. A rider sees
  only their deliveries; a customer only their own subscriptions/deliveries.
- New entity/endpoint → decide its persona and scoping *first*.

## 6. Persistence

- `ddl-auto=update` (dev-convenient). For anything destructive or ambiguous,
  prefer an explicit migration/SQL over trusting auto-DDL. Never drop/alter
  columns in prod without an authorized, reversible plan.
- DB reached through the Supabase **session pooler** (IPv4) — see CLAUDE.md.

## 7. Catalog (source of truth)

Plans/prices live in `PlanCatalog` (Small ₹1699, Large ₹2199, ABC ₹1500,
Classic ₹1350; ~24–26 boxes/month; up to 5 pause days). Keep it aligned with the
web `lib/data.ts` — they must not drift.
