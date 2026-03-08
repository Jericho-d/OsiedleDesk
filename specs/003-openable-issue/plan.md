# Implementation Plan: Openable Issue Detail View

**Branch**: `003-openable-issue` | **Date**: 2026-02-22 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/003-openable-issue/spec.md`

## Summary

Add a Jira-style overlay detail view that opens when a user clicks any issue card on the Kanban board. The view displays
all issue fields in full. Administrators can change status and send the issue to the Administrative Company directly
from the detail view. Implemented using HTMX fragment fetching from a new `FragmentController` endpoint and a new
Thymeleaf fragment — no new data model changes required.

## Technical Context

**Language/Version**: Java 25 (via Gradle toolchain)  
**Primary Dependencies**: Spring Boot 4.0.2, Spring Data JDBC, Thymeleaf 3, HTMX 2.0.8, Spring Security 6, Lombok  
**Storage**: PostgreSQL 16+ — no schema changes needed for this feature  
**Testing**: JUnit 5 + AssertJ via `spring-boot-starter-test`; `@WebMvcTest` for controller layer  
**Target Platform**: Web application (server-rendered, minimal frontend JS)  
**Project Type**: Web application (Spring Boot monolith, server-side rendering)  
**Performance Goals**: Detail view loads within 300ms of click (SC-001); adds ≤20KB to page weight (SC-007)  
**Constraints**: No Alpine.js or heavy JS frameworks; vanilla JS only for overlay dismiss behaviour (Escape key +
click-outside); total new JS < 1KB  
**Scale/Scope**: Single-user board; overlay replaces prior one if already open (only one at a time)

## Constitution Check

- No new database schema changes → no migration risk
- No new roles or permissions — existing ADMIN/USER distinction reused
- No new external dependencies — all libraries already in `build.gradle`
- HTMX OOB swap used for board column refresh after action → existing pattern, no architectural risk

## Project Structure

### Documentation (this feature)

```text
specs/003-openable-issue/
├── plan.md              # This file
├── research.md          # Codebase findings and HTMX strategy
├── data-model.md        # Confirms no model changes; documents UI model
├── quickstart.md        # Developer setup guide for running this feature
├── contracts/
│   └── api.md           # New endpoint + HTMX/Thymeleaf fragment contracts
└── tasks.md             # Checklist of implementation steps
```

### Source Code (changes for this feature)

```text
src/main/java/com/administrativetool/
└── controller/
    └── view/
        └── FragmentController.java          # ADD: GET /fragments/issues/{id}/detail

src/main/resources/templates/
├── board/
│   └── index.html                           # MODIFY: add #issue-modal container + JS dismiss logic
└── fragments/
    ├── issue-card.html                      # MODIFY: add hx-get click handler to .issue-card
    └── issue-detail.html                    # NEW: detail overlay fragment

src/test/java/com/administrativetool/
└── controller/
    └── view/
        └── FragmentControllerTest.java      # ADD: test for new detail endpoint
```

## Complexity Tracking

No architectural violations. All patterns follow existing codebase conventions:

- New endpoint matches existing `FragmentController` pattern
- New template fragment matches existing `issue-card.html` fragment pattern
- HTMX OOB swap for board refresh matches existing send/status pattern
