# Implementation Plan: Issue Tracker for Property Management

**Branch**: `001-issue-tracker` | **Date**: 2026-02-07 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/001-issue-tracker/spec.md`

## Summary

Build a Jira-like issue tracker for property management with the following key characteristics:
- **Board view**: Kanban-style columns (PREPARED → IN_PROGRESS → ACKNOWLEDGED → RESOLVED)
- **Email integration**: Admin clicks "Send" to email issue details to administrative company
- **Authentication**: All users must login; role-based access (USER vs ADMINISTRATOR)
- **Lightweight**: Runs on 4GB RAM / 8GB disk machine
- **Tech stack**: Java 25, Spring Boot 4.0.2, HTMX + Thymeleaf, PostgreSQL

**Technical approach**: Server-side rendering with HTMX for progressive enhancement - no heavy JavaScript frameworks, minimal resource footprint.

---

## Technical Context

**Language/Version**: Java 25 (LTS)  
**Primary Framework**: Spring Boot 4.0.2  
**Build Tool**: Gradle 9.x with Kotlin DSL support  

**Storage**: PostgreSQL 16+ with Spring Data JDBC  
**Frontend**: Thymeleaf 3.2.x (server-side rendering) + HTMX 2.0.x (14KB progressive enhancement)  
**Email**: JavaMailSender with Spring Boot Mail Starter (async sending with retry)  
**Security**: Spring Security 6.x with BCrypt password encoding  
**Validation**: Jakarta Bean Validation  
**Testing**: JUnit 5 + AssertJ + Spring Boot Test + GreenMail  
**Additional**: Lombok (boilerplate reduction), htmx-spring-boot (integration library)

**Target Platform**: Linux server with 4GB RAM, 8GB disk (also runs on Windows/macOS for dev)  
**Project Type**: Web application with server-side rendering  
**Performance Goals**: 
- Page load <3 seconds on 4GB RAM machine
- API response <200ms for 95th percentile
- Email delivery <30 seconds (95% success rate with retry)

**Constraints**:
- Frontend bundle must be <500KB (achieved: ~14KB HTMX only)
- No Node.js/npm build pipeline
- No heavy JavaScript frameworks
- Must work on resource-constrained machine

**Scale/Scope**:
- Up to 1000 active issues
- Single residential area deployment
- Moderate concurrent users (single property management)
- Single server (no clustering required)

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution file is a template in this project. Based on the AGENTS.md guidelines and standard practices:

### Gates Analysis

| Gate | Requirement | Status | Notes |
|------|-------------|--------|-------|
| **No implementation details in spec** | Spec must be technology-agnostic | ✅ PASS | Spec uses business language only |
| **Test-First Development** | JUnit 5 + AssertJ required | ✅ PASS | Matches AGENTS.md requirements |
| **Security Best Practices** | Form auth, BCrypt, no plaintext | ✅ PASS | Spec requires role-based auth |
| **Integration Testing** | Required for external deps | ✅ PASS | Email functionality needs integration tests |
| **Resource Constraints** | Must run on 4GB/8GB | ✅ PASS | HTMX+Thymeleaf chosen specifically for this |

### Re-check After Phase 1 Design

| Design Decision | Constitution Alignment | Notes |
|-----------------|------------------------|-------|
| Spring Data JDBC over JPA | ✅ Aligned | Lighter weight, better for simple CRUD |
| HTMX over React/Vue | ✅ Aligned | Minimal JS, no build step, fits constraints |
| Async email with @Async | ✅ Aligned | Non-blocking HTTP, better UX |
| Server-side rendering | ✅ Aligned | No API/frontend split complexity |

**Result**: ✅ ALL GATES PASS - No violations to justify

---

## Project Structure

### Documentation (this feature)

```text
specs/001-issue-tracker/
├── spec.md              # Feature specification (business requirements)
├── plan.md              # This file - implementation plan
├── research.md          # Phase 0 - Technology research and decisions
├── data-model.md        # Phase 1 - Database schema and entities
├── quickstart.md        # Phase 1 - Setup and development guide
├── contracts/           # Phase 1 - API contracts and specifications
│   └── api.md          # REST API and HTMX endpoint documentation
└── tasks.md             # Phase 2 - Implementation tasks (created later)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/administrativetool/
│   │   ├── AdministrativeToolApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java          # Spring Security configuration
│   │   │   ├── EmailConfig.java             # JavaMailSender configuration
│   │   │   ├── AsyncConfig.java             # @Async thread pool config
│   │   │   └── WebConfig.java               # CORS, interceptors
│   │   ├── controller/
│   │   │   ├── IssueController.java         # REST API endpoints
│   │   │   ├── BoardController.java         # Thymeleaf view endpoints
│   │   │   ├── AuthController.java          # Login/register views
│   │   │   └── FragmentController.java      # HTMX fragment endpoints
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Issue.java               # Issue entity
│   │   │   │   ├── User.java                # User entity
│   │   │   │   ├── Status.java              # Status enum
│   │   │   │   └── Priority.java            # Priority enum
│   │   │   └── dto/
│   │   │       ├── IssueCreateRequest.java
│   │   │       ├── IssueUpdateRequest.java
│   │   │       └── EmailSendResponse.java
│   │   ├── repository/
│   │   │   ├── IssueRepository.java         # Spring Data JDBC
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   ├── IssueService.java            # Issue business logic
│   │   │   ├── IssueEmailService.java       # Idempotent email sending
│   │   │   ├── EmailService.java            # Async email delivery
│   │   │   └── UserService.java             # User management
│   │   ├── security/
│   │   │   ├── CustomUserDetailsService.java
│   │   │   └── Role.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       └── ResourceNotFoundException.java
│   └── resources/
│       ├── templates/                       # Thymeleaf templates
│       │   ├── layout/
│       │   │   └── main.html               # Base layout
│       │   ├── board/
│       │   │   └── index.html              # Kanban board view
│       │   ├── fragments/
│       │   │   ├── issue-card.html         # Issue card fragment
│       │   │   └── column.html             # Board column fragment
│       │   ├── issues/
│       │   │   ├── list.html
│       │   │   ├── detail.html
│       │   │   └── form.html
│       │   └── auth/
│       │       ├── login.html
│       │       └── register.html
│       ├── static/
│       │   └── js/
│       │       └── htmx.min.js            # Or via CDN
│       ├── application.properties
│       └── application-local.properties   # Gitignored - local config
└── test/
    ├── java/com/administrativetool/
    │   ├── AdministrativeToolApplicationTests.java
    │   ├── controller/
    │   │   ├── IssueControllerTest.java
    │   │   └── IssueControllerWebTest.java
    │   ├── service/
    │   │   ├── IssueServiceTest.java
    │   │   └── EmailServiceTest.java
    │   └── integration/
    │       └── EmailIntegrationTest.java    # GreenMail tests
    └── resources/
        └── application-test.properties
