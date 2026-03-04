# Quickstart: Kanban Board with HTMX and Material Design

**Branch**: `007-kanban-board-htmx`  
**Date**: 2026-02-22

This guide gets a developer up to speed on building and running the Kanban board feature.

---

## Prerequisites

- Docker and Docker Compose (for PostgreSQL)
- No local JDK required — Gradle toolchains downloads Java 25 automatically on first build
- Git (branch `007-kanban-board-htmx` checked out)

---

## Running the Application

**Start PostgreSQL** (Docker Compose is provided):
```bash
docker compose up -d
```

**Run the application**:
```bash
./gradlew bootRun
```

The board is available at: `http://localhost:8080/board`

**Default credentials** (seeded via Liquibase):
- Admin: `admin` / `admin` (role: `ADMINISTRATOR`)
- Standard user: (check `005-material-redesign` seeded data or create via `/issues/new`)

---

## Building and Testing

```bash
# Full build (compile + test)
./gradlew build

# Tests only
./gradlew test

# Single test class
./gradlew test --tests IssueServiceTest

# Clean build
./gradlew clean build
```

Tests use an H2 in-memory database; no running PostgreSQL is required for `./gradlew test`.

---

## Feature Overview: What Was Changed / Added

### New Endpoint

| Method | URL | Controller | Returns |
|--------|-----|-----------|--------|
| `PATCH` | `/fragments/board/move` | `FragmentController.moveIssue()` | HTML fragments (HTMX) |

**How it works**:
1. Receives `issueId` (Long) and `targetStatus` (Status enum name) as form parameters
2. Reads the issue's current `status` (sourceStatus) from the database
3. Calls `IssueService.updateIssueStatus(issueId, targetStatus)`
4. Loads `targetIssues = IssueService.getIssuesByStatus(targetStatus)` and `sourceIssues = IssueService.getIssuesByStatus(sourceStatus)`
5. Returns template `fragments/board :: move-response` with both column lists

### Modified Endpoint

`GET /board` — `BoardController.board()` updated to populate a `Map<Status, List<IssueResponse>> issuesByStatus` model attribute (grouped by status) rather than a flat list.

### Templates

| File | Change |
|------|--------|
| `board/index.html` | Rewritten with M3 layout: five `<section class="kanban-column">` elements, each with a `<div id="{status}-list">` target, `data-status` attributes, HTMX polling, drag-over handlers |
| `fragments/issue-card.html` | Added `draggable="true"`, `data-issue-id`, `data-status` to each card; M3 elevation CSS classes |
| `fragments/board-columns.html` | **New**: contains `move-response` fragment (primary column content + OOB sibling) |
| `fragments/send-error.html` | No changes |
| `fragments/issue-detail.html` | No changes |

### CSS (`static/css/material.css`)

New additions (appended, existing rules unchanged):

| Section | What was added |
|---------|---------------|
| M3 System Color Tokens | `--md-sys-color-primary: #6750A4` and full M3 baseline palette |
| M3 Typography Tokens | `--md-sys-typescale-title-medium-*`, `--md-sys-typescale-label-small-*`, `--md-sys-typescale-label-large-*` |
| Card Elevation | `.issue-card` at level 1; `.issue-card.card--dragging` at level 3; `transition: box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1)` |
| State Layers | `::after` pseudo overlay on `.issue-card`, `.priority-chip` for hover (8%) and pressed (12%) states |
| Drag CSS | `.card--dragging { opacity: 0.75 }`, `.card--pending { opacity: 0.5; pointer-events: none }`, `.column--drag-over { background-color: color-mix(in srgb, var(--md-sys-color-primary) 8%, var(--md-sys-color-surface)) }` |
| Priority Chips (M3) | `.priority-chip` updated to `border-radius: 8px`, `height: 32px`, `label-large` typography, per-priority tonal backgrounds |

### JavaScript (inline in `board/index.html`)

A small `<script>` block handles the five drag-and-drop events:
- Module-scope `let dragState = null` holds the in-flight card reference
- Wires `dragstart`, `dragover`, `dragleave`, `drop`, `dragend` to all `.issue-card` and `.kanban-column` elements respectively
- Calls `htmx.ajax('PATCH', '/fragments/board/move', {...})` on drop
- Listens for `htmx:afterRequest` to detect and revert failed requests

---

## Key Architecture Decisions

### Why the service layer uses `IssueService` (not `IssueCommandService`)

`IssueService` is the view-layer service: it returns `IssueResponse` DTOs and has `getIssuesByStatus()` which is needed to populate column fragments. `IssueCommandService` operates on raw `Issue` entities via the event-sourcing pattern and does not provide by-status queries. New view-layer code uses `IssueService` consistently.

### Why `PATCH` instead of `POST` for `/fragments/board/move`

`PATCH` semantically represents a partial update (changing only the `status` field). `POST` is also acceptable here since this is an HTMX-specific endpoint, but `PATCH` more accurately describes the intent and avoids collision with any future POST-based HTMX endpoints.

### Why column-level OOB swap (not card-level)

Replacing the entire column's issue list (`innerHTML` of `#prepared-list`) rather than doing card-level `outerHTML` OOB swaps handles the removal of the card from the source column atomically. There is no native `hx-swap="delete"` for OOB use, so card-level OOB cannot remove a card from its source column. Column-level `innerHTML` replacement is the simplest correct approach.

### `Status.WONT_DO` — no apostrophe

The Java enum value, database column value, and all HTML `data-status` attributes must use `WONT_DO`. The display label "Won't Do" is a Thymeleaf expression applied only in the template header.

---

## Testing the Drag-and-Drop Feature Manually

1. Log in as `admin` at `http://localhost:8080/login`
2. Navigate to `http://localhost:8080/board`
3. If no issues exist, create one at `http://localhost:8080/issues/new`
4. Drag a card from the **Prepared** column to **In Progress**
5. Verify: card appears in **In Progress**, card is gone from **Prepared**, no full page reload occurred
6. Open the Network tab in browser DevTools — you should see a single `PATCH /fragments/board/move` request with a 200 response containing two HTML fragments
7. Test error recovery: temporarily stop the server, try dragging a card — the card should revert to its original column with an error indication

---

## Common Issues

| Problem | Likely Cause | Fix |
|---------|-------------|-----|
| Board shows blank columns | PostgreSQL not running | `docker compose up -d` |
| Drag-and-drop does nothing | JS error in console | Check browser DevTools for HTMX errors |
| Card moves but doesn't persist | `PATCH /fragments/board/move` returning 422 | Check status transition validity (see `IssueService.updateIssueStatus()` transition rules) |
| Card moves but source column not updated | OOB swap IDs mismatch | Verify `id="{status}-list"` in `board/index.html` matches the OOB element `id` in `fragments/board-columns.html` |
| Send button missing | Issue `sent = true` already | Expected behaviour — email was already sent |
| `WONT_DO` column not accepting drops | `data-status` typo | Check HTML uses `WONT_DO` not `WON'T_DO` |

---

## File Reference

| File | Package / Path | Role |
|------|---------------|------|
| `FragmentController.java` | `controller/view` | `PATCH /fragments/board/move` handler |
| `BoardController.java` | `controller/view` | `GET /board` page handler |
| `IssueService.java` | `service` | `updateIssueStatus()`, `getIssuesByStatus()` |
| `board/index.html` | `templates/board` | Main board page template |
| `fragments/board-columns.html` | `templates/fragments` | `move-response` two-column fragment |
| `fragments/issue-card.html` | `templates/fragments` | Card and `column-content` fragments |
| `material.css` | `static/css` | All styles including M3 tokens and drag CSS |
