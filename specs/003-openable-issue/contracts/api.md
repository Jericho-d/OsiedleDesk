# API Contracts: Openable Issue Detail View

**Change**: `003-openable-issue`  
**Date**: 2026-02-22

---

## New Endpoints

### 1. GET /fragments/issues/{id}/detail

Fetches the issue detail overlay fragment for rendering in the modal container.

**Controller**: `FragmentController` (add to existing class)  
**Access**: Any authenticated user (ADMIN or USER)

**Request**

```
GET /fragments/issues/{id}/detail
Path params:
  id  — Long, required — ID of the issue to display
```

**Response — 200 OK**

Returns Thymeleaf fragment: `"fragments/issue-detail :: issue-detail"`

The rendered HTML is a complete modal overlay (backdrop + panel) containing all issue fields. HTMX injects this as
`innerHTML` of `#issue-modal`.

**Response — 404 (issue not found)**

Returns Thymeleaf fragment: `"fragments/issue-detail :: issue-not-found"` (or an inline error message fragment). The
modal shows a "Issue not found" message with a close button.

**HTMX call site** (in `issue-card.html`):

```html
<div th:fragment="issue-card" class="issue-card" th:id="'issue-' + ${issue.id()}"
     th:hx-get="@{'/fragments/issues/' + ${issue.id()} + '/detail'}"
     hx-target="#issue-modal"
     hx-swap="innerHTML">
```

---

### 2. POST /api/issues/{id}/send (detail variant)

Sends the issue to the Administrative Company and returns the updated **detail fragment** (not the card fragment) so the
modal updates in place.

**Controller**: `IssueViewController` (add new mapping) or `FragmentController`  
**Access**: `ADMIN` role only (`@PreAuthorize("hasRole('ADMIN')")`)

**Request**

```
POST /api/issues/{id}/send/detail
Path params:
  id  — Long, required
```

**Response — 200 OK**

Returns Thymeleaf fragment: `"fragments/issue-detail :: issue-detail"` with updated issue (status=IN_PROGRESS,
sent=true).

**HTMX call site** (in `issue-detail.html`):

```html
<button th:hx-post="@{'/api/issues/' + ${issue.id()} + '/send/detail'}"
        hx-target="#issue-modal"
        hx-swap="innerHTML">
    Send to Administration
</button>
```

---

### 3. POST /api/issues/{id}/status (detail variant)

Changes the issue status and returns the updated **detail fragment**.

**Controller**: `IssueViewController` (add new mapping)  
**Access**: `ADMIN` role only (`@PreAuthorize("hasRole('ADMIN')")`)

**Request**

```
POST /api/issues/{id}/status/detail
Path params:
  id    — Long, required
Form params:
  status — String, required — one of: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WONT_DO
```

**Response — 200 OK**

Returns Thymeleaf fragment: `"fragments/issue-detail :: issue-detail"` with updated issue.

**HTMX call site** (in `issue-detail.html`):

```html
<select th:hx-post="@{'/api/issues/' + ${issue.id()} + '/status/detail'}"
        hx-trigger="change"
        hx-target="#issue-modal"
        hx-swap="innerHTML">
    <option value="" selected disabled>Change Status</option>
    <option value="PREPARED">Prepared</option>
    ...
</select>
```

---

## Thymeleaf Fragment Contracts

### fragments/issue-detail.html — `issue-detail` fragment

**Template variables**:

- `issue` — `IssueResponse` — all fields as described in `data-model.md`

**Rendered structure** (semantic HTML):

```
#issue-modal-backdrop  (full-screen backdrop, click closes modal)
└── .issue-detail-panel  (centered card panel, click does NOT close)
    ├── .detail-header
    │   ├── h2  (issue title)
    │   └── button.close-btn  (×, clears #issue-modal innerHTML)
    ├── .detail-body
    │   ├── .detail-description  (full text, scrollable)
    │   └── .detail-meta
    │       ├── Status badge
    │       ├── Priority badge (reuses existing priority-* CSS classes)
    │       ├── Assignee (or "Unassigned" if null)
    │       ├── Created date (formatted)
    │       ├── Updated date (formatted)
    │       └── Sent indicator (badge if sent=true, nothing if false)
    └── .detail-actions (sec:authorize="hasRole('ADMIN')" only)
        ├── Status dropdown  (POST /api/issues/{id}/status/detail)
        └── Send button  (POST /api/issues/{id}/send/detail, if PREPARED and not sent)
```

**Fragment**: `th:fragment="issue-detail"`

### fragments/issue-detail.html — `issue-not-found` fragment

Inline error state shown when the issue cannot be found. Contains a message and a close button that clears
`#issue-modal`.

```
.issue-detail-panel
└── "Issue not found or has been deleted."
└── [Close] button
```

---

## HTMX Wiring Summary

| Trigger                             | Endpoint                              | Target         | Swap            |
|-------------------------------------|---------------------------------------|----------------|-----------------|
| Click `.issue-card`                 | `GET /fragments/issues/{id}/detail`   | `#issue-modal` | `innerHTML`     |
| Click "Send" in detail              | `POST /api/issues/{id}/send/detail`   | `#issue-modal` | `innerHTML`     |
| Change status in detail             | `POST /api/issues/{id}/status/detail` | `#issue-modal` | `innerHTML`     |
| Escape / click backdrop / close btn | (JS)                                  | `#issue-modal` | clear innerHTML |

---

## Board Column Refresh Strategy

After send/status change from the detail view, the affected board column will update via its existing 5-second
auto-poll (`hx-trigger="load, every 5s"`). No OOB swap is implemented in the initial version. The detail view shows the
updated state immediately; the board column catches up within 5 seconds.

If sub-second board refresh is required in future, `HX-Trigger` response headers can be added to trigger an immediate
column poll.
