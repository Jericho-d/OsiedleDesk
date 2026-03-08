# Tasks: Issue Email Notification to Administration

**Change**: `004-issue-email` | **Branch**: `004-issue-email` | **Date**: 2026-02-22  
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

## Summary

Full end-to-end implementation of the email send feature with error feedback.

## Tasks

### Phase 1 — Core Email Fix

- [x] **T-01** Fix `EmailService.buildSubject()` to return `"Issue Report: " + issue.getTitle()`
    - File: `src/main/java/com/administrativetool/service/EmailService.java`
    - Change: replaced `String.format("[Issue #%d - %s] %s", ...)` with `"Issue Report: " + issue.getTitle()`

- [x] **T-02** Run `EmailServiceTest` and confirm both test methods pass
    - Both `sendIssueEmail_shouldSendEmailToAdminCompany` and `sendIssueEmail_shouldHandleNullAssignee` pass

### Phase 2 — Template Fixes

- [x] **T-03** Fix Thymeleaf accessor syntax in `issue-card.html` and `issue-detail.html`
    - Changed record-style `issue.title()` → `${issue.title}` (and all other fields)
    - Added null guards on `createdAt`/`updatedAt`

- [x] **T-04** Fix role check `hasRole('ADMIN')` → `hasRole('ADMINISTRATOR')` in templates and controller
    - Updated `sec:authorize` attributes in `issue-card.html` and `issue-detail.html`
    - Updated `@PreAuthorize` in `IssueViewController.java`

- [x] **T-05** Remove status dropdown `<select>` from `issue-card.html` and `issue-detail.html`
    - Column movement is fully automatic via the send action
    - Kept Send button and Sent badge only

### Phase 3 — Send → Error Feedback Architecture

- [x] **T-06** Make `EmailService` synchronous (remove `@Async` and `@Retryable`)
    - File: `src/main/java/com/administrativetool/service/EmailService.java`
    - Exception now propagates to caller on failure

- [x] **T-07** Rework `IssueEmailService` to send email before DB update inside `@Transactional`
    - File: `src/main/java/com/administrativetool/service/IssueEmailService.java`
    - Email sent first; if it throws, transaction rolls back and DB is unchanged
    - Removed `afterCommit` hook and `EmailSendResponse` return type (now `void`)

- [x] **T-08** Add `<div id="toast-container">` fixed-position container to `board/index.html`
    - Top-centre overlay, `z-index: 2000`, pointer-events passthrough

- [x] **T-09** Create `fragments/send-error.html` — OOB error toast
    - `hx-swap-oob="true"` targets `#toast-container`
    - "The pigeon didn't make it." headline with `${errorDetail}` message
    - Auto-dismiss after 7 s with fade; manual ✕ dismiss button; slide-down CSS animation

- [x] **T-10** Update `IssueViewController` to catch exceptions and return error toast
    - File: `src/main/java/com/administrativetool/controller/view/IssueViewController.java`
    - Added `@Slf4j`; both `sendIssue` and `sendIssueFromDetail` wrapped in try/catch
    - On success: set `oob=true` model attribute, return card/detail fragment (OOB swap)
    - On error: set `errorDetail` model attribute, return `fragments/send-error :: send-error`

- [x] **T-11** Change Send buttons to `hx-swap="none"`; use OOB for both success and error responses
    - `issue-card.html`: send button changed from `hx-swap="outerHTML"` + `hx-target="closest .issue-card"` →
      `hx-swap="none"`
    - Card fragment gains `th:attr="hx-swap-oob=..."` conditional on `${oob}`
    - `issue-detail.html`: send button changed from `hx-target="#issue-modal"` + `hx-swap="innerHTML"` →
      `hx-swap="none"`
    - Detail fragment gains `th:attr="hx-swap-oob=..."` conditional on `${oob}`

### Phase 4 — Cleanup (low priority)

- [ ] **T-12** Remove unused `emailExecutor` `@Bean` from `EmailConfig.java`
    - Was used by old `@Async("emailExecutor")`; now that `@Async` is removed it's dead code
