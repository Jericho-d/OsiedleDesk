# Quickstart: Issue Email Notification to Administration

**Feature**: `004-issue-email`  
**Date**: 2026-02-22  
**Branch**: `004-issue-email`

## What This Feature Does

Every issue card on the board has a "Send" button (visible to administrators only). Clicking it sends a plain-text email
containing all issue details to the configured administrative company email address, then transitions the issue status
from `PREPARED` to `IN_PROGRESS`.

The feature is **already fully implemented** in the codebase. The only task in this feature branch is fixing the email
subject line to match the existing test expectation.

---

## Developer Setup

### Prerequisites

- Docker (for PostgreSQL): `docker compose up -d`
- Java 25 (via Gradle toolchain — no local install needed)
- SMTP credentials (or use `SPRING_MAIL_HOST=localhost` with a local test server like MailHog)

### Environment Variables

```bash
# Required for email sending
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your@email.com
SPRING_MAIL_PASSWORD=yourpassword
ADMIN_COMPANY_EMAIL=administration@company.com

# Optional (defaults shown)
DB_URL=jdbc:postgresql://localhost:5432/admtool
DB_USERNAME=admtool
DB_PASSWORD=admtool
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin
```

### Run Locally

```bash
docker compose up -d          # start PostgreSQL
./gradlew bootRun             # start the application on :8080
```

---

## The One Code Change

`EmailService.buildSubject()` in `src/main/java/com/administrativetool/service/EmailService.java`:

**Before** (produces `"[Issue #1 - HIGH] Test Issue"`):

```java
return String.format("[Issue #%d - %s] %s",
        issue.getId(),
        issue.getPriority(),
        issue.getTitle());
```

**After** (produces `"Issue Report: Test Issue"`):

```java
return "Issue Report: " + issue.getTitle();
```

---

## Testing the Send Flow

### Manual test (browser)

1. Log in as `admin` at `http://localhost:8080`
2. Create an issue — it appears in the **PREPARED** column
3. Click the **📧 Send** button on the issue card
4. The card updates: status becomes **IN PROGRESS**, a ✓ SENT badge appears
5. Check the inbox of `ADMIN_COMPANY_EMAIL` — the email arrives with all issue details

### Unit test

```bash
./gradlew test --tests EmailServiceTest
```

After the subject fix, both tests in `EmailServiceTest` pass:

- `sendIssueEmail_shouldSendEmailToAdminCompany` — asserts subject = `"Issue Report: Test Issue"`
- `sendIssueEmail_shouldHandleNullAssignee` — asserts body contains "Unassigned"

### Full test suite

```bash
./gradlew test
```

Expected result: all tests pass (the 4 pre-existing failures in `StatusTest`, `PriorityTest`, `IssueDeletedEventTest`
are separate issues not addressed here, but `EmailServiceTest` should now fully pass).

---

## Key Files

| File                                       | Role                                                                   |
|--------------------------------------------|------------------------------------------------------------------------|
| `service/EmailService.java`                | Builds and sends the email (subject fix here)                          |
| `service/IssueEmailService.java`           | Orchestrates send: idempotency check, status update, post-commit email |
| `controller/view/IssueViewController.java` | `POST /api/issues/{id}/send` and `POST /api/issues/{id}/send/detail`   |
| `templates/fragments/issue-card.html`      | Send button + sent badge on board card                                 |
| `templates/fragments/issue-detail.html`    | Send button + sent indicator in detail overlay                         |
| `config/EmailConfig.java`                  | JavaMailSender bean configuration                                      |
| `resources/application.yml`                | SMTP and admin email env var bindings                                  |

---

## Integration Scenarios

### Scenario A: First-time send (happy path)

1. User: `POST /api/issues/5/send` (admin authenticated)
2. `IssueEmailService`: finds issue #5, `sent=false` → updates DB, registers afterCommit hook
3. Transaction commits → `EmailService.sendIssueEmail()` called async
4. SMTP delivers email to `ADMIN_COMPANY_EMAIL`
5. Response: updated card fragment with `IN_PROGRESS` status + sent badge

### Scenario B: Already-sent issue

1. User: `POST /api/issues/5/send` (issue already has `sent=true`)
2. `IssueEmailService`: detects `isSent() == true` → returns `ALREADY_SENT`, no DB write, no email
3. Response: updated card fragment (unchanged state)

### Scenario C: Email delivery failure

1. Send action proceeds → DB updated, `sent=true`, status=`IN_PROGRESS`
2. SMTP call fails (async) → `@Retryable` retries up to 3 times with backoff
3. If all retries fail: error logged, but DB state is already committed (issue marked sent)
4. This is acceptable — the administrative company may need to be contacted manually in this case

### Scenario D: Send from detail view

Same as Scenario A, but using `POST /api/issues/{id}/send/detail` — returns updated `issue-detail` fragment instead of
`issue-card`.
