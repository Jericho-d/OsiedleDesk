# Data Model: Kanban Board with Drag-and-Drop

**Feature**: `002-kanban-board-dnd`  
**Date**: 2026-02-22

---

## Overview

This feature introduces **no new database tables or schema migrations**. All entities and relationships are already in place. The work involves:
- Adding `draggable` HTML attributes to existing rendered entities
- Adding a new service method for DnD-specific status transitions
- Relaxing the existing status transition validation

---

## Existing Entities (unchanged schema)

### Issue

**Table**: `issues`  
**Java class**: `com.administrativetool.domain.model.Issue`

| Column | Java field | Type | Constraints | Notes |
|---|---|---|---|---|
| `id` | `id` | `BIGSERIAL` / `Long` | PK, NOT NULL | Auto-generated |
| `creator_id` | `creatorId` | `BIGINT` / `Long` | FK → users.id, NOT NULL | Set at creation |
| `title` | `title` | `VARCHAR(200)` / `String` | NOT NULL | Required (FR-019) |
| `description` | `description` | `TEXT` / `String` | NOT NULL | Required |
| `status` | `status` | `VARCHAR(20)` / `Status` | NOT NULL | Workflow stage |
| `priority` | `priority` | `VARCHAR(20)` / `Priority` | NOT NULL | Urgency level |
| `assignee` | `assignee` | `VARCHAR(50)` / `String` | nullable | Optional |
| `is_sent` | `sent` | `BOOLEAN` | NOT NULL, DEFAULT false | Idempotency guard |
| `sent_at` | `sentAt` | `TIMESTAMP` / `LocalDateTime` | nullable | Set when email sent |
| `created_at` | `createdAt` | `TIMESTAMP` / `LocalDateTime` | NOT NULL | Set at creation |
| `updated_at` | `updatedAt` | `TIMESTAMP` / `LocalDateTime` | NOT NULL | Set on every update |

**Lombok annotations**: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

---

### User

**Table**: `users`  
**Java class**: `com.administrativetool.domain.model.User`

| Column | Java field | Type | Notes |
|---|---|---|---|
| `id` | `id` | `BIGSERIAL` / `Long` | PK |
| `username` | `username` | `VARCHAR(50)` | Unique, NOT NULL |
| `password` | `password` | `VARCHAR(255)` | BCrypt encoded |
| `role_name` | `role` | `VARCHAR(20)` / `Role` | `USER` or `ADMINISTRATOR` |
| `created_at` | `createdAt` | `TIMESTAMP` | NOT NULL |
| `updated_at` | `updatedAt` | `TIMESTAMP` | NOT NULL |
| `failed_login_attempts` | `failedLoginAttempts` | `INTEGER` | Account lockout |
| `account_locked_until` | `accountLockedUntil` | `TIMESTAMP` | nullable |
| `last_failed_login` | `lastFailedLogin` | `TIMESTAMP` | nullable |

---

## Enums

### Status

```java
public enum Status {
    PREPARED,
    IN_PROGRESS,
    ACKNOWLEDGED,
    RESOLVED,
    WONT_DO
}
```

**Valid drag-and-drop transitions** (post-feature relaxation of `IssueService.validateStatusTransition`):

```
PREPARED    → IN_PROGRESS   ✅ (permitted; does NOT trigger email)
PREPARED    → WONT_DO       ✅ (reject without sending)
IN_PROGRESS → ACKNOWLEDGED  ✅
IN_PROGRESS → WONT_DO       ✅
ACKNOWLEDGED→ RESOLVED      ✅
ACKNOWLEDGED→ WONT_DO       ✅
RESOLVED    → PREPARED      ✅ (reopen; resets is_sent flag)
RESOLVED    → WONT_DO       ✅
WONT_DO     → PREPARED      ✅ (reopen)
```

**Invalid transitions** (server returns 422 on DnD drop to these):
```
Any status → same status (no-op; client skips server call)
PREPARED    → ACKNOWLEDGED  ✗
PREPARED    → RESOLVED      ✗
IN_PROGRESS → PREPARED      ✗
IN_PROGRESS → RESOLVED      ✗
ACKNOWLEDGED→ PREPARED      ✗
ACKNOWLEDGED→ IN_PROGRESS   ✗
RESOLVED    → IN_PROGRESS   ✗
RESOLVED    → ACKNOWLEDGED  ✗
WONT_DO     → IN_PROGRESS   ✗
WONT_DO     → ACKNOWLEDGED  ✗
WONT_DO     → RESOLVED      ✗
```

