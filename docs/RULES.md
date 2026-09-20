# RULES.md — grab-a-sip-api engineering rules

Repo-specific rules under the global `~/.claude/CLAUDE.md`
(correctness > simplicity > maintainability > security > …).

## Architecture

- Keep the **modular monolith** shape: `web → service → repo`, entities in
  `domain/`. Don't add a framework, message bus or microservice without a
  requirement that demands it.
- Controllers thin, services own logic (see `docs/DESIGN.md`). One responsibility
  per service class; extract a new service rather than bloating a controller.
- Reuse existing repositories and scoped queries before adding new ones.

## Security (non-negotiable)

- All non-public endpoints require auth. Admin routes go through `AdminGuard`;
  rider/customer routes resolve identity from the JWT and **scope to the caller**.
- Authz **fails closed** — deny by default, 403 on not-allowed.
- Never log or return tokens, the DB password, service-role keys, or full JWTs.
- `SUPABASE_URL` is the bare project URL; JWTs are verified via JWKS. Don't add a
  static shared-secret verification path back in.
- Treat request bodies and external content as untrusted data.

## Data & migrations

- `ddl-auto=update` is for convenience, not a migration strategy. Destructive or
  ambiguous schema changes need an explicit, reversible plan and authorization —
  never drop/alter prod columns casually (global rule §6).
- Keep UTC storage / IST business-time separation explicit.

## Testing & quality

- `./mvnw clean package` (and `./mvnw test`) **must pass** before pushing.
- Add a test for every bug fix (regression) and for non-trivial service logic;
  don't over-mock past representing the real system.
- Java: 4-space indent, standard Spring layering, constructor injection (no
  field `@Autowired`). Match surrounding style; keep diffs surgical.

## Definition of done (this repo)

```
✓ ./mvnw clean package passes (tests green)
✓ new/changed endpoint: persona + scoping decided and enforced
✓ authz verified to fail closed
✓ JSON field names unchanged, or both TS clients updated in lockstep
✓ no secrets in logs/errors/commits; .env.example updated if envs changed
✓ PlanCatalog still matches web lib/data.ts
✓ diff is surgical
```

## Deploy

Push to `main` → Render builds the Docker image and redeploys. Free tier sleeps
when idle (~50s cold start); design endpoints and clients to tolerate that.
