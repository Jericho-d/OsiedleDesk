# Implementation Plan: Kanban Board with Drag-and-Drop

**Branch**: `002-kanban-board-dnd` | **Date**: 2026-02-22 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/002-kanban-board-dnd/spec.md`

## Summary

Implement a Material Design 3 Kanban board with HTML5 Drag-and-Drop in the existing Spring Boot 4 + Thymeleaf
application. The board already has all five status columns rendered via HTMX polling fragments. The work adds: (1)
drag-and-drop JS glue wiring HTML5 DnD events to `htmx.ajax()` PATCH calls, (2) a new `PATCH /api/issues/{id}/status`
endpoint returning dual-column OOB fragments, (3) MD3-compliant CSS upgrades to `material.css` covering exact shadow
values, motion curves, state layers, and priority chip colors, and (4) `draggable="true"` + `data-issue-id` /
`data-status` attributes on cards and column lists.

The codebase is largely complete. The primary gap is: no drag-and-drop interaction, the `PATCH` endpoint does not exist,
and `material.css` uses approximate (non-spec) elevation shadows and motion values.

---

## Technical Context

**Language/Version**: Java 25 (via Gradle toolchain)  
**Primary Dependencies**: Spring Boot 4.0.2, Thymeleaf 3, HTMX 2.0.8 (WebJar + CDN), Spring Security 6, Spring Data
JDBC, Lombok, Liquibase  
**Storage**: PostgreSQL 16+ (production); H2 (test/dev); schema managed by Liquibase  
**Testing**: JUnit 5 via `spring-boot-starter-test`, AssertJ, Spring Security Test, `@WebMvcTest` for controller layer  
**Target Platform**: Desktop browser, Linux server (Docker), port 8080  
**Project Type**: Single monolithic Spring Boot web application (SSR + partial HTMX fragments)  
**Performance Goals**: Drag-drop status change rendered within 1 second; board initial load under 2 seconds  
**Constraints**: No full-page reload on DnD; only affected columns re-rendered; desktop viewport ≥1280px; no mobile
DnD  
**Scale/Scope**: Small team (~5–20 concurrent users); no horizontal scaling requirements in this iteration

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The project constitution file is a blank template — no project-specific principles are defined. The following gates are
derived from the `AGENTS.md` code style guidelines, which serve as the effective constitution for this project:

| Gate                                                   | Status               | Notes                                |
|--------------------------------------------------------|----------------------|--------------------------------------|
| Use Lombok annotations; no boilerplate getters/setters | PASS                 | All entities use `@Data`, `@Builder` |
| `final var` for local variables in production code     | PASS — MUST MAINTAIN | New code must use `final var`        |
| `var` for local variables in test code                 | PASS — MUST MAINTAIN | All test locals use `var`            |
| No wildcard imports                                    | PASS — MUST MAINTAIN |                                      |
| JUnit 5 only (no JUnit 4)                              | PASS                 | `useJUnitPlatform()` in build        |
| AssertJ for assertions                                 | PASS — MUST MAINTAIN |                                      |
| `@PreAuthorize` + `sec:authorize` dual security guards | PASS — MUST MAINTAIN | Admin actions use both               |
| No checked exceptions in service layer                 | PASS — MUST MAINTAIN | Runtime exceptions only              |
| `ResponseEntity` for 404/not-found                     | PASS — MUST MAINTAIN |                                      |
| Liquibase for all schema changes                       | PASS — MUST MAINTAIN | No raw DDL in code                   |

**Post-Phase 1 re-check**: No constitution violations introduced by the design. The new `PATCH` endpoint follows
existing patterns. No new entities or schema changes required.

---

## Project Structure

### Documentation (this feature)

```text
specs/002-kanban-board-dnd/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── status-change.yaml
│   └── board-fragments.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

This is a single Spring Boot project. The feature touches these locations:

```text
src/main/java/com/administrativetool/
├── controller/
│   └── view/
│       ├── FragmentController.java          [MODIFY] add PATCH endpoint for DnD
│       └── IssueViewController.java         [MODIFY] or add new PATCH /api/issues/{id}/status for DnD
├── service/
│   └── IssueService.java                    [MODIFY] relax/extend validateStatusTransition for DnD

src/main/resources/
├── templates/
│   ├── board/
│   │   └── index.html                       [MODIFY] add DnD attributes + CSRF meta + JS glue
│   └── fragments/
│       └── issue-card.html                  [MODIFY] add draggable + data-issue-id attributes
├── static/
│   └── css/
│       └── material.css                     [MODIFY] MD3 shadow values, motion curves, state layers

src/test/java/com/administrativetool/
└── controller/view/
    └── IssueViewControllerTest.java         [NEW] or extend existing — test PATCH status endpoint
```

**Structure Decision**: Single project, existing layout. No new packages, no new entities. Feature is implemented as
modifications to existing files plus one new endpoint.

---

## Complexity Tracking

No constitution violations require justification. The design follows the existing patterns without adding new
architectural layers.
