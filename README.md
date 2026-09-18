# Grab A Sip — API (Spring Boot)

Backend for Grab A Sip: **serviceability check**, **lead capture**, and an
Excel (`.xlsx`) **undelivered-areas export**, backed by **Supabase Postgres**.

- Java 21 · Spring Boot 3.3 · Spring Data JPA · Apache POI
- Modular monolith — grows into deliveries/inventory in later phases
- Frontend (Next.js) calls this API; this API is the system of record

## Endpoints

| Method | Path | Auth | Purpose |
| ------ | ---- | ---- | ------- |
| GET  | `/api/health` | – | Liveness check |
| GET  | `/api/serviceability/check?q=Gachibowli` | – | `serviceable` / `not_serviceable` / `ask_again` |
| GET  | `/api/serviceability/areas` | – | Served-locality list (for autocomplete) |
| POST | `/api/leads` | – | Record a check as a lead |
| GET  | `/api/leads?token=…&all=1` | admin | List leads (undelivered by default) |
| GET  | `/api/leads/export?token=…&all=1` | admin | Download `.xlsx` |
| GET  | `/api/customers?token=…&q=` | admin | List / search customers |
| GET  | `/api/customers/{id}?token=…` | admin | One customer |
| POST | `/api/customers?token=…` | admin | Create (validates locality + unique phone) |
| PUT  | `/api/customers/{id}?token=…` | admin | Update (incl. mark address verified) |

`POST /api/leads` body:
```json
{ "rawLocation": "flat 302, madhapur", "pincode": "500081",
  "matchedArea": "Madhapur", "serviceable": true, "phone": "9876543210" }
```

## Configuration (environment variables)

| Var | Example | Notes |
| --- | ------- | ----- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres` | Supabase Postgres |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | |
| `SPRING_DATASOURCE_PASSWORD` | `••••••` | your Supabase DB password |
| `ADMIN_TOKEN` | long random string | protects the admin endpoints |
| `CORS_ALLOWED_ORIGINS` | `https://grab-a-sip.vercel.app` | your site origin(s), comma-separated |
| `PORT` | `8080` | injected by Render/Railway |

Tables (`leads`, `service_areas`) are created automatically by JPA
(`ddl-auto=update`) — no SQL to run. The served localities are seeded on
first boot.

## Connect Supabase (what you do)

1. Create a project at [supabase.com](https://supabase.com).
2. **Project Settings → Database → Connection string → JDBC** (or "URI").
   Use the **Session pooler** / direct connection. It looks like
   `jdbc:postgresql://db.<ref>.supabase.co:5432/postgres`.
3. Grab the DB password you set when creating the project.
4. Put those into `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` (below).

## Run locally

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://db.<ref>.supabase.co:5432/postgres
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your-password
export ADMIN_TOKEN=dev-token
export CORS_ALLOWED_ORIGINS=http://localhost:3000
./mvnw spring-boot:run          # or: mvn spring-boot:run
```
Health check: `curl localhost:8080/api/health`

Build a jar: `./mvnw -DskipTests package` → `target/grab-a-sip-api-0.1.0.jar`

## Deploy on Render (recommended)

1. Push this repo to GitHub (done).
2. Render → **New → Web Service** → connect the repo.
3. Environment: **Docker** (the included `Dockerfile` builds & runs it).
4. Add the environment variables from the table above.
5. Deploy. Render gives you a URL like `https://grab-a-sip-api.onrender.com`.
6. Point the frontend at it: set `NEXT_PUBLIC_API_BASE_URL` to that URL in
   Vercel, and set `CORS_ALLOWED_ORIGINS` here to your Vercel domain.

## Keep in sync

The served-locality list + matching logic in
`service/ServiceabilityService.java` mirrors the frontend's
`lib/serviceability.ts`. Update both together when delivery areas change.
