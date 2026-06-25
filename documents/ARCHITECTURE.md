# Waypoint — New Architecture Blueprint

Target stack: **Spring Boot + PostgreSQL → REST API → React (web) → iOS / Android (native)**

---

## 1. System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Clients                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ React    │  │ iOS      │  │ Android  │  │ 3rd-party│   │
│  │ (Web)    │  │ (Native) │  │ (Native) │  │ API      │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       │             │             │             │          │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
   ┌────▼─────────────▼─────────────▼─────────────▼────┐
   │                    API Gateway                     │
   │         (Spring Cloud Gateway / Nginx)            │
   │         Rate limiting · Auth · Routing            │
   └────────────────────┬──────────────────────────────┘
                        │
   ┌────────────────────▼──────────────────────────────┐
   │              REST API (Spring Boot)                │
   │  ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
   │  │ Auth     │ │ Goals    │ │ Users            │   │
   │  │ Controller│ │ Controller│ │ Controller      │   │
   │  └──────────┘ └──────────┘ └──────────────────┘   │
   │  ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
   │  │ Service  │ │ Service  │ │ Service          │   │
   │  │ Layer    │ │ Layer    │ │ Layer            │   │
   │  └──────────┘ └──────────┘ └──────────────────┘   │
   │  ┌──────────────────────────────────────────────┐  │
   │  │         Repository Layer (Spring Data JPA)   │  │
   │  └────────────────────┬─────────────────────────┘  │
   └───────────────────────┼────────────────────────────┘
                           │
   ┌───────────────────────▼────────────────────────────┐
   │              PostgreSQL                             │
   │  users · goals · milestones · deposits · transfers │
   │  completions · refresh_tokens                      │
   └────────────────────────────────────────────────────┘
