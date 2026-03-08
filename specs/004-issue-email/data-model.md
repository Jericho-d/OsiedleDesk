# Data Model: Issue Email Notification to Administration

**Feature**: `004-issue-email`  
**Date**: 2026-02-22

## Overview

No schema changes are required for this feature. All data needed to compose and send issue emails already exists on the
`Issue` entity. No new tables, columns, or migration scripts are needed.

---

## Existing Entity: Issue

**Table**: `issues`  
**Java class**: `com.administrativetool.domain.model.Issue`

| Column        | Java Field    | Type              | Nullable | Description                           |
|---------------|---------------|-------------------|----------|---------------------------------------|
| `id`          | `id`          | `Long`            | No       | Primary key                           |
| `creator_id`  | `creatorId`   | `Long`            | Yes      | ID of the user who created the issue  |
| `title`       | `title`       | `String`          | No       | Short summary of the issue            |
| `description` | `description` | `String`          | No       | Full description text                 |
| `status`      | `status`      | `Status` (enum)   | No       | Workflow state                        |
| `priority`    | `priority`    | `Priority` (enum) | No       | Urgency level                         |
| `assignee`    | `assignee`    | `String`          | Yes      | Name of assigned person               |
| `is_sent`     | `sent`        | `boolean`         | No       | Whether email was dispatched to admin |
| `sent_at`     | `sentAt`      | `LocalDateTime`   | Yes      | Timestamp when email was sent         |
| `created_at`  | `createdAt`   | `LocalDateTime`   | No       | Issue creation timestamp              |
| `updated_at`  | `updatedAt`   | `LocalDateTime`   | No       | Last modification timestamp           |

### Status Enum Values

| Value          | Meaning                                      |
|----------------|----------------------------------------------|
| `PREPARED`     | Newly created, ready to send                 |
| `IN_PROGRESS`  | Email sent, being processed by admin company |
| `ACKNOWLEDGED` | Admin company has acknowledged the issue     |
| `RESOLVED`     | Issue resolved                               |
| `WONT_DO`      | Issue rejected                               |

### Priority Enum Values

| Value      | Meaning           |
|------------|-------------------|
| `LOW`      | Minor issue       |
| `MEDIUM`   | Standard priority |
| `HIGH`     | Urgent            |
| `CRITICAL` | Emergency         |

---

## Email Notification (Transient — Not Persisted)

The email sent to the administrative company is not stored as a separate entity. It is constructed at send time from the
`Issue` entity and dispatched via SMTP.

### Email Structure

| Field       | Value                                              |
|-------------|----------------------------------------------------|
| **To**      | `${ADMIN_COMPANY_EMAIL}` environment variable      |
| **Subject** | `Issue Report: <issue.title>`                      |
| **Body**    | Plain text containing all issue fields (see below) |

### Email Body Sections

```
=== ISSUE REPORT ===

Issue ID: <id>
Title: <title>
Priority: <priority>
Status: <status>

=== DESCRIPTION ===
<description>

=== METADATA ===
Created: <createdAt formatted as yyyy-MM-dd HH:mm:ss>
Assignee: <assignee or "Unassigned">

=== ACTION REQUIRED ===
Please review and process this issue.
Access the issue tracker at: http://localhost:8080/board
```

---

## State Transition on Send

When an issue is sent to the administrative company:

```
Before send:
  issue.status  = PREPARED
  issue.sent    = false
  issue.sentAt  = null

After send:
  issue.status  = IN_PROGRESS
  issue.sent    = true
  issue.sentAt  = <current timestamp>
  issue.updatedAt = <current timestamp>
```

This transition is persisted in the same transaction before the email is dispatched (post-commit delivery via
`TransactionSynchronization.afterCommit()`).

---

## Idempotency

| Condition             | Behaviour                                                       |
|-----------------------|-----------------------------------------------------------------|
| `issue.sent == false` | Email dispatched, status → IN_PROGRESS                          |
| `issue.sent == true`  | No email sent, returns `ALREADY_SENT` response, no state change |

This prevents duplicate email delivery even if the send endpoint is called multiple times.

---

## No Migration Required

All columns (`is_sent`, `sent_at`, `status`, etc.) were added in the `001-issue-tracker` feature. This feature makes no
schema changes.
