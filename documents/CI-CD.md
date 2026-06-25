# Waypoint — CI/CD Plan

Monorepo · GitHub Actions · Self-Hosted Runner · Docker · Cloudflare Tunnel

---

## 1. Infrastructure Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         GitHub                                      │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    waypoint/waypoint                          │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │   │
│  │  │   api/   │  │   web/   │  │   ios/   │  │ android/ │     │   │
│  │  │ Spring   │  │  React   │  │  (future)│  │ (future) │     │   │
│  │  │   Boot   │  │  + Vite  │  │          │  │          │     │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘     │   │
│  │  ┌──────────────────────────────────────────────────────┐    │   │
│  │  │  .github/workflows/                                  │    │   │
│  │  │  ├── ci.yml              (PR checks)                  │    │   │
│  │  │  ├── deploy-dev.yml      (push to develop)           │    │   │
│  │  │  ├── deploy-staging.yml  (tag v*.*.*-rc*)            │    │   │
│  │  │  ├── deploy-prod.yml     (tag v*.*.*)                │    │   │
│  │  │  └── cleanup.yml         (housekeeping)              │    │   │
│  │  └──────────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
             push / PR          tag v*.*.*     tag v*.*.*-rc*
                    │               │               │
                    ▼               ▼               ▼
          ┌─────────────────────────────────────────────┐
          │         GitHub Actions (runs on...)          │
          └─────────────────────┬───────────────────────┘
                                │ triggers
                                ▼
          ┌─────────────────────────────────────────────┐
          │     Self-Hosted Runner (on-premise server)   │
          │     ┌─────────────────────────────────────┐  │
          │     │  Linux (Ubuntu 22.04 / 24.04)       │  │
          │     │  - Docker                          │  │
          │     │  - GitHub Actions Runner           │  │
          │     │  - Docker Compose                  │  │
          │     │  - cloudflared                     │  │
          │     └─────────────────────────────────────┘  │
          └─────────────────────┬───────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │                       │
                    ▼                       ▼
          ┌──────────────────┐   ┌──────────────────┐
          │   Docker Compose  │   │  Docker Compose  │
          │   (api + db)      │   │  (nginx static)  │
          │   :8080           │   │  :3000           │
          └──────────────────┘   └──────────────────┘
                    │                       │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │   cloudflared tunnel  │
                    │   waypoint.example.com│
                    └──────────────────────┘
                                │
                                ▼
                          Internet
```

### Components

| Component | Role |
|---|---|
| GitHub | Source control, PR review, Actions orchestration |
| Self-Hosted Runner | On-prem Linux server executing workflows + hosting apps |
| Docker | Containerization for api, web, db |
| Docker Compose | Multi-container orchestration on the server |
| PostgreSQL | Database on the same server (or separate if needed) |
| Cloudflare Tunnel | Secure public exposure without opening firewall ports |
| cloudflared | Cloudflare Tunnel client running on the server |

---

## 2. Repository Structure

```
waypoint/
├── .github/
│   ├── actions/                        # Reusable composite actions
│   │   ├── setup-java/action.yml
│   │   ├── setup-node/action.yml
│   │   └── docker-build-push/action.yml
│   └── workflows/
│       ├── ci.yml                      # PR checks (api + web)
│       ├── deploy-dev.yml              # Push to develop
│       ├── deploy-staging.yml          # Release candidate tag
│       ├── deploy-prod.yml             # Release tag
│       └── cleanup.yml                 # Scheduled docker prune + log rotate
├── api/                                # Spring Boot backend
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/
├── web/                                # React frontend
│   ├── Dockerfile
│   ├── package.json
│   └── src/
├── deploy/                             # Deployment artifacts
│   ├── docker-compose.yml              # Shared compose (dev/staging/prod)
│   ├── docker-compose.dev.yml          # Dev overrides
│   ├── docker-compose.staging.yml      # Staging overrides
│   ├── docker-compose.prod.yml         # Prod overrides
│   ├── nginx/
│   │   └── default.conf                # Reverse proxy config
│   ├── cloudflared/
│   │   └── config.yml                  # Tunnel config
│   └── scripts/
│       ├── deploy.sh                   # Deploy helper
│       ├── rollback.sh                 # Rollback to previous version
│       └── seed-db.sh                  # DB seeding for dev
├── .github/workflows/                  # (shown above)
├── docker-compose.yml                  # Root-level for local dev
└── README.md
```

---

## 3. GitHub Actions Workflows

### 3.1 CI — Pull Request Checks (`ci.yml`)

```yaml
name: CI
on:
  pull_request:
    branches: [develop, main]
    paths:
      - 'api/**'
      - 'web/**'

