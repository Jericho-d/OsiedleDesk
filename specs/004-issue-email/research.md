# Research: Issue Email Notification to Administration

**Feature**: `004-issue-email`  
**Date**: 2026-02-22  
**Branch**: `004-issue-email`

## Summary

This research documents what is already implemented vs. what genuinely needs to be built for the "send email to
administration" feature. The codebase has significant existing email infrastructure; most of the feature is already
delivered. The gaps are narrow.

---

## Finding 1: What Is Already Implemented

### Decision

The core email-sending capability is **fully implemented** from features `001-issue-tracker` and `003-openable-issue`.
No new backend services are needed.

### What exists today

| Component                    | File                                                        | Status                                   |
|------------------------------|-------------------------------------------------------------|------------------------------------------|
| SMTP configuration           | `EmailConfig.java`                                          | ✅ Complete                               |
| Email sender service         | `EmailService.java`                                         | ✅ Complete (async, retryable)            |
| Issue-to-email orchestration | `IssueEmailService.java`                                    | ✅ Complete (idempotency, TX sync)        |
| Send from card endpoint      | `POST /api/issues/{id}/send` (`IssueViewController`)        | ✅ Complete                               |
| Send from detail endpoint    | `POST /api/issues/{id}/send/detail` (`IssueViewController`) | ✅ Complete                               |
| Send button on board card    | `fragments/issue-card.html`                                 | ✅ Complete (admin only, PREPARED+unsent) |
| Send button in detail view   | `fragments/issue-detail.html`                               | ✅ Complete (admin only, PREPARED+unsent) |
| Sent badge on card           | `fragments/issue-card.html`                                 | ✅ Complete                               |
| Sent indicator in detail     | `fragments/issue-detail.html`                               | ✅ Complete                               |
| `is_sent` / `sent_at` fields | `Issue.java`                                                | ✅ Complete                               |
| Duplicate-send prevention    | `IssueEmailService.java` (isSent check)                     | ✅ Complete                               |
| Async delivery + retry       | `EmailService.java` (`@Async`, `@Retryable`)                | ✅ Complete                               |
| Post-commit email send       | `IssueEmailService.java` (TransactionSynchronization)       | ✅ Complete                               |

### Rationale

All spec requirements (FR-001 through FR-011) map to already-implemented code. This feature is **effectively complete**.

---

## Finding 2: The One Genuine Gap — EmailService Subject Line

### Decision

Fix the `EmailService.buildSubject()` method to produce a subject line that matches what the spec and existing test
expect.

### Current behaviour

```java
// Produces: "[Issue #1 - HIGH] Test Issue"
return String.format("[Issue #%d - %s] %s", issue.getId(), issue.getPriority(), issue.getTitle());
```

### Expected behaviour (per `EmailServiceTest`)

The existing test asserts:

```java
assertThat(capturedMessage.getSubject()).isEqualTo("Issue Report: Test Issue");
```

### Rationale

The test was written to match a simpler, human-readable subject format. The current implementation diverges. Since the
test pre-dates this feature branch, the subject format should be aligned. The simpler format `"Issue Report: <title>"`
is clearer for administrative company recipients.

### Alternatives considered

- Keep current format `[Issue #N - PRIORITY] title` — rejected because the pre-existing test explicitly asserts the
  simpler format, and fixing the code to match the test is less disruptive than deleting the test.
- Delete the test — rejected as it provides valid coverage.

---

## Finding 3: Email Body Completeness

### Decision

The email body is **complete** relative to spec requirements (FR-003). Current body includes: issue ID, title, priority,
status, description, creation date, and assignee (with "Unassigned" fallback).

### Rationale

`EmailService.buildEmailBody()` already outputs all fields required by FR-003. No changes needed.

### One minor gap: `updatedAt` not in email body

The spec does not require `updatedAt` in the email, only `createdAt`. Current implementation matches spec exactly.

---

## Finding 4: Role-Based Button Visibility

### Decision

The `ADMINISTRATOR` role restriction is **already enforced** via `sec:authorize="hasRole('ADMIN')"` on the admin
controls wrapper in `issue-card.html` and `issue-detail.html`, and `@PreAuthorize("hasRole('ADMIN')")` on the send
endpoints.

### Rationale

Spring Security's Thymeleaf integration correctly hides the button block for non-admin users. Server-side endpoint
protection is also in place. FR-009 and SC-005 are met.

---

## Finding 5: Board Auto-Refresh After Send

### Decision

The board **already auto-refreshes** via HTMX polling (`hx-trigger="load, every 5s"`) on each column. The send action
also returns an updated card fragment via `hx-swap="outerHTML"`, providing immediate card-level update.

### Rationale

FR-011 (card reflects new status within 1s) is met — the card is replaced via HTMX outerHTML swap immediately on
success, not relying on the 5s poll.

---

## Finding 6: Pre-existing Failing Tests

### Decision

Fix the `EmailServiceTest.sendIssueEmail_shouldSendEmailToAdminCompany()` test failure as part of this feature by
aligning the `EmailService.buildSubject()` method with the test expectation.

### Root cause

The subject format in `buildSubject()` was changed after the test was written, creating a mismatch. The test asserts
`"Issue Report: Test Issue"` but the method produces `"[Issue #1 - HIGH] Test Issue"`.

### Fix

Change `buildSubject()` to:

```java
return "Issue Report: " + issue.getTitle();
```

### Impact on spec

The email subject line change is cosmetic — the spec does not specify a subject format. Using `"Issue Report: <title>"`
is a valid, human-readable subject for the administrative company.

---

## Summary: Actual Implementation Scope

| Task                              | What's needed                       |
|-----------------------------------|-------------------------------------|
| Email infrastructure              | Already complete — no changes       |
| Send button on card               | Already complete — no changes       |
| Send button in detail view        | Already complete — no changes       |
| Idempotency                       | Already complete — no changes       |
| Role enforcement                  | Already complete — no changes       |
| Fix `EmailService.buildSubject()` | **1 line change** — align with test |
| Fix `EmailServiceTest`            | Passes after subject fix            |

This feature effectively validates and confirms delivery of work already done, with a single small fix.