**is_sent reset rule**: When an issue transitions to `PREPARED` (reopen), `is_sent` MUST be reset to `false` and `sent_at` MUST be set to `null`.

### Priority

```java
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
```

**Visual representation on board cards**:

| Value | Badge CSS class | Color swatch |
|---|---|---|
| `LOW` | `priority-chip--low` | Green (P-90 / P-10 tone pair) |
| `MEDIUM` | `priority-chip--medium` | Amber |
| `HIGH` | `priority-chip--high` | Orange |
| `CRITICAL` | `priority-chip--critical` | Red (error-container / on-error-container) |

### Role

```java
public enum Role {
    USER,
    ADMINISTRATOR
}
```

---

## Conceptual Board Model (no schema change)

The board is a projection of `Issue` grouped by `Status`. There is no `Column` table — columns are computed views.

```
Board
└── Column[PREPARED]     ← issues WHERE status = 'PREPARED' ORDER BY created_at DESC
└── Column[IN_PROGRESS]  ← issues WHERE status = 'IN_PROGRESS' ORDER BY created_at DESC
└── Column[ACKNOWLEDGED] ← issues WHERE status = 'ACKNOWLEDGED' ORDER BY created_at DESC
└── Column[RESOLVED]     ← issues WHERE status = 'RESOLVED' ORDER BY created_at DESC
└── Column[WONT_DO]      ← issues WHERE status = 'WONT_DO' ORDER BY created_at DESC
```

---

## HTML Data Attributes (new — no DB change)

The following `data-*` attributes must be added to rendered HTML for DnD functionality:

### Issue card (`<div class="issue-card">`)

| Attribute | Value | Purpose |
|---|---|---|
| `draggable` | `"true"` | Activates HTML5 drag handle |
| `data-issue-id` | `${issue.id}` | Passed via `dataTransfer` on dragstart |
| `data-current-status` | `${issue.status.name()}` | Used by JS to detect same-column drops (no-op) |

### Column list (`<div class="md-column__list">`)

| Attribute | Value | Purpose |
|---|---|---|
| `data-status` | Status enum name (e.g., `PREPARED`) | Read on `drop` event to build PATCH URL |

**Column list IDs** (already present, must match exactly):

| Status | `id` attribute |
|---|---|
| `PREPARED` | `prepared-list` |
| `IN_PROGRESS` | `in-progress-list` |
| `ACKNOWLEDGED` | `acknowledged-list` |
| `RESOLVED` | `resolved-list` |
| `WONT_DO` | `wont-do-list` |

---

## Service Layer Change: `IssueService.validateStatusTransition`

The existing method blocks all moves out of `PREPARED`. The modified logic for the DnD endpoint:

```
// DnD-specific validation (more permissive than manual status buttons)
WONT_DO from any status → always allowed
PREPARED → IN_PROGRESS  → allowed (does NOT set is_sent)
PREPARED → WONT_DO      → allowed
RESOLVED → PREPARED     → allowed (must reset is_sent + sent_at)
WONT_DO  → PREPARED     → allowed (must reset is_sent + sent_at)
IN_PROGRESS → ACKNOWLEDGED → allowed
ACKNOWLEDGED → RESOLVED    → allowed
All other transitions    → throw IllegalArgumentException (→ 422 response)
```

**Note**: A new `updateIssueStatusForDragAndDrop(Long id, Status newStatus)` method is recommended to keep the existing `updateIssueStatus` behavior intact (used by the status-button flow). The DnD endpoint calls the new method.

---

## IssueResponse DTO (unchanged)

```java
// com.administrativetool.domain.dto.IssueResponse
// Fields used by Thymeleaf templates:
id, title, description, status, priority, assignee, sent, creatorId, createdAt, updatedAt
```

No new DTOs required for this feature. The DnD endpoint accepts `targetStatus` as a `@RequestParam String` (validated via `Status.valueOf()`).