jobs:
  api-lint-test-build:
    name: API — Lint, Test, Build
    runs-on: self-hosted
    defaults:
      run:
        working-directory: ./api
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Lint
        run: ./gradlew checkstyleMain checkstyleTest

      - name: Test
        run: ./gradlew test

      - name: Build
        run: ./gradlew bootJar

      - name: Docker build (smoke test)
        run: docker build -t waypoint-api:ci --build-arg JAR_FILE=build/libs/*.jar .

  web-lint-test-build:
    name: Web — Lint, Typecheck, Test, Build
    runs-on: self-hosted
    defaults:
      run:
        working-directory: ./web
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: 'npm'
          cache-dependency-path: ./web/package-lock.json

      - name: Install
        run: npm ci

      - name: Lint
        run: npm run lint

      - name: Typecheck
        run: npm run typecheck

      - name: Test
        run: npm run test -- --run

      - name: Build
        run: npm run build

      - name: Docker build (smoke test)
        run: docker build -t waypoint-web:ci .
```

### 3.2 Deploy — Development (`deploy-dev.yml`)

**Trigger**: Push to `develop` branch

```yaml
name: Deploy Dev
on:
  push:
    branches: [develop]
    paths:
      - 'api/**'
      - 'web/**'
      - 'deploy/**'

concurrency:
  group: deploy-dev
  cancel-in-progress: true

jobs:
  api:
    name: Build & Deploy API
    runs-on: self-hosted
    defaults:
      run:
        working-directory: ./api
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Test
        run: ./gradlew test

      - name: Build JAR
        run: ./gradlew bootJar

      - name: Build & push Docker image
        run: |
          docker build \
            -t waypoint-api:dev \
            -t waypoint-api:dev-${GITHUB_SHA::7} \
            --build-arg JAR_FILE=build/libs/*.jar \
            .
          docker tag waypoint-api:dev localhost:5000/waypoint-api:dev
          docker tag waypoint-api:dev-${GITHUB_SHA::7} localhost:5000/waypoint-api:dev-${GITHUB_SHA::7}
          docker push localhost:5000/waypoint-api:dev
          docker push localhost:5000/waypoint-api:dev-${GITHUB_SHA::7}

      - name: Deploy
        run: |
          docker service update --image localhost:5000/waypoint-api:dev waypoint_api \
          || docker stack deploy -c ../deploy/docker-compose.yml -c ../deploy/docker-compose.dev.yml waypoint
        working-directory: ./deploy

  web:
    name: Build & Deploy Web
    runs-on: self-hosted
    defaults:
      run:
        working-directory: ./web
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: 'npm'
          cache-dependency-path: ./web/package-lock.json

      - name: Install
        run: npm ci

      - name: Lint & typecheck
        run: npm run lint && npm run typecheck

      - name: Build
        run: npm run build
        env:
          VITE_API_BASE_URL: https://dev.waypoint.example.com/api/v1

      - name: Build & push Docker image
        run: |
          docker build \
            -t waypoint-web:dev \
            -t waypoint-web:dev-${GITHUB_SHA::7} \
            .
          docker tag waypoint-web:dev localhost:5000/waypoint-web:dev
          docker tag waypoint-web:dev-${GITHUB_SHA::7} localhost:5000/waypoint-web:dev-${GITHUB_SHA::7}
          docker push localhost:5000/waypoint-web:dev
          docker push localhost:5000/waypoint-web:dev-${GITHUB_SHA::7}

      - name: Deploy
        run: |
          docker service update --image localhost:5000/waypoint-web:dev waypoint_web \
          || docker stack deploy -c ../deploy/docker-compose.yml -c ../deploy/docker-compose.dev.yml waypoint
        working-directory: ./deploy
```

### 3.3 Deploy — Staging (`deploy-staging.yml`)

**Trigger**: Tag matching `v*.*.*-rc*` (e.g., `v1.2.0-rc1`)

```yaml
name: Deploy Staging
on:
  push:
    tags:
      - 'v[0-9]+.[0-9]+.[0-9]+-rc*'

jobs:
  api:
    name: Build & Deploy API to Staging
    runs-on: self-hosted
    environment: staging
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Test
        run: ./gradlew test
        working-directory: ./api

      - name: Security scan (dependencies)
        run: ./gradlew dependencyCheckAnalyze
        working-directory: ./api

      - name: Build JAR
        run: ./gradlew bootJar
        working-directory: ./api

      - name: Build & push Docker image
        run: |
          docker build \
            -t waypoint-api:staging \
            -t waypoint-api:${GITHUB_REF_NAME} \
            --build-arg JAR_FILE=build/libs/*.jar \
            .
          docker tag waypoint-api:staging localhost:5000/waypoint-api:staging
          docker tag waypoint-api:${GITHUB_REF_NAME} localhost:5000/waypoint-api:${GITHUB_REF_NAME}
          docker push localhost:5000/waypoint-api:staging
          docker push localhost:5000/waypoint-api:${GITHUB_REF_NAME}
        working-directory: ./api

      - name: Run DB migration (staging)
        run: |
          docker run --rm \
            --network waypoint_default \
            -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/waypoint_staging \
            -e SPRING_DATASOURCE_USERNAME=waypoint \
            -e SPRING_DATASOURCE_PASSWORD=${{ secrets.STAGING_DB_PASSWORD }} \
            localhost:5000/waypoint-api:staging \
            java -jar app.jar --spring.flyway.enabled=true --spring.flyway.baseline-on-migrate=true

      - name: Deploy
        run: |
          docker service update \
            --image localhost:5000/waypoint-api:staging \
            --with-registry-auth \
            waypoint_staging_api

  web:
    name: Build & Deploy Web to Staging
    runs-on: self-hosted
    environment: staging
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: 'npm'

      - name: Install
        run: npm ci
        working-directory: ./web

      - name: Lint & typecheck
        run: npm run lint && npm run typecheck
        working-directory: ./web

      - name: Build
        run: npm run build
        working-directory: ./web
        env:
          VITE_API_BASE_URL: https://staging.waypoint.example.com/api/v1

      - name: Build & push Docker image
        run: |
          docker build \
            -t waypoint-web:staging \
            -t waypoint-web:${GITHUB_REF_NAME} \
            .
          docker tag waypoint-web:staging localhost:5000/waypoint-web:staging
          docker tag waypoint-web:${GITHUB_REF_NAME} localhost:5000/waypoint-web:${GITHUB_REF_NAME}
          docker push localhost:5000/waypoint-web:staging
          docker push localhost:5000/waypoint-web:${GITHUB_REF_NAME}
        working-directory: ./web

      - name: Deploy
        run: |
          docker service update \
            --image localhost:5000/waypoint-web:staging \
            --with-registry-auth \
            waypoint_staging_web
```

### 3.4 Deploy — Production (`deploy-prod.yml`)

**Trigger**: Tag matching `v*.*.*` (e.g., `v1.2.0`)

```yaml
name: Deploy Production
on:
  push:
    tags:
      - 'v[0-9]+.[0-9]+.[0-9]+'
      - 'v[0-9]+.[0-9]+.[0-9]+.[0-9]+'   # hotfix

concurrency:
  group: deploy-prod
  cancel-in-progress: false

jobs:
  validate:
    name: Validate Tag & Changelog
    runs-on: self-hosted
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Check tag matches CHANGELOG
        run: |
          if ! grep -q "## ${GITHUB_REF_NAME#v}" CHANGELOG.md; then
            echo "❌ CHANGELOG.md missing entry for ${GITHUB_REF_NAME}"
            exit 1
          fi

  api:
    name: Build, Scan, Deploy API
    runs-on: self-hosted
    environment: production
    needs: [validate]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Full test suite
        run: ./gradlew test integrationTest
        working-directory: ./api

      - name: Security scan
        run: ./gradlew dependencyCheckAnalyze
        working-directory: ./api
        continue-on-error: true  # Don't block deploy but flag issues

      - name: Build JAR
        run: ./gradlew bootJar
        working-directory: ./api

      - name: Build production Docker image
        run: |
          docker build \
            -t waypoint-api:prod \
            -t waypoint-api:${GITHUB_REF_NAME} \
            --build-arg JAR_FILE=build/libs/*.jar \
            .
          docker tag waypoint-api:prod localhost:5000/waypoint-api:prod
          docker tag waypoint-api:${GITHUB_REF_NAME} localhost:5000/waypoint-api:${GITHUB_REF_NAME}
          docker push localhost:5000/waypoint-api:prod
          docker push localhost:5000/waypoint-api:${GITHUB_REF_NAME}
        working-directory: ./api

      - name: Run DB migration (prod)
        run: |
          docker run --rm \
            --network waypoint_prod_default \
            -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/waypoint_prod \
            -e SPRING_DATASOURCE_USERNAME=${{ secrets.PROD_DB_USER }} \
            -e SPRING_DATASOURCE_PASSWORD=${{ secrets.PROD_DB_PASSWORD }} \
            localhost:5000/waypoint-api:prod \
            java -jar app.jar --spring.flyway.enabled=true --spring.flyway.baseline-on-migrate=true

      - name: Deploy (rolling update)
        run: |
          docker service update \
            --image localhost:5000/waypoint-api:prod \
            --with-registry-auth \
            --update-parallelism 1 \
            --update-delay 10s \
            --rollback-monitor 30s \
            waypoint_prod_api

      - name: Health check
        run: |
          for i in $(seq 1 12); do
            STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/actuator/health || true)
            if [ "$STATUS" = "200" ]; then
              echo "✅ API healthy"
              exit 0
            fi
            echo "Waiting... ($i/12)"
            sleep 5
          done
          echo "❌ API health check failed"
          exit 1

  web:
    name: Build & Deploy Web
    runs-on: self-hosted
    environment: production
    needs: [validate]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: 'npm'

      - name: Install
        run: npm ci
        working-directory: ./web

      - name: Build
        run: npm run build
        working-directory: ./web
        env:
          VITE_API_BASE_URL: https://waypoint.example.com/api/v1

      - name: Build production Docker image
        run: |
          docker build \
            -t waypoint-web:prod \
            -t waypoint-web:${GITHUB_REF_NAME} \
            .
          docker tag waypoint-web:prod localhost:5000/waypoint-web:prod
          docker tag waypoint-web:${GITHUB_REF_NAME} localhost:5000/waypoint-web:${GITHUB_REF_NAME}
          docker push localhost:5000/waypoint-web:prod
          docker push localhost:5000/waypoint-web:${GITHUB_REF_NAME}
        working-directory: ./web

      - name: Deploy (rolling update)
        run: |
          docker service update \
            --image localhost:5000/waypoint-web:prod \
            --with-registry-auth \
            --update-parallelism 1 \
            --update-delay 5s \
            waypoint_prod_web

      - name: Health check (web)
        run: |
          for i in $(seq 1 6); do
            STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 || true)
            if [ "$STATUS" = "200" ]; then
              echo "✅ Web healthy"
              exit 0
            fi
            sleep 5
          done
          echo "❌ Web health check failed"
          exit 1

  github-release:
    name: Create GitHub Release
    runs-on: self-hosted
    needs: [api, web]
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
      - name: Create Release
        uses: softprops/action-gh-release@v2
        with:
          generate_release_notes: true
          make_latest: true
```

### 3.5 Cleanup (`cleanup.yml`)

```yaml
name: Cleanup
on:
  schedule:
    - cron: '0 3 * * 0'   # Every Sunday at 3 AM
  workflow_dispatch:       # Manual trigger

jobs:
  docker-prune:
    runs-on: self-hosted
    steps:
      - name: Prune unused Docker resources
        run: |
          docker system prune -af --filter "until=168h"  # 7 days

      - name: Remove old images (keep last 10 per service)
        run: |
          for IMAGE in waypoint-api waypoint-web; do
            for TAG in dev staging prod; do
              KEPT=$(docker image ls --format '{{.Tag}}' "localhost:5000/${IMAGE}:*${TAG}*" | head -10)
              docker image ls --format '{{.Repository}}:{{.Tag}}' "localhost:5000/${IMAGE}:*${TAG}*" | \
                grep -v -E "$(echo "$KEPT" | tr '\n' '|')" | \
                xargs -r docker rmi 2>/dev/null || true
            done
          done

  log-rotate:
    runs-on: self-hosted
    steps:
      - name: Rotate Docker container logs
        run: |
          docker ps -q | xargs -r -I{} sh -c 'docker logs {} 2>&1 | tail -1000 > /var/log/docker/{}.log 2>/dev/null; truncate -s 0 $(docker inspect --format="{{.LogPath}}" {}) 2>/dev/null' || true
```

---

## 4. Self-Hosted Runner Setup

### Server Requirements

| Spec | Minimum | Recommended |
|---|---|---|
| CPU | 4 cores | 8 cores |
| RAM | 8 GB | 16 GB |
| Storage | 80 GB SSD | 160 GB SSD |
| OS | Ubuntu 22.04 LTS | Ubuntu 24.04 LTS |
| Network | 100 Mbps | 1 Gbps |

### Provisioning Script

```bash
#!/usr/bin/env bash
set -euo pipefail

# --- Docker ---
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# --- Docker Compose plugin ---
sudo apt-get update
sudo apt-get install -y docker-compose-plugin

# --- GitHub Actions Runner ---
mkdir -p /opt/actions-runner && cd /opt/actions-runner
curl -o actions-runner-linux-x64-2.320.0.tar.gz \
  -L https://github.com/actions/runner/releases/download/v2.320.0/actions-runner-linux-x64-2.320.0.tar.gz
tar xzf actions-runner-linux-x64-2.320.0.tar.gz
sudo ./bin/installdependencies.sh

# Register runner (interactive, or use --url --token from GitHub)
./config.sh --url https://github.com/waypoint/waypoint --token <REGISTRATION_TOKEN> --labels self-hosted,linux --work /opt/actions-runner/_work

# Install as service
sudo ./svc.sh install
sudo ./svc.sh start

# --- Local Docker Registry (for on-prem image storage) ---
docker run -d \
  --name registry \
  --restart always \
  -p 127.0.0.1:5000:5000 \
  -v registry_data:/var/lib/registry \
  registry:2

# --- Cloudflare Tunnel (cloudflared) ---
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /usr/local/bin/cloudflared
chmod +x /usr/local/bin/cloudflared

# Authenticate (requires browser login)
cloudflared tunnel login

# Create and configure tunnel
cloudflared tunnel create waypoint
# This creates credentials file at ~/.cloudflared/<tunnel-id>.json

# Configure DNS
cloudflared tunnel route dns waypoint waypoint.example.com
cloudflared tunnel route dns waypoint dev.waypoint.example.com
cloudflared tunnel route dns waypoint staging.waypoint.example.com

# --- cloudflared systemd service ---
cat > /etc/systemd/system/cloudflared-waypoint.service << 'EOF'
[Unit]
Description=Cloudflare Tunnel for Waypoint
Wants=network-online.target
After=network-online.target

[Service]
Type=simple
User=root
ExecStart=/usr/local/bin/cloudflared tunnel run waypoint
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable --now cloudflared-waypoint

# --- Firewall ---
ufw allow OpenSSH
ufw enable
```

### Runner Labels

| Label | Purpose |
|---|---|
| `self-hosted` | Target all on-prem workflows |
| `linux` | Platform |
| `dev` | Dev environment workflows |
| `staging` | Staging environment workflows |
| `prod` | Production environment workflows |

---

## 5. Docker Compose Configuration

### `deploy/docker-compose.yml` (shared base)

```yaml
services:
  api:
    image: ${API_IMAGE:-waypoint-api:latest}
    restart: unless-stopped
    networks:
      - waypoint
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-dev}
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/${DB_NAME:-waypoint}
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-waypoint}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
    depends_on:
      db:
        condition: service_healthy
    deploy:
      replicas: 2
      update_config:
        parallelism: 1
        delay: 10s
        order: start-first

  web:
    image: ${WEB_IMAGE:-waypoint-web:latest}
    restart: unless-stopped
    networks:
      - waypoint
    ports:
      - "3000:80"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:80"]
      interval: 30s
      timeout: 5s
      retries: 3
    deploy:
      replicas: 1
      update_config:
        parallelism: 1
        delay: 5s

  db:
    image: postgres:16-alpine
    restart: unless-stopped
    networks:
      - waypoint
    volumes:
      - pgdata:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: ${DB_NAME:-waypoint}
      POSTGRES_USER: ${DB_USER:-waypoint}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-waypoint}"]
      interval: 10s
      timeout: 5s
      retries: 5

networks:
  waypoint:
    driver: overlay

volumes:
  pgdata:
```

### `deploy/docker-compose.dev.yml` (dev overrides)

```yaml
services:
  api:
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
    deploy:
      replicas: 1

  db:
    ports:
      - "5432:5432"

  web:
    ports:
      - "3000:80"
```

### `deploy/docker-compose.prod.yml` (prod overrides)

```yaml
services:
  api:
    ports:
      - "127.0.0.1:8080:8080"    # Only local — cloudflared handles public access
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

  web:
    deploy:
      replicas: 2

  db:
    deploy:
      resources:
        limits:
          memory: 1G
    volumes:
      - pgdata_prod:/var/lib/postgresql/data
```

---

## 6. Dockerfiles

### `api/Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS base
RUN adduser -D waypoint
USER waypoint
WORKDIR /app

FROM base AS final
ARG JAR_FILE
COPY --chown=waypoint:waypoint build/libs/${JAR_FILE} app.jar
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v1/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### `web/Dockerfile`

```dockerfile
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:80 || exit 1
```

### `deploy/nginx/default.conf`

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_types text/css application/javascript image/svg+xml;

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache-busted assets (vite generates hashed filenames)
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Security headers
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
}
```

---

## 7. Cloudflare Tunnel Configuration

### `deploy/cloudflared/config.yml`

```yaml
tunnel: <tunnel-id>
credentials-file: /home/waypoint/.cloudflared/<tunnel-id>.json

ingress:
  # Production
  - hostname: waypoint.example.com
    service: http://localhost:3000
    originRequest:
      noTLSVerify: true

  # API subdomain (for direct API access, if needed)
  - hostname: api.waypoint.example.com
    service: http://localhost:8080
    originRequest:
      noTLSVerify: true

  # Development
  - hostname: dev.waypoint.example.com
    service: http://localhost:3000
    originRequest:
      noTLSVerify: true

  # Staging
  - hostname: staging.waypoint.example.com
    service: http://localhost:3000
    originRequest:
      noTLSVerify: true

  # Catch-all
  - service: http_status:404
```

### Cloudflare Dashboard Settings

| Setting | Value |
|---|---|
| SSL/TLS | Full (strict) — requires a valid origin cert or Cloudflare origin CA |
| Always Use HTTPS | On |
| Auto Minify | JavaScript, CSS, HTML |
| Brotli | On |
| HTTP/2 | On |
| HTTP/3 (QUIC) | On |
| Cache Level | Standard |
| Firewall | Rate limiting rules on `/api/v1/auth/*` (max 10 req/min per IP) |

---

## 8. Environment Strategy

| Environment | Branch | Tag Pattern | URL | DB Name | Replicas | Rollout |
|---|---|---|---|---|---|---|
| Development | `develop` | — | `dev.waypoint.example.com` | `waypoint_dev` | 1 api, 1 web | Immediate |
| Staging | — | `v*.*.*-rc*` | `staging.waypoint.example.com` | `waypoint_staging` | 1 api, 1 web | Manual approval |
| Production | `main` | `v*.*.*` | `waypoint.example.com` | `waypoint_prod` | 3 api, 2 web | Release tag + health checks |

### Environment Variables & Secrets

| Variable | Dev | Staging | Prod | Source |
|---|---|---|---|---|
| `DB_PASSWORD` | ✅ | ✅ | ✅ | GitHub Actions Secret |
| `JWT_SECRET` | dev-only value | ✅ | ✅ | GitHub Actions Secret |
| `VITE_API_BASE_URL` | dev URL | staging URL | prod URL | `env` in workflow |
| `SPRING_PROFILES_ACTIVE` | `dev` | `staging` | `prod` | Docker Compose env |

---

## 9. Quality Gates

### Pull Request Merge Requirements

| Gate | Required | Blocking |
|---|---|---|
| API lint (checkstyle) | ✅ | ✅ |
| API tests pass | ✅ | ✅ |
| Web lint (ESLint) | ✅ | ✅ |
| Web typecheck (TypeScript) | ✅ | ✅ |
| Web tests pass | ✅ | ✅ |
| API + Web Docker build succeeds | ✅ | ✅ |
| At least 1 reviewer approval | ✅ | ✅ |
| Branch up to date with target | ✅ | ✅ |

### Pre-Deploy Gates (Staging → Prod)

- Tag must match semantic versioning (`vX.Y.Z`)
- CHANGELOG.md must contain entry for the version
- All integration tests pass
- Dependency security scan passes (non-blocking, alerts only)
- Manual approval via GitHub Environments (Staging → Prod)

---

## 10. Rollback Strategy

### Automated Rollback (Health Check Failure)

Docker Swarm rollback is configured in the deploy workflow:

```yaml
--rollback-monitor 30s
```

If the new container fails health checks within 30 seconds, Swarm automatically reverts to the previous image.

### Manual Rollback (Script)

```bash
#!/usr/bin/env bash
# deploy/scripts/rollback.sh
set -euo pipefail

SERVICE="$1"      # api or web
ENVIRONMENT="$2"  # dev, staging, prod

# Find previous stable image
PREVIOUS=$(docker service inspect "waypoint_${ENVIRONMENT}_${SERVICE}" \
  --format '{{.Spec.TaskTemplate.ContainerSpec.Image}}' | \
  sed 's/@.*//' | sed 's/:.*//')