```

**Structure Decision**: Single Spring Boot web application with server-side rendering using Thymeleaf + HTMX. No separate frontend project or build pipeline. This keeps the architecture simple and minimizes resource usage as required by the 4GB RAM constraint.

---

## Phase 0: Research Summary

Completed: 2026-02-07

### Key Decisions

| Area | Decision | Rationale |
|------|----------|-----------|
| **Frontend** | HTMX + Thymeleaf | 14KB, no build step, excellent Spring Boot integration via htmx-spring-boot |
| **Data Access** | Spring Data JDBC | 20-30% better performance than JPA for simple CRUD, lower memory footprint |
| **Email** | JavaMailSender + @Async | Standard Spring Boot approach, supports retry and async sending |
| **Security** | Spring Security 6.x | Form-based auth, BCrypt, role-based access |
| **Build Tool** | Gradle 9.x | Already configured, optimal for Java 25 toolchain support |

### Resource Impact

- **Frontend**: 14KB (HTMX) vs 500KB+ (React/Vue) ✅
- **Memory**: ~1.5GB total runtime (JVM + PostgreSQL) ✅
- **Disk**: ~80MB JAR + ~100MB DB ✅
- **Constraints met**: All within 4GB RAM / 8GB disk ✅

**Research Document**: [research.md](./research.md)

---

## Phase 1: Design Summary

Completed: 2026-02-07

### Data Model

Two main entities:
1. **User** - Authentication with roles (USER, ADMINISTRATOR)
2. **Issue** - Issue tracking with status workflow

**Key Features**:
- BCrypt password hashing
- Status enum with workflow transitions
- Optimistic locking for email sending (prevents duplicates)
- Automatic timestamp updates

**Document**: [data-model.md](./data-model.md)

### API Contracts

- **REST API**: `/api/issues`, `/api/users` - JSON endpoints for CRUD
- **HTMX Endpoints**: `/fragments/*` - HTML partials for dynamic updates
- **Views**: `/board`, `/login`, `/register` - Full page Thymeleaf templates
- **Auth**: Form-based with Spring Security

**Document**: [contracts/api.md](./contracts/api.md)

### Quickstart

Setup guide includes:
1. Environment setup (Docker PostgreSQL)
2. Configuration (application-local.properties)
3. Running the application
4. Development workflow
5. Production deployment

**Document**: [quickstart.md](./quickstart.md)

---

## Next Steps

Phase 1 is complete. The next phase is **Phase 2: Task Planning** which will create:
- `tasks.md` - Detailed implementation tasks
- Task breakdown for development

**To proceed**: Run `/speckit.tasks` command to generate implementation tasks.

---

## Artifacts Generated

| Artifact | Path | Phase | Status |
|----------|------|-------|--------|
| Feature Specification | `specs/001-issue-tracker/spec.md` | Input | ✅ Complete |
| Research | `specs/001-issue-tracker/research.md` | Phase 0 | ✅ Complete |
| Data Model | `specs/001-issue-tracker/data-model.md` | Phase 1 | ✅ Complete |
| API Contracts | `specs/001-issue-tracker/contracts/api.md` | Phase 1 | ✅ Complete |
| Quickstart | `specs/001-issue-tracker/quickstart.md` | Phase 1 | ✅ Complete |
| Implementation Plan | `specs/001-issue-tracker/plan.md` | Phase 1 | ✅ Complete |
| Tasks | `specs/001-issue-tracker/tasks.md` | Phase 2 | ⏳ Pending |

---

## Complexity Tracking

No complexity violations identified. All technology choices align with:
- AGENTS.md guidelines
- Resource constraints (4GB RAM / 8GB disk)
- Security best practices
- Test-first development approach

The architecture is intentionally simple to meet the constraint requirements while providing all required functionality.
