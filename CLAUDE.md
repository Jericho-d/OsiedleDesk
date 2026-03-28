# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OsiedleDesk is a Jira-like issue tracking system for property management. It uses a Spring Boot 4.0.2 backend (Java 25) and a React 19 + Vite + TypeScript frontend, all orchestrated via Docker Compose.

## Commands

All development happens through Docker — no local Java or Node installation is required.

```bash
make dev             # Start full dev environment (postgres + backend + frontend with hot reload)
make dev-backend     # Start only postgres + backend
make dev-frontend    # Start only postgres + frontend
make test            # Run backend tests in Docker (auto-rollback DB)
make build           # Build backend Docker image
make run             # Build and start all services
make clean           # Stop all containers and remove volumes
make rollback        # Reset database to initial state
make logs            # View container logs
make shell-backend   # Open shell in backend container
make shell-frontend  # Open shell in frontend container
```

Frontend scripts (run inside container or with local Node):
```bash
npm run dev          # Vite dev server
npm run build        # TypeScript check + Vite build
npm run lint         # ESLint
```

Backend (run inside container or with local Java 25):
```bash
./gradlew test                              # Run all tests
./gradlew test --tests "*.IssueServiceTest" # Run single test class
./gradlew bootRun                           # Run dev server
```

## Architecture

```
Frontend (React 19 + Vite)
        │  HTTP/REST + session cookies
        ▼
Backend (Spring Boot 4.0.2)
        │  Spring Data JDBC
        ▼
PostgreSQL 15 (Liquibase migrations)
```

### Backend

Package root: `com.administrativetool`

The backend follows **CQRS** with domain events:
- `application/command/IssueCommandService` — write operations, publishes domain events
- `application/query/IssueQueryService` — read operations
- `application/handler/IssueCommandHandler` — listens for domain events, executes side effects (e.g. email sending)
- `service/` — orchestration services (`IssueService`, `IssueEmailService`, `EmailService` via Gmail API)
- `domain/model/` — aggregates (`Issue`, `User`, `Attachment`) and `Status` enum
- `domain/event/` — `IssueCreatedEvent`, `IssueUpdatedEvent`, `IssueDeletedEvent`
- `repository/` — Spring Data JDBC repositories
- `controller/api/` — REST controllers (`IssueController`, `AuthController`, `AttachmentController`)
- `config/` — security, CORS, async, Gmail OAuth2 configuration

Authentication is form-based (Spring Security sessions). CSRF is disabled. Roles: `ADMIN` and `USER`. Only ADMINs can change issue status or send issues via email.

Database migrations live in `backend/src/main/resources/db/changelog/changes/` and are managed by Liquibase.

Tests use JUnit 5 + Mockito with an H2 in-memory database (`TestDatabaseConfig.java`).

### Frontend

Entry: `frontend/src/main.tsx` → `App.tsx` (React Router routes)

```
src/
├── services/api.ts          # Typed fetch wrapper (all API calls go here)
├── hooks/useAuth.tsx        # Auth context (login state, current user)
├── hooks/useToast.tsx       # Toast notifications
├── components/              # Shared layout: AppShell, TopBar, Sidenav, Toast
├── types/index.ts           # Shared TypeScript interfaces
└── features/
    ├── auth/LoginPage.tsx
    └── board/               # Kanban board (main feature)
        ├── BoardPage.tsx    # Fetches issues, manages DnD state
        ├── BoardColumn.tsx  # Column with droppable area (@dnd-kit)
        ├── IssueCard.tsx    # Draggable issue card
        ├── IssueDetailModal.tsx   # View/edit issue, send email, manage attachments
        └── CreateIssueModal.tsx   # Create issue with file upload (multipart)
```

Styling uses **CSS Modules** (`.module.css`) per component. State management is local React hooks + Context API — no Redux or external state library.

## Default Dev Credentials

| Username | Password | Role  |
|----------|----------|-------|
| `admin`  | `admin`  | ADMIN |
| `user`   | `user`   | USER  |

## Key API Endpoints

| Endpoint | Method | Auth |
|----------|--------|------|
| `/api/auth/login` | POST | Public |
| `/api/auth/me` | GET | Any |
| `/api/issues` | GET, POST | USER+ |
| `/api/issues/{id}` | GET, PUT, DELETE | USER+ |
| `/api/issues/{id}/status` | POST | ADMIN |
| `/api/issues/{id}/send` | POST | ADMIN |
| `/api/attachments/{id}/download` | GET | USER+ |
| `/actuator/health` | GET | Public |
