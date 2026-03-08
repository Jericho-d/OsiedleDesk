# Data Model: Kanban Board with HTMX and Material Design

**Branch**: `007-kanban-board-htmx`  
**Date**: 2026-02-22  
**Phase**: 1 — Design

---

## Overview

This feature introduces **no new database tables or schema changes**. It builds entirely on the existing `issues` table
and domain model. All entities described below already exist; this document captures them for implementation reference
and identifies the fields/transitions relevant to the board view.

---

## Existing Entities (Relevant Fields)

### Issue

**Table**: `issues`  
**Java class**: `com.administrativetool.domain.model.Issue`  
**Repository**: `com.administrativetool.repository.IssueRepository` (`ListCrudRepository<Issue, Long>`)

| Java Field    | Column        | Type              | Nullable | Notes                                                          |
|---------------|---------------|-------------------|----------|----------------------------------------------------------------|
| `id`          | `id`          | `Long`            | No       | Primary key, BIGSERIAL                                         |
| `creatorId`   | `creator_id`  | `Long`            | No       | FK → `users.id` ON DELETE CASCADE                              |
| `title`       | `title`       | `String`          | No       | VARCHAR(200); displayed on card                                |
| `description` | `description` | `String`          | No       | TEXT; displayed in detail view (not on card)                   |
| `status`      | `status`      | `Status` (enum)   | No       | VARCHAR(20); determines which column the card appears in       |
| `priority`    | `priority`    | `Priority` (enum) | No       | VARCHAR(20); rendered as a priority chip on the card           |
| `assignee`    | `assignee`    | `String`          | Yes      | VARCHAR(50); displayed as metadata on the card                 |
| `sent`        | `is_sent`     | `boolean`         | No       | If `true`, email has been sent; Send button is hidden/disabled |
| `sentAt`      | `sent_at`     | `LocalDateTime`   | Yes      | Timestamp of email send; shown in detail view                  |
| `createdAt`   | `created_at`  | `LocalDateTime`   | No       | Shown in detail view                                           |
| `updatedAt`   | `updated_at`  | `LocalDateTime`   | No       | Updated on every status change                                 |

**Lombok annotations on `Issue`**: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`  
**Note on field naming**: The `sent` field maps to `is_sent` column. Lombok generates `isSent()` / `setSent()`
accessors (not `getSent()`).

---

### Status Enum

**Java class**: `com.administrativetool.domain.model.Status`

| Enum Value     | Column Value   | Display Name | Column Order |
|----------------|----------------|--------------|--------------|
| `PREPARED`     | `PREPARED`     | Prepared     | 1            |
| `IN_PROGRESS`  | `IN_PROGRESS`  | In Progress  | 2            |
| `ACKNOWLEDGED` | `ACKNOWLEDGED` | Acknowledged | 3            |
| `RESOLVED`     | `RESOLVED`     | Resolved     | 4            |
| `WONT_DO`      | `WONT_DO`      | Won't Do     | 5            |

**Critical note**: The enum value is `WONT_DO` (no apostrophe). The spec document and `AGENTS.md` reference `WON'T_DO`,
but the actual Java enum uses `WONT_DO`. All URL parameters, JSON values, and `data-status` HTML attributes must use
`WONT_DO`.

---

### Priority Enum

**Java class**: `com.administrativetool.domain.model.Priority`

| Enum Value | Display Label | Chip Background | Chip Text |
|------------|---------------|-----------------|-----------|
| `LOW`      | Low           | `#E8F5E9`       | `#1B5E20` |
| `MEDIUM`   | Medium        | `#FFF8E1`       | `#E65100` |
| `HIGH`     | High          | `#FBE9E7`       | `#BF360C` |
| `CRITICAL` | Critical      | `#FCE4EC`       | `#880E4F` |

---

## State Transitions

The `IssueService.updateIssueStatus()` method validates transitions. Valid transitions based on the existing
implementation:

```
PREPARED     → IN_PROGRESS      (normal progression)
IN_PROGRESS  → ACKNOWLEDGED     (normal progression)
ACKNOWLEDGED → RESOLVED         (completion)
RESOLVED     → PREPARED         (reopen)
ANY          → WONT_DO          (rejection override — always allowed)
```

