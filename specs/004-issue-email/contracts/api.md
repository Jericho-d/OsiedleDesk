# API Contracts: Issue Email Notification to Administration

**Feature**: `004-issue-email`  
**Date**: 2026-02-22

## Overview

All endpoints for this feature already exist. No new endpoints are introduced. This document describes the existing contracts and the one behavioural fix (subject line alignment).

---

## Endpoint 1: Send Issue from Board Card

**Already implemented** in `IssueViewController.java`

```
POST /api/issues/{id}/send
```

### Authentication
- Required: Yes (session-based form login)
- Required role: `ADMINISTRATOR`

### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | `Long` | Yes | Issue identifier |

### Request Body
None.

### Responses

| Status | Condition | Body |
|--------|-----------|------|
| `200 OK` | Issue found and email queued (or already sent) | Thymeleaf fragment: `fragments/issue-card :: issue-card` |
| `404 Not Found` | Issue not found | None |
| `403 Forbidden` | User lacks ADMINISTRATOR role | None |

### Side Effects
1. If `issue.sent == false`: marks issue as sent, sets `sentAt`, transitions status `PREPARED → IN_PROGRESS`, registers post-commit email dispatch.
2. If `issue.sent == true`: no state change, no email sent.
3. Returns updated issue card fragment (HTMX replaces card via `hx-swap="outerHTML"`).

### HTMX Wiring (existing, in `issue-card.html`)
```html
th:hx-post="@{'/api/issues/' + ${issue.id()} + '/send'}"
hx-swap="outerHTML"
hx-target="closest .issue-card"
```

---

## Endpoint 2: Send Issue from Detail View

**Already implemented** in `IssueViewController.java`

```
POST /api/issues/{id}/send/detail
```

### Authentication
- Required: Yes (session-based form login)
- Required role: `ADMINISTRATOR`

### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | `Long` | Yes | Issue identifier |

### Request Body
None.

### Responses

| Status | Condition | Body |
|--------|-----------|------|
| `200 OK` | Issue found and email queued (or already sent) | Thymeleaf fragment: `fragments/issue-detail :: issue-detail` |
| `404 Not Found` | Issue not found | None |
| `403 Forbidden` | User lacks ADMINISTRATOR role | None |

### Side Effects
Same as Endpoint 1 — delegates to the same `IssueEmailService.sendIssueToAdmin()`.

### HTMX Wiring (existing, in `issue-detail.html`)
```html
th:hx-post="@{'/api/issues/' + ${issue.id()} + '/send/detail'}"
hx-target="#issue-modal"
hx-swap="innerHTML"
```

---

## Email Message Contract

The email is a plain-text `SimpleMailMessage` sent via SMTP.

### Subject (after fix)
```
Issue Report: <issue.title>
```

**Note**: The current implementation produces `[Issue #<id> - <PRIORITY>] <title>`. This will be changed to `Issue Report: <title>` to align with the `EmailServiceTest` assertion and provide a cleaner subject for administrative company recipients.

### Body
```
=== ISSUE REPORT ===

Issue ID: <id>
Title: <title>
Priority: <priority>
Status: <status>

=== DESCRIPTION ===
<description>

=== METADATA ===
Created: <createdAt in yyyy-MM-dd HH:mm:ss>
Assignee: <assignee | "Unassigned">

=== ACTION REQUIRED ===
Please review and process this issue.
Access the issue tracker at: http://localhost:8080/board
```

All fields are populated from the `Issue` entity at send time. The description is included in full without truncation.

---

## UI Component Contracts

### Send Button on Issue Card (existing, `fragments/issue-card.html`)

**Visibility rules**:
- Shown only when: `issue.status == PREPARED AND issue.sent == false AND hasRole('ADMIN')`
- Hidden otherwise

**On success**:
- Card fragment is replaced with updated version showing "Sent" badge, no send button, status reflects `IN_PROGRESS`

### Send Button in Detail View (existing, `fragments/issue-detail.html`)

**Visibility rules**:
- Same as above, within `sec:authorize="hasRole('ADMIN')"` wrapper

**On success**:
- Detail fragment reloaded into `#issue-modal`, showing sent indicator and no send button

### Sent Badge
Displayed on both card and detail view when `issue.sent == true`:
```html
<span class="sent-badge">✓ SENT</span>
```

---

## Test Contract

### `EmailServiceTest.sendIssueEmail_shouldSendEmailToAdminCompany()`

This pre-existing test is currently **failing** due to subject mismatch. After the fix:

| Assertion | Expected | Actual (after fix) |
|-----------|----------|--------------------|
| `subject` | `"Issue Report: Test Issue"` | `"Issue Report: Test Issue"` ✓ |
| `body` contains `"Test Issue"` | `true` | `true` ✓ |
| `body` contains `"Test Description"` | `true` | `true` ✓ |
| `body` contains `"HIGH"` | `true` | `true` ✓ |
| `body` contains `"PREPARED"` | `true` | `true` ✓ |
| `body` contains `"John Doe"` | `true` | `true` ✓ |
