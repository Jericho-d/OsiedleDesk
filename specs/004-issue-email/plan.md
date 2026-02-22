# Implementation Plan: Issue Email Notification to Administration

**Branch**: `004-issue-email` | **Date**: 2026-02-22 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/004-issue-email/spec.md`

## Summary

Send a plain-text email containing all issue details to the configured administrative company email address when an administrator clicks the "Send" button on an issue card or detail view. The feature is **already fully implemented** from prior features. The only implementation task is fixing `EmailService.buildSubject()` to match the existing `EmailServiceTest` assertion (1-line change).

## Technical Context

**Language/Version**: Java 25 (via Gradle toolchain)  
**Primary Dependencies**: Spring Boot 4.0.2, Spring Mail (JavaMailSender), Spring Security 6, Thymeleaf 3, HTMX 2.0.8, Lombok  
**Storage**: PostgreSQL 16+ via Spring Data JDBC (no schema changes needed)  
**Testing**: JUnit 5 + AssertJ + Mockito via `spring-boot-starter-test`  
**Target Platform**: Linux server (Docker), port 8080  
**Project Type**: Spring Boot web application (Thymeleaf server-rendered)  
**Performance Goals**: Email send button response ≤ 1s (card fragment returned immediately; email delivered async)  
**Constraints**: No new dependencies; email delivery is async and retried; subject format must match `EmailServiceTest`  
**Scale/Scope**: Single-tenant, single admin company recipient

## Constitution Check

The project constitution file (`constitution.md`) is an unfilled template — no active governance rules are defined. No gate violations apply.

Post-design re-check: No violations. This is a single-service, single-change fix to an existing feature.

## Project Structure

### Documentation (this feature)

```text
specs/004-issue-email/
├── plan.md              # This file
├── research.md          # Phase 0 output ✅
├── data-model.md        # Phase 1 output ✅
├── quickstart.md        # Phase 1 output ✅
├── contracts/
│   └── api.md           # Phase 1 output ✅
├── checklists/
│   └── requirements.md  # Spec quality checklist ✅
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (affected files only)

```text
src/
└── main/
    └── java/com/administrativetool/
        └── service/
            └── EmailService.java        ← ONLY FILE MODIFIED (buildSubject, 1 line)

src/
└── test/
    └── java/com/administrativetool/
        └── service/
            └── EmailServiceTest.java    ← Passes after fix (no test changes needed)
```

All other email-related files (`IssueEmailService.java`, `EmailConfig.java`, `IssueViewController.java`, `issue-card.html`, `issue-detail.html`) are **already complete and require no changes**.

## Complexity Tracking

No violations. This is the simplest possible change: 1 line in 1 file.

## Phase 0 Research Findings

See [research.md](research.md) for full details.

**Key findings**:
1. The entire email send feature is already implemented (infrastructure, endpoints, UI buttons, idempotency, async delivery, role enforcement).
2. The only gap: `EmailService.buildSubject()` produces `"[Issue #N - PRIORITY] title"` but `EmailServiceTest` asserts `"Issue Report: title"`.
3. Fix: change `buildSubject()` to return `"Issue Report: " + issue.getTitle()`.
4. After this fix, `EmailServiceTest` fully passes (both test methods).

## Phase 1 Design Decisions

See [data-model.md](data-model.md) and [contracts/api.md](contracts/api.md) for full details.

**Key decisions**:
- No schema changes (all fields exist on `Issue` entity).
- No new endpoints (both send endpoints already exist in `IssueViewController`).
- Email subject changes from `"[Issue #N - PRIORITY] title"` → `"Issue Report: title"` — simpler, cleaner for recipients.
- Email body already contains all required fields (FR-003 met).

## Implementation Summary

| Task | File | Change |
|------|------|--------|
| Fix email subject | `service/EmailService.java` | `buildSubject()`: 1-line change |

That is the complete implementation scope for this feature branch.
