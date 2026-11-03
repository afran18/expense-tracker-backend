# Expense & Approval Tracker: Backend

A microservices-based expense submission and multi-level approval system. Employees submit expenses, managers and finance approve them, and dashboards summarize spend and pending work.

The project is built as a hands-on exercise in **Spring Boot microservices, OAuth2/OIDC with Keycloak, Docker, Kubernetes (Helm + Istio), and CI/CD with GitHub Actions**.

> **Status:** 🚧 In progress. See the [Roadmap](#roadmap).

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Roles & Workflow](#roles--workflow)
- [Project Structure](#project-structure)
- [Services](#services)
- [Getting Started](#getting-started)
- [Conventions](#conventions)
- [Roadmap](#roadmap)

---

## Features

- OAuth2 / OpenID Connect login through Keycloak
- Role-based access control (Employee, Manager, Finance, Admin)
- Expense submission with categories and approval history
- Multi-step approval workflow with rejection support
- Dashboard with spend summaries and pending-approval counts
- Database migrations with Flyway
- Containerized services, deployable to Kubernetes with Helm and Istio

## Architecture

```
                    ┌──────────────┐
   Browser / FE ───►│ Istio Ingress│
                    │   Gateway    │
                    └──────┬───────┘
                           │  /api/*
        ┌──────────────────┼───────────────────┐
        ▼                  ▼                   ▼
 ┌──────────────┐  ┌───────────────┐  ┌──────────────────┐
 │ auth-service │  │expense-service│◄─│ dashboard-service│
 └──────┬───────┘  └───────┬───────┘  └──────────────────┘
        │                  │
        ▼                  ▼
 ┌──────────────┐   ┌─────────────┐
 │   Keycloak   │   │  Postgres   │
 └──────────────┘   └─────────────┘

 commons-module → shared jar used by every service
```

- The frontend and backend share one domain. All backend APIs live under `/api`, and everything else routes to the frontend.
- Services validate the JWT issued by Keycloak and read roles from its claims.
- `dashboard-service` reads data from `expense-service` through service-to-service calls.

## Tech Stack

| Area | Technology |
|---|---|
| Language / Framework | Java, Spring Boot, Spring Security (OAuth2 Resource Server / Client) |
| Identity | Keycloak (OIDC) |
| Database | PostgreSQL, Flyway |
| Build | Maven (multi-module) |
| Containers | Docker, Docker Compose |
| Orchestration | Kubernetes (kind locally), Helm, Istio |
| CI/CD | GitHub Actions |

> Exact versions are pinned in the parent `pom.xml`.

## Roles & Workflow

| Role | Capabilities |
|---|---|
| `EMPLOYEE` | Create, edit and submit own expenses; view own history |
| `MANAGER` | Approve or reject expenses submitted by their team |
| `FINANCE` | Give final approval and mark expenses as reimbursed |
| `ADMIN` | Manage users and categories |

**Expense lifecycle**

```
DRAFT → SUBMITTED → MANAGER_APPROVED → FINANCE_APPROVED → REIMBURSED
                 ↘ REJECTED (at either approval step)
```

## Project Structure

```
expense-tracker-backend/
├── .github/
│   └── workflows/
│       └── ci.yml                  # build, test, image push (added later)
├── .gitignore
├── README.md
├── docker-compose.yml              # local dev: Keycloak + Postgres
│
├── keycloak/                       # infra, not a Maven module
│   ├── Dockerfile
│   ├── realm-export/
│   │   └── expense-realm.json      # clients, roles, test users
│   └── themes/                     # optional custom login theme
│
└── microservices/
    ├── pom.xml                     # parent POM (packaging=pom)
    ├── commons-module/             # shared jar, not runnable
    ├── auth-service/               # OIDC flow with Keycloak
    ├── expense-service/            # core domain
    └── dashboard-service/          # aggregated, read-only stats
```

Each service follows the same internal layout:

```
<service>/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/com/yourname/expense/<service>/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/             # where a database is used
    │   ├── entity/ dto/ config/
    │   └── <Service>Application.java
    └── resources/
        ├── application.yml
        └── db/migration/           # Flyway scripts (expense-service)
```

## Services

| Module | Type | Responsibility |
|---|---|---|
| `commons-module` | Library (jar) | Shared DTOs, exception handling, JWT role converter, security utilities, constants |
| `auth-service` | Spring Boot | Handles the OAuth2/OIDC flow with Keycloak; exposes login, token and user-info endpoints |
| `expense-service` | Spring Boot | Expense CRUD, categories, approval workflow, approval history |
| `dashboard-service` | Spring Boot | Spend by category, pending approvals, summary stats; calls `expense-service` |
| `keycloak` | Infra | Keycloak container and realm configuration |

## Getting Started

> Setup instructions will be filled in as each piece is built.

**Prerequisites**

- JDK (version per parent POM)
- Maven
- Docker and Docker Compose
- Git

**Planned local flow**

```bash
# 1. Clone
git clone https://github.com/<your-username>/expense-tracker-backend.git
cd expense-tracker-backend

# 2. Copy env file and fill in values (never commit .env)
cp .env.example .env

# 3. Start Keycloak and Postgres
docker compose up -d

# 4. Build all modules
cd microservices
mvn clean install

# 5. Run a service
cd auth-service
mvn spring-boot:run
```

## Conventions

- **API base path:** all backend endpoints are served under `/api`. The decision on whether the app (`server.servlet.context-path`) or the Istio VirtualService owns this prefix will be documented here once made.
- **Branching:** feature branches (`feature/<name>`) merged through pull requests into `main`.
- **Commits:** short, descriptive messages, e.g. `feat: add parent pom`, `fix: context path in auth-service`.
- **Secrets:** never committed. Use environment variables and a git-ignored `.env`; keep a `.env.example` with placeholder values.
- **Database:** one schema per service; all changes via Flyway migrations.

## Roadmap

**Phase 1: Backend**
- [ ] Repo setup, `.gitignore`, README
- [ ] Parent POM and `commons-module`
- [ ] `docker-compose.yml` with Keycloak and Postgres
- [ ] Keycloak realm export (clients, roles, test users)
- [ ] `auth-service`: OIDC login flow
- [ ] `expense-service`: CRUD, then approval workflow
- [ ] `dashboard-service`: summaries and stats
- [ ] Dockerfile for each service

**Phase 2: Frontend** (separate repo)
- [ ] Login via auth-service
- [ ] Expense forms and approval screens
- [ ] Dashboard

**Phase 3: Kubernetes** (separate infra repo)
- [ ] Local cluster with kind
- [ ] Deployments, Services, ConfigMaps, Secrets
- [ ] Helm charts and values files per service
- [ ] Istio Gateway and VirtualServices (`/api` → backend, `/` → frontend)
- [ ] Keycloak in-cluster

**Phase 4: CI/CD**
- [ ] GitHub Actions: build, test, push images to GHCR
- [ ] Chart validation on a throwaway kind cluster
- [ ] Deployment to local kind (self-hosted runner or Argo CD)
- [ ] Optional: short-lived EKS deployment

## License