```

### Cross-Cutting Services

| Service | Technology | Purpose |
|---|---|---|
| Auth | Spring Security + JWT | Stateless auth, token refresh |
| Validation | Jakarta Validation | Request validation |
| Migrations | Flyway | Schema versioning |
| Caching | Redis (optional) | Session cache, rate limiting |
| Async | Spring @Async / Kafka (if needed) | Email notifications |
| Monitoring | Spring Actuator + Micrometer | Health checks, metrics |

---

## 2. Project Structure

### Backend (`waypoint-api/`)

```
waypoint-api/
├── build.gradle / pom.xml
├── Dockerfile
├── docker-compose.yml
├── src/main/java/com/waypoint/
│   ├── WaypointApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── CorsConfig.java
│   │   ├── JacksonConfig.java
│   │   └── OpenApiConfig.java
│   ├── auth/
│   │   ├── controller/AuthController.java
│   │   ├── dto/LoginRequest.java
│   │   ├── dto/RegisterRequest.java
│   │   ├── dto/AuthResponse.java
│   │   ├── dto/RefreshTokenRequest.java
│   │   ├── service/AuthService.java
│   │   ├── service/JwtService.java
│   │   ├── model/User.java
│   │   ├── model/RefreshToken.java
│   │   └── repository/UserRepository.java
│   │   └── repository/RefreshTokenRepository.java
│   ├── goal/
│   │   ├── controller/GoalController.java
│   │   ├── dto/CreateGoalRequest.java
│   │   ├── dto/UpdateGoalRequest.java
│   │   ├── dto/GoalResponse.java
│   │   ├── service/GoalService.java
│   │   ├── model/Goal.java
│   │   └── repository/GoalRepository.java
│   ├── milestone/
│   │   ├── controller/MilestoneController.java
│   │   ├── dto/...
│   │   ├── service/MilestoneService.java
│   │   ├── model/Milestone.java
│   │   └── repository/MilestoneRepository.java
│   ├── deposit/
│   │   ├── controller/DepositController.java
│   │   ├── dto/...
│   │   ├── service/DepositService.java
│   │   ├── model/Deposit.java
│   │   └── repository/DepositRepository.java
│   ├── transfer/
│   │   ├── controller/TransferController.java
│   │   ├── dto/...
│   │   ├── service/TransferService.java
│   │   ├── model/Transfer.java
│   │   └── repository/TransferRepository.java
│   ├── completion/
│   │   ├── controller/CompletionController.java
│   │   ├── dto/...
│   │   ├── service/CompletionService.java
│   │   ├── model/Completion.java
│   │   └── repository/CompletionRepository.java
│   ├── analytics/
│   │   ├── controller/AnalyticsController.java
│   │   ├── dto/GoalAnalyticsResponse.java
│   │   └── service/AnalyticsService.java
│   └── common/
│       ├── exception/
│       ├── security/CurrentUser.java
│       └── util/
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__create_users_table.sql
│       ├── V2__create_goals_table.sql
│       ├── V3__create_milestones_table.sql
│       ├── V4__create_deposits_table.sql
│       ├── V5__create_transfers_table.sql
│       └── V6__create_completions_table.sql
└── src/test/java/com/waypoint/
```

### Frontend (`waypoint-web/`)

```
waypoint-web/
├── vite.config.ts
├── package.json
├── tsconfig.json
├── tailwind.config.ts
├── public/
│   ├── manifest.json
│   ├── icon.svg
│   └── sw.js
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── api/
│   │   ├── client.ts              (axios instance, interceptors)
│   │   ├── auth.ts
│   │   ├── goals.ts
│   │   ├── milestones.ts
│   │   ├── deposits.ts
│   │   ├── transfers.ts
│   │   └── completions.ts
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useGoals.ts
│   │   ├── useGoal.ts
│   │   ├── useMilestones.ts
│   │   ├── useDeposits.ts
│   │   └── useAnalytics.ts
│   ├── store/
│   │   └── authStore.ts            (Zustand)
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppShell.tsx
│   │   │   ├── Header.tsx
│   │   │   └── Sidebar.tsx
│   │   ├── auth/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── RegisterForm.tsx
│   │   │   └── ProtectedRoute.tsx
│   │   ├── goal/
│   │   │   ├── GoalCard.tsx
│   │   │   ├── GoalDropdown.tsx
│   │   │   ├── GoalForm.tsx
│   │   │   └── GoalList.tsx
│   │   ├── milestone/
│   │   │   ├── MilestoneRow.tsx
│   │   │   ├── MilestoneForm.tsx
│   │   │   ├── MilestoneList.tsx
│   │   │   ├── MilestoneProgress.tsx
│   │   │   └── MilestoneTransfer.tsx
│   │   ├── wallet/
│   │   │   ├── BalanceCard.tsx
│   │   │   ├── DepositForm.tsx
│   │   │   └── DepositList.tsx
│   │   ├── journal/
│   │   │   ├── CompletionJournal.tsx
│   │   │   └── MilestoneJournal.tsx
│   │   ├── settings/
│   │   │   ├── SettingsModal.tsx
│   │   │   ├── BackupSection.tsx
│   │   │   └── ProfileSection.tsx
│   │   └── ui/
│   │       ├── Button.tsx
│   │       ├── Modal.tsx
│   │       ├── Toast.tsx
│   │       ├── ProgressBar.tsx
│   │       └── StatusPill.tsx
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── DashboardPage.tsx
│   │   └── GoalPage.tsx
│   ├── lib/
│   │   ├── formatters.ts
│   │   ├── validators.ts
│   │   └── constants.ts
│   └── types/
│       ├── api.ts
│       ├── goal.ts
│       ├── milestone.ts
│       ├── deposit.ts
│       └── transfer.ts
```

---

## 3. Database Schema (PostgreSQL)

```sql
-- Users & Auth
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(120),
    locale        VARCHAR(5)   NOT NULL DEFAULT 'en',
    currency      VARCHAR(3)   NOT NULL DEFAULT 'USD',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Goals
