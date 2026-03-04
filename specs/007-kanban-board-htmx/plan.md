# Implementation Plan: Kanban Board with HTMX and Material Design

**Branch**: `007-kanban-board-htmx` | **Date**: 2026-02-22 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/007-kanban-board-htmx/spec.md`

## Summary

Build a Jira-like Kanban board web view that displays all issues in five status columns (PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO) with HTML5 Drag and Drop to move cards between columns. On drop, an HTMX request persists the new status and the response replaces only the two affected column fragments (source + destination) via `hx-swap-oob`. The board is styled with Material Design 3 color, typography, elevation, and state-layer tokens. No new database schema changes are required; the feature wires exclusively to the existing `Issue` entity and `Status` enum.

## Technical Context

**Language/Version**: Java 25 (via Gradle toolchain)  
**Primary Dependencies**: Spring Boot 4.0.2, Spring Data JDBC, Spring Security 6, Thymeleaf 3, HTMX 2.0.8 (via `io.github.wimdeblauwe:htmx-spring-boot-thymeleaf:5.0.0`), Lombok  
**Storage**: PostgreSQL 16+ with Liquibase migrations (no schema changes needed for this feature)  
**Testing**: JUnit 5 + AssertJ via `spring-boot-starter-test`; H2 in-memory for integration tests  
**Target Platform**: Linux/Docker server, desktop browser only (no mobile/touch DnD)  
**Project Type**: Web application (Spring MVC + Thymeleaf, no separate frontend build)  
**Performance Goals**: Board loads in <1s; drag-and-drop round-trip (drop → column swap) in <1s on broadband  
**Constraints**: No full page reloads; at most two column fragments re-rendered per drag operation; all transitions ≤300ms  
**Scale/Scope**: Single-tenant; ~100 issues typical; five columns always visible

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The project constitution file is a template (not filled in with project-specific rules). No violations are detectable against an empty constitution. All principles listed below are inferred from `AGENTS.md` and the existing codebase conventions:

| Principle | Status | Notes |
|-----------|--------|-------|
| Use `final var` for local variables in Java | PASS | Must be applied in all new service/controller code |
| Use `var` for local variables in tests | PASS | All new test locals must use `var` |
| No public modifiers on test methods or fields | PASS | Existing test convention, must be preserved |
| JUnit 5 + AssertJ only | PASS | No JUnit 4 or Hamcrest allowed |
| Lombok annotations on model classes | PASS | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` |
| Constructor injection with `@Autowired` | PASS | Use `@RequiredArgsConstructor` or explicit `@Autowired` constructor |
| `ResponseEntity` for HTTP status codes | PASS | New controller method returns `ResponseEntity` for error states |
| No new database schema changes | PASS | This feature uses existing `issues` table as-is |

No gate violations. Proceed to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/007-kanban-board-htmx/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── openapi.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/administrativetool/
├── controller/
│   └── view/
│       ├── BoardController.java          (MODIFY: add GET /board with all-column model)
│       └── FragmentController.java       (MODIFY: add POST /fragments/board/move endpoint)
├── service/
│   └── IssueService.java                 (MODIFY: add updateIssueStatus already exists — confirm signature)
└── (no new classes required)

src/main/resources/
├── templates/
│   ├── board/
│   │   └── index.html                    (REPLACE: full Material Design 3 Kanban board layout)
│   └── fragments/
│       ├── issue-card.html               (MODIFY: add draggable attributes, M3 card elevation CSS)
│       └── board-columns.html            (NEW: move-response fragment returning two OOB columns)
└── static/
    └── css/
        └── material.css                  (MODIFY: add M3 tokens, elevation shadows, state layers, drag CSS)
```

**Structure Decision**: Existing single-project Spring Boot structure is retained. No new packages, modules, or build steps are introduced. All changes are additive modifications to existing controllers, services, templates, and the stylesheet.

## Complexity Tracking

No constitution violations to justify. No complexity exceptions required.