# Deploy previous version
docker service update \
  --image "${PREVIOUS}:prev" \
  --rollback \
  "waypoint_${ENVIRONMENT}_${SERVICE}"

echo "✅ Rolled back ${SERVICE} in ${ENVIRONMENT}"
```

---

## 11. Monitoring & Observability

| Tool | Purpose | Runs |
|---|---|---|
| Spring Actuator | API health, metrics, info endpoint | In API container |
| Docker logs | `journalctl -u docker` + `docker logs` | On server |
| Uptime Kuma (optional) | External uptime monitoring via Cloudflare Tunnel | Separate container |
| Cloudflare Analytics | Request volume, cache hit rate, security events | Cloudflare dashboard |

### Healthcheck Endpoints

```
GET /api/v1/actuator/health   → {"status":"UP"}
GET /api/v1/actuator/info     → {"version":"1.2.0","environment":"prod"}
```

---

## 12. Workflow Diagram (Summary)

```
                        ┌──────────────┐
                        │  Developer    │
                        │  pushes code  │
                        └──────┬───────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
               feature/*              develop
                    │                     │
                    ▼                     ▼
            ┌──────────────┐    ┌──────────────────┐
            │  Open PR      │    │ Deploy Dev        │
            │  → CI checks  │    │ → Build & push    │
            │  → Review     │    │ → Update service  │
            │  → Merge      │    │ → dev.*.com live  │
            └──────┬───────┘    └──────────────────┘
                   │
                   ▼
               develop
                   │
                   ▼
            ┌──────────────────┐
            │  Tag v*.*.*-rc*  │
            │  → Deploy Staging│
            │  → Manual verify │
            └──────┬───────────┘
                   │
                   ▼ (approve)
            ┌──────────────────┐
            │  Tag v*.*.*      │
            │  → Run migrations│
            │  → Deploy Prod   │
            │  → Health check  │
            │  → GitHub Release│
            └──────────────────┘
```