**Illegal transitions** (rejected by the server; triggers a 400 or business exception):

- `WONT_DO → *` (any transition out of WONT_DO except back to PREPARED if reopen allowed)
- `RESOLVED → IN_PROGRESS` (skipping PREPARED)
- `PREPARED → ACKNOWLEDGED` (skipping IN_PROGRESS)

**Board behaviour on rejected transition**: The drag-and-drop frontend must handle the server's rejection by reverting
the card to its original column (see research decision 5 — DOM revert pattern).

---

## IssueResponse DTO

**Java class**: `com.administrativetool.domain.dto.IssueResponse`  
Used by `IssueService` (the view-layer service). All board fragments should use `IssueResponse` (not raw `Issue`
entities).

| Field         | Type            | Notes                                                  |
|---------------|-----------------|--------------------------------------------------------|
| `id`          | `Long`          | Used as `data-issue-id` on drag handle                 |
| `title`       | `String`        | Displayed on card, `title-medium` typography           |
| `description` | `String`        | Not displayed on card; available in detail fragment    |
| `status`      | `Status`        | Determines column placement                            |
| `priority`    | `Priority`      | Determines priority chip colour                        |
| `assignee`    | `String`        | Displayed as label-small metadata on card; may be null |
| `sent`        | `boolean`       | Controls visibility of Send button                     |
| `createdAt`   | `LocalDateTime` | Displayed in detail view                               |
| `updatedAt`   | `LocalDateTime` | Displayed in detail view                               |

---

## View Model: Kanban Board

The board page requires the following model attributes, populated by `BoardController`:

| Attribute Name   | Type                               | Source                                            |
|------------------|------------------------------------|---------------------------------------------------|
| `issuesByStatus` | `Map<Status, List<IssueResponse>>` | `IssueService.getAllIssues()` grouped by `status` |
| `statuses`       | `List<Status>`                     | `Status.values()` in display order                |
| `currentUser`    | `String`                           | `Authentication.getName()`                        |

**Alternative**: pass five separate model attributes (`preparedIssues`, `inProgressIssues`, etc.) for Thymeleaf
directness. The `Map<Status, List>` approach is more maintainable as the number of statuses grows.

---

## View Model: Move Response Fragment

The `POST /fragments/board/move` endpoint populates:

| Attribute Name   | Type                  | Source                                                          |
|------------------|-----------------------|-----------------------------------------------------------------|
| `targetIssues`   | `List<IssueResponse>` | `IssueService.getIssuesByStatus(targetStatus)`                  |
| `sourceIssues`   | `List<IssueResponse>` | `IssueService.getIssuesByStatus(sourceStatus)`                  |
| `targetStatus`   | `Status`              | From request parameter                                          |
| `sourceStatus`   | `Status`              | Derived from `Issue` before update                              |
| `targetStatusId` | `String`              | `targetStatus.name().toLowerCase().replace('_', '-') + "-list"` |
| `sourceStatusId` | `String`              | Same transform for source                                       |

The `sourceStatusId` and `targetStatusId` attributes are computed status-to-DOM-ID mappings (e.g.,
`Status.IN_PROGRESS` → `"in-progress-list"`) used in the OOB fragment's `id` attribute.

---

## No Schema Changes Required

This feature's database layer is read-only (for display) and write-only through the existing
`IssueService.updateIssueStatus()` path. No Liquibase changelogs need to be added.

---

## New Files

No new Java entity classes, enums, or repositories. New Thymeleaf fragments only:

| File                                     | Purpose                                                                                               |
|------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `templates/fragments/board-columns.html` | Contains `move-response` fragment (primary column + OOB source column)                                |
| `templates/board/index.html`             | Modified to use M3 layout, draggable cards, HTMX column targets                                       |
| `templates/fragments/issue-card.html`    | Modified to add `draggable="true"`, `data-issue-id`, `data-status` attributes and M3 card CSS classes |
| `static/css/material.css`                | Modified to add M3 system tokens, elevation shadows, state layers, drag-and-drop CSS                  |
