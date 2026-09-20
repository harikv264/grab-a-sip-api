# CLAUDE.md — grab-a-sip-api (backend)

Repo facts for the **Grab A Sip** backend — the system of record for leads,
customers, subscriptions, deliveries and identity. The global
`~/.claude/CLAUDE.md` owns *how to think*; this file owns *this repo*.

- **`docs/DESIGN.md`** — API + domain design conventions (endpoints, DTOs,
  errors, auth model). Read before adding/changing an endpoint or entity.
- **`docs/RULES.md`** — engineering rules for this repo. Read before coding.

---

## What this is

A **modular monolith** Spring Boot service. Deliberately not microservices —
one deployable, organised by package. Fronts a Supabase Postgres DB and verifies
Supabase-issued JWTs. Consumed by the web app (`grab-a-sip`) and the mobile app
(`grab-a-sip-app`).

## Stack

Spring Boot 3.3.5 · Java 21 · Spring MVC · Spring Data JPA (Hibernate) ·
Spring Security (OAuth2 resource server, JWT) · PostgreSQL · Apache POI (xlsx
export) · Maven (wrapper) · Docker.

## Commands

```bash
./mvnw spring-boot:run          # run locally on :8080 (needs DB env vars)
./mvnw clean package            # build the jar (target/*.jar) — must pass
./mvnw test                     # run tests
java -jar target/*.jar          # run the built jar
docker build -t grab-a-sip-api .    # container build (as Render deploys)
```

JDK 21 required. Locally: `JAVA_HOME=/opt/homebrew/opt/openjdk@21/...`.

## Structure (`src/main/java/com/grabasip/api/`)

```
GrabASipApiApplication.java   entrypoint (@EnableScheduling)
config/     SecurityConfig (JWKS + permitAll), CorsConfig
domain/     JPA entities (Lead, Customer, Subscription, Delivery,
            DeliveryPerson, Holiday, SubscriptionPause, AppUser, ServiceArea)
repo/       Spring Data repositories (incl. rider/customer-scoped queries)
security/   AdminGuard (token OR admin-JWT), CurrentUserService (role from sub)
service/    ServiceabilityService, PlanCatalog, DeliveryGenerationService,
            ScheduledJobs, ServiceAreaSeeder
web/        REST controllers (Lead, Customer, Subscription, Delivery, Me,
            AppUser, Rider, CustomerSelf, Plan, Serviceability, Holiday, …)
src/main/resources/application.properties
```

## Auth model (read before touching security)

- **Supabase asymmetric JWTs**, verified via JWKS at
  `${SUPABASE_URL}/auth/v1/.well-known/jwks.json` (RS256/ES256). `SUPABASE_URL`
  is the bare project URL.
- `app_users` maps a Supabase `uid` → role (`admin` / `rider` / `customer`) and
  links to a `customer_id` / `delivery_person_id`.
- Legacy `ADMIN_TOKEN` still works as a fallback for admin endpoints
  (`AdminGuard` accepts token OR admin-JWT) — kept for resilience during rollout.
- Rider/customer endpoints are **scoped to the caller** — never return another
  user's data.

## Environment & deploy

- Env vars: see `.env.example` (DB via Supabase **session pooler**, not the
  direct IPv6 host; `ADMIN_TOKEN`; `CORS_ALLOWED_ORIGINS`; `SUPABASE_URL`).
- Hosted on **Render** (Docker), auto-deploys on push to `main`. Free tier
  **sleeps when idle** (~50s cold start); scheduled jobs may not fire while
  asleep — don't assume perfect timeliness.
- Scheduled jobs (`ScheduledJobs`): monthly pause reset (00:05 IST, 1st) and
  daily delivery generation (05:00 IST).
