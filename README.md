# Waypoint

Goal tracking application — set goals, break them into milestones, deposit funds, allocate to milestones, and track progress.

## Architecture

```
Clients → REST API (Spring Boot) → PostgreSQL
                ↓
         React (Web) / iOS / Android
```

## Modules

| Module | Stack | Description |
|---|---|---|
| `waypoint-api` | Spring Boot 4.1, Java 21, Maven | REST API backend |
| `waypoint-web` | React 19, Vite 8, TypeScript 6 | Web frontend |

## Quick Start

### Prerequisites

- Java 21+
- Node.js 22+
- Docker & Docker Compose

### Option A: Full stack via Docker

Runs everything (API, web, DB, MinIO) in containers — production-like, no hot reload.

```bash
docker compose up --build
```

| Service | URL |
| --- | --- |
| Web | http://localhost:8880 |
| API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/api/v1/swagger-ui.html |
| MinIO console | http://localhost:9001 |
| Dozzle (logs) | http://localhost:8889 |

### Option B: Local development

Run infrastructure (DB, MinIO) in Docker, but the API and frontend natively for hot reload.

**1. Start infrastructure:**

```bash
docker compose up db minio -d
```

This starts:
- **PostgreSQL 16** on `localhost:5432` (db=`waypoint`, user=`waypoint`, pass=`waypoint`)
- **MinIO** on `localhost:9000` (S3) and `localhost:9001` (console, user=`minioadmin`, pass=`minioadmin`)

**2. Start the API:**

```bash
cd waypoint-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API runs on `http://localhost:8080/api/v1`. The `dev` profile enables SQL logging. All config (DB credentials, JWT secret, MinIO endpoint) has dev defaults baked into `application.yaml` — no `.env` file needed.

**3. Start the frontend:**

```bash
cd waypoint-web
npm install
npm run dev
```

Vite dev server runs on `http://localhost:5173` and proxies API calls to `http://localhost:8080/api/v1` automatically.

### Ports reference

| Port | Service | Notes |
| --- | --- | --- |
| 5432 | PostgreSQL | |
| 8080 | API (Spring Boot) | context path `/api/v1` |
| 8880 | Web (nginx, Docker) | only in Option A |
| 5173 | Web (Vite dev server) | only in Option B |
| 9000 | MinIO (S3 API) | |
| 9001 | MinIO console | |
| 8889 | Dozzle (log viewer) | only in Option A |

## API Documentation

Swagger UI is available when the API is running:

- **Swagger UI:** http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI spec:** http://localhost:8080/api/v1/v3/api-docs

### API Base

All endpoints are under `/api/v1/`.

### Authentication

JWT-based. Register → login → use `Authorization: Bearer <token>`.

| Endpoint | Method | Description |
|---|---|---|
| `/auth/register` | POST | Create account |
| `/auth/login` | POST | Login |
| `/auth/refresh` | POST | Refresh token |
| `/auth/logout` | POST | Invalidate session |
| `/auth/me` | GET | Current user profile |
| `/auth/me` | PATCH | Update profile |

### Goals

| Endpoint | Method | Description |
|---|---|---|
| `/goals` | GET | List goals |
| `/goals` | POST | Create goal |
| `/goals/{id}` | GET | Get goal |
| `/goals/{id}` | PATCH | Update goal |
| `/goals/{id}` | DELETE | Delete goal |
| `/goals/reorder` | PATCH | Batch reorder |

### Milestones

| Endpoint | Method | Description |
|---|---|---|
| `/goals/{goalId}/milestones` | GET | List milestones |
| `/goals/{goalId}/milestones` | POST | Create milestone |
| `/goals/{goalId}/milestones/{id}` | PATCH | Update milestone |
| `/goals/{goalId}/milestones/{id}` | DELETE | Delete milestone |
| `/goals/{goalId}/milestones/{id}/complete` | POST | Mark complete |
| `/goals/{goalId}/milestones/{id}/uncomplete` | POST | Undo completion |
| `/goals/{goalId}/milestones/{id}/toggle` | PATCH | Toggle enabled |
| `/goals/{goalId}/milestones/reorder` | PATCH | Batch reorder |
| `/goals/{goalId}/milestones/toggle-all` | PATCH | Enable/disable all |
| `/goals/{goalId}/milestones/{id}/transfers` | GET | Per-milestone transfers |

### Deposits

| Endpoint | Method | Description |
|---|---|---|
| `/goals/{goalId}/deposits` | GET | List deposits |
| `/goals/{goalId}/deposits` | POST | Add deposit |
| `/goals/{goalId}/deposits/{id}` | PATCH | Update deposit |
| `/goals/{goalId}/deposits/{id}` | DELETE | Delete deposit |

### Transfers

| Endpoint | Method | Description |
|---|---|---|
| `/goals/{goalId}/transfers` | GET | List transfers |
| `/goals/{goalId}/transfers/allocate` | POST | Allocate wallet → milestone |
| `/goals/{goalId}/transfers/withdraw` | POST | Withdraw milestone → wallet |
| `/goals/{goalId}/transfers/{id}` | PATCH | Update transfer |
| `/goals/{goalId}/transfers/{id}` | DELETE | Delete transfer |

### Completions

| Endpoint | Method | Description |
|---|---|---|
| `/goals/{goalId}/completions` | GET | List completion history |
| `/goals/{goalId}/completions/{id}` | DELETE | Undo completion |

### Analytics

| Endpoint | Method | Description |
|---|---|---|
| `/goals/{id}/analytics` | GET | Per-goal analytics |
| `/analytics/summary` | GET | Cross-goal summary |

## Response Format

Success:
```json
{
  "data": { ... },
  "meta": { "requestId": "uuid", "timestamp": "2026-06-25T12:00:00Z" }
}
```

Error:
```json
{
  "error": { "code": "ERROR_CODE", "message": "Human-readable message", "details": {} }
}
```

## Database

PostgreSQL 16 with Flyway migrations in `waypoint-api/src/main/resources/db/migration/`.

Schema: `users`, `refresh_tokens`, `goals`, `milestones`, `deposits`, `transfers`, `completions`.

All monetary values in **integer cents**.

## Design Docs

Detailed design documentation is in the [`documents/`](documents/) folder:
- [ARCHITECTURE.md](documents/ARCHITECTURE.md) — System architecture, schema, API design
- [CI-CD.md](documents/CI-CD.md) — CI/CD pipeline, Docker Compose, deployment
- [MVP.md](documents/MVP.md) — MVP feature specification
- [IDEAS.md](documents/IDEAS.md) — Post-MVP feature ideas