CREATE TABLE goals (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title          VARCHAR(120) NOT NULL,
    description    TEXT         NOT NULL DEFAULT '',
    icon           VARCHAR(32)  NOT NULL DEFAULT 'target',
    sort_order     INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_goals_user_id ON goals(user_id);

-- Milestones
CREATE TABLE milestones (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id      UUID         NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    title        VARCHAR(120) NOT NULL,
    cost         INTEGER      NOT NULL DEFAULT 0 CHECK (cost >= 0),
    details      TEXT         NOT NULL DEFAULT '',
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    completed    BOOLEAN      NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    sort_order   INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_milestones_goal_id ON milestones(goal_id);

-- Deposits
CREATE TABLE deposits (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id    UUID        NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    amount     INTEGER     NOT NULL CHECK (amount > 0),
    note       TEXT        NOT NULL DEFAULT '',
    timestamp  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_deposits_goal_id ON deposits(goal_id);

-- Transfers (allocations & withdrawals)
CREATE TABLE transfers (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id      UUID         NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    milestone_id UUID         NOT NULL REFERENCES milestones(id) ON DELETE CASCADE,
    amount       INTEGER      NOT NULL,  -- positive = allocate, negative = withdraw
    type         VARCHAR(32)  NOT NULL DEFAULT 'allocate' CHECK (type IN ('allocate', 'withdraw', 'legacy')),
    comment      TEXT         NOT NULL DEFAULT '',
    timestamp    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_transfers_goal_id ON transfers(goal_id);
CREATE INDEX idx_transfers_milestone_id ON transfers(milestone_id);

-- Completion records
CREATE TABLE completions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id      UUID        NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    milestone_id UUID        NOT NULL REFERENCES milestones(id) ON DELETE CASCADE,
    amount       INTEGER     NOT NULL CHECK (amount >= 0),
    timestamp    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_completions_goal_id ON completions(goal_id);
CREATE INDEX idx_completions_milestone_id ON completions(milestone_id);
```

### Schema Notes

- All monetary amounts stored as **integer cents** (avoid floating-point issues)
- `sort_order` columns enable drag-and-drop reordering
- `user_id` on every entity enables multi-tenant isolation without schema separation
- Composite indexes not shown but should be added based on query patterns
- Cascade deletes ensure referential integrity (delete goal → cascades to all children)

---

## 4. REST API Design

### Base URL: `/api/v1`

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Create account (email, password, displayName) |
| POST | `/auth/login` | Login, returns access + refresh tokens |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Invalidate refresh token |
| GET | `/auth/me` | Get current user profile |
| PATCH | `/auth/me` | Update profile (locale, currency, displayName) |

**Token format**: JWT (access: 15min, refresh: 30 days, stored hashed in DB)

### Goals

| Method | Endpoint | Description |
|---|---|---|
| GET | `/goals` | List user's goals (active state, basic analytics per goal) |
| POST | `/goals` | Create goal |
| GET | `/goals/{id}` | Get goal with full analytics |
| PATCH | `/goals/{id}` | Update title/description |
| DELETE | `/goals/{id}` | Delete goal + all children |
| PATCH | `/goals/reorder` | Batch update sort_order |

### Milestones

| Method | Endpoint | Description |
|---|---|---|
| GET | `/goals/{goalId}/milestones` | List milestones (ordered, with status & balance) |
| POST | `/goals/{goalId}/milestones` | Create milestone |
| PATCH | `/goals/{goalId}/milestones/{id}` | Update title/cost/details |
| DELETE | `/goals/{goalId}/milestones/{id}` | Delete + cascade transfers/completions |
| POST | `/goals/{goalId}/milestones/{id}/complete` | Mark complete |
| POST | `/goals/{goalId}/milestones/{id}/uncomplete` | Undo completion |
| PATCH | `/goals/{goalId}/milestones/{id}/toggle` | Toggle enabled |
| PATCH | `/goals/{goalId}/milestones/reorder` | Batch reorder |
| PATCH | `/goals/{goalId}/milestones/toggle-all` | Enable/disable all |

### Deposits

| Method | Endpoint | Description |
|---|---|---|
| GET | `/goals/{goalId}/deposits` | List deposits (reverse chronological) |
| POST | `/goals/{goalId}/deposits` | Add deposit |
| PATCH | `/goals/{goalId}/deposits/{id}` | Update amount |
| DELETE | `/goals/{goalId}/deposits/{id}` | Delete deposit |

### Transfers

| Method | Endpoint | Description |
|---|---|---|
| GET | `/goals/{goalId}/transfers` | List all transfers |
| GET | `/goals/{goalId}/milestones/{id}/transfers` | Per-milestone journal |
| POST | `/goals/{goalId}/transfers/allocate` | Allocate wallet→milestone |
| POST | `/goals/{goalId}/transfers/withdraw` | Withdraw milestone→wallet |
| PATCH | `/goals/{goalId}/transfers/{id}` | Update amount |
| DELETE | `/goals/{goalId}/transfers/{id}` | Delete transfer entry |

### Completions

| Method | Endpoint | Description |
|---|---|---|
| GET | `/goals/{goalId}/completions` | List completion history |
| DELETE | `/goals/{goalId}/completions/{id}` | Undo (also unmarks milestone) |

### Analytics

| Method | Endpoint | Description |
|---|---|---|
| GET | `/goals/{id}/analytics` | Full analytics (balance, progress, next milestone, etc.) |
| GET | `/analytics/summary` | Cross-goal summary (total saved, total targets, active goal) |

### Response Envelope

```json
{
  "data": { ... },
  "meta": {
    "requestId": "uuid",
    "timestamp": "2026-06-24T12:00:00Z"
  }
}
```

### Error Envelope

```json
{
  "error": {
    "code": "MILESTONE_NOT_FOUND",
    "message": "Milestone with the given ID does not exist",
    "details": { "milestoneId": "..." }
  }
}
```

---

## 5. Frontend (React) — Key Decisions

### State Management

| Concern | Solution |
|---|---|
| Server state | **TanStack Query** (React Query) — cache, refetch, optimistic updates |
| Auth state | **Zustand** — lightweight, no boilerplate, persists tokens |
| Form state | **React Hook Form** — performant, validation via Zod |

### Routing (React Router v6)

```
/login          → LoginPage
/register       → RegisterPage
/dashboard      → DashboardPage (all goals overview)
/goals/:id      → GoalPage (single goal view)
```

### Data Flow

```
User action
  → React Hook Form validates
  → TanStack Query mutation fires
  → API call via axios (JWT in Authorization header)
  → Success: invalidate queries → UI re-renders
  → Error: toast notification, form error
```

### Key Libraries

| Library | Purpose |
|---|---|
| `@tanstack/react-query` | Server state, caching, mutations |
| `zustand` | Client-side auth state |
| `react-hook-form` + `zod` | Form handling & validation |
| `axios` | HTTP client with interceptors |
| `tailwindcss` | Utility-first styling |
| `react-router-dom` v6 | Routing |
| `@dnd-kit/core` | Drag-and-drop (milestone reorder) |
| `date-fns` | Date formatting |
| `vite-plugin-pwa` | PWA + service worker generation |
| `lucide-react` | Icon library |
| `framer-motion` | Animations (optional) |

### PWA Strategy

- `vite-plugin-pwa` generates service worker with precaching
- App shell cached on first load
- Dynamic data comes from API (no offline write support initially)
- Web Manifest remains similar to current

---

## 6. Authentication Flow

```
┌─────────┐         ┌──────────┐         ┌──────────┐
│  Client │         │  Spring  │         │ Postgres │
└────┬────┘         └────┬─────┘         └────┬─────┘
     │                   │                    │
     │  POST /auth/login │                    │
     │  {email,password} │                    │
     │──────────────────>│                    │
     │                   │  SELECT user       │
     │                   │───────────────────>│
     │                   │  user row          │
     │                   │<───────────────────│
     │                   │                    │
     │                   │  verify bcrypt     │
     │                   │  generate JWT      │
     │                   │  store refresh tok │
     │                   │───────────────────>│
     │  {accessToken,    │                    │
     │   refreshToken}   │                    │
     │<──────────────────│                    │
     │                   │                    │
     │  ─── Subsequent requests ────          │
     │  Authorization: Bearer <jwt>           │
     │──────────────────>│                    │
     │                   │  validate JWT      │
     │                   │  extract userId    │
     │                   │  (no DB lookup)    │
     │                   │                    │
     │  200 + data       │                    │
     │<──────────────────│                    │
┌────┴────┐         ┌────┴─────┐         ┌────┴─────┐
│ Zustand │         │ Spring   │         │ Postgres │
│ stores  │         │ Security │         │          │
│ token   │         │ filter   │         │          │
└─────────┘         └──────────┘         └──────────┘
```

- Access token: 15 minutes, contains `sub` (userId), `iat`, `exp`
- Refresh token: 30 days, stored as bcrypt hash in DB, can be revoked
- On 401 → axios interceptor tries refresh → if fails, redirect to login

---

## 7. Backend Service Logic: Transfer Validation

The transfer system from the MVP must be preserved with server-side enforcement:

```
allocate(goalId, milestoneId, requestedAmount):
  1. Load goal with deposits + transfers (within transaction)
  2. walletBalance = SUM(deposits) - SUM(transfers)
  3. milestoneBalance = SUM(transfers WHERE milestoneId)
  4. remainingNeed = milestone.cost - milestoneBalance
  5. allowed = min(walletBalance, remainingNeed, requestedAmount)
  6. if allowed <= 0 → reject with reason
  7. INSERT transfer (amount=allowed, type='allocate')
  8. Return { applied: allowed, requested: requestedAmount }
```

All validation happens server-side — client hints are purely UX convenience.

---

## 8. Security Considerations

| Concern | Mitigation |
|---|---|
| Auth | bcrypt for passwords, JWT with RS256, refresh token rotation |
| Rate limiting | Spring Cloud Gateway / bucket4j, tiered by endpoint |
| CORS | Whitelist known origins per environment |
| Input validation | Jakarta Validation + Zod (dual, server is source of truth) |
| SQL injection | JPA / PreparedStatements (never concatenate SQL) |
| XSS | React escapes by default, CSP headers |
| CSRF | Stateless JWT + `SameSite=Strict` cookies for refresh |
| Data isolation | Every query filtered by `userId` from JWT (never trust client ID) |
| Audit logging | Log all mutations with userId, timestamp, diff |

---

## 9. Deployment

### Docker Compose (Dev)

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: waypoint
      POSTGRES_USER: waypoint
      POSTGRES_PASSWORD: waypoint
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  api:
    build: ./waypoint-api
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/waypoint
      SPRING_DATASOURCE_USERNAME: waypoint
      SPRING_DATASOURCE_PASSWORD: waypoint
    depends_on:
      - db

  web:
    build: ./waypoint-web
    ports:
      - "5173:5173"
    depends_on:
      - api
```

### Production Considerations

- API behind a reverse proxy (Nginx / Cloudflare)
- PostgreSQL RDS (Aurora or plain) with automated backups
- Frontend served from CDN (Cloudflare Pages / Vercel / S3+CloudFront)
- Environment config via env vars, secrets via vault
- Health endpoints for monitoring (Spring Actuator)

---

## 10. Future-Proofing for Mobile

- API is fully RESTful, JSON-based — no GraphQL dependency required
- All list endpoints support pagination (`?page=1&size=50`)
- All monetary values in integer cents (no floating-point ambiguity)
- UUID primary keys (no sequential ID leakage, safe for offline ID generation)
- ETag / If-None-Match headers on GET endpoints for conditional requests
- Mobile-only fields (`device_token` for push notifications) added when needed
- API versioning via URL prefix (`/api/v1/`) allows co-existence of old mobile clients

---

## 11. Migration from MVP

The MVP stores everything in `localStorage`. A one-time import path:

1. Expose an endpoint `POST /api/v1/import/mvp` that accepts the MVP JSON
2. Backend maps: anonymous goal → authenticated user's goal, generates real IDs from slugs
3. Frontend provides "Import from MVP" option after registration
4. No Firebase migration needed (Firebase was optional and user-configured)
