# Research: Openable Issue Detail View

**Change**: `003-openable-issue`  
**Date**: 2026-02-22  
**Branch**: `003-openable-issue`

## Overview

This document records what was found in the existing codebase during the research phase for this feature. All findings inform the implementation approach.

---

## 1. HTMX Fragment Pattern

The application uses HTMX 2.0.8 for all dynamic board interactions. There is no Alpine.js or other JS framework.

**Existing pattern** (`FragmentController.java`):
- `GET /fragments/board/column?status=X` → returns `"fragments/issue-card :: column-content"`
- `GET /fragments/issues/{id}/card` → returns `"fragments/issue-card :: issue-card"`

Both are simple Spring MVC controller methods returning a Thymeleaf fragment selector string. The pattern is straightforward: populate the `Model`, return the fragment path.

**Board polling** (`board/index.html`):
```html
<div class="issue-list" id="prepared-list"
     hx-get="/fragments/board/column?status=PREPARED"
     hx-trigger="load, every 5s">
```
Each column polls every 5 seconds. After an action (send/status change), the individual card is swapped via `hx-swap="outerHTML" hx-target="closest .issue-card"`.

**New endpoint needed**: `GET /fragments/issues/{id}/detail` → returns `"fragments/issue-detail :: issue-detail"` (new fragment to create).

---

## 2. Issue Card Click Behaviour

Currently, `.issue-card` has `cursor: pointer` and hover effects defined in `board/index.html` CSS, but no click handler. The card itself has no HTMX attributes.

**Required change to `issue-card.html`**: Add to the outer `.issue-card` div:
```html
hx-get="/fragments/issues/{id}/detail"
hx-target="#issue-modal"
hx-swap="innerHTML"
```

This will fetch the detail fragment and inject it into a new `#issue-modal` container in `board/index.html`.

**Important**: The `.issue-card` div already contains admin controls (send button, status dropdown) that have their own HTMX attributes. The click handler on the card must not interfere with these child element interactions. HTMX handles this correctly — clicks on child elements that have their own `hx-*` attributes will not bubble up to trigger the parent's `hx-get`.

---

## 3. Send and Status Change Endpoints

Both are in `IssueViewController.java` (despite being in the `view` package, they map to `/api/issues/...`):

- `POST /api/issues/{id}/send` — `@PreAuthorize("hasRole('ADMIN')")` — calls `issueEmailService.sendIssueToAdmin(id)`, then fetches updated issue and returns `"fragments/issue-card :: issue-card"`
- `POST /api/issues/{id}/status` — `@PreAuthorize("hasRole('ADMIN')")` — calls `issueService.updateIssueStatus(id, newStatus)`, returns `"fragments/issue-card :: issue-card"`

**Current swap target** (in `issue-card.html`): `hx-swap="outerHTML" hx-target="closest .issue-card"` — replaces the card in the board column.

**Problem for the detail view**: When these actions are triggered from inside the detail overlay, `closest .issue-card` won't match because the detail fragment is not inside a `.issue-card` element. Two approaches:

**Option A — OOB swap**: Return the updated card via `hx-swap-oob` in the detail fragment response, refreshing the board column card in the background, while the main response updates the detail view itself.

**Option B — Separate endpoints for detail view**: New endpoints that return the detail fragment instead of the card fragment.

**Decision**: Option B is simpler and cleaner. Create new endpoints (or parameterised versions) that return `"fragments/issue-detail :: issue-detail"` when called from the detail context. These can be added to `FragmentController` or a new `IssueDetailController`. The board column will refresh naturally via its 5-second polling rather than requiring OOB.

Alternatively, a hybrid: the detail-triggered endpoints return the updated detail fragment as the main response, and the column will auto-refresh within 5s. This avoids OOB complexity and matches the existing low-coupling polling pattern.

**Chosen approach**: Detail view actions (`send`, `status`) use the existing endpoints but target `#issue-modal` with `hx-swap="innerHTML"`. After the action, the endpoint returns the updated detail fragment. The board column refreshes via its 5s poll. This requires the existing `IssueViewController` endpoints to return the detail fragment when called from the detail view — achievable by adding separate `/detail/send` and `/detail/status` endpoints in `FragmentController`.

---

## 4. IssueResponse DTO

`IssueResponse` is a Java record with `@Builder`:

```java
public record IssueResponse(
    Long id, String title, String description,
    Status status, Priority priority, String assignee,
    boolean sent, Long creatorId,
    LocalDateTime createdAt, LocalDateTime updatedAt
)
```

- `sentAt` is absent (not in the DTO) — the sent timestamp is not available to the template. If we want to show "sent on [date]", `sentAt` would need to be added to `IssueResponse`. For this feature it is optional — we show the `sent` boolean indicator only.
- All fields are accessed via record accessors in Thymeleaf: `${issue.title()}`, `${issue.status()}`, `${issue.sent()}`, etc.
- `assignee` can be null — the detail view must handle this with `"Unassigned"` placeholder (FR-013).

---

## 5. Security / Role Checks in Templates

Spring Security Thymeleaf dialect is present (`xmlns:sec="http://www.thymeleaf.org/extras/spring-security"`). Admin-only controls use:

```html
<div sec:authorize="hasRole('ADMIN')">...</div>
```

or `th:if` on individual elements. The same pattern must be used in the detail fragment for the status dropdown and send button.

---

## 6. Dismiss Behaviour — Vanilla JS

HTMX alone cannot handle Escape key press or "click outside to close" natively. A small vanilla JS snippet is needed in `board/index.html`:

```javascript
// ~15 lines, no framework needed
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') clearModal();
});
document.addEventListener('click', e => {
    if (e.target.id === 'issue-modal-backdrop') clearModal();
});
function clearModal() {
    document.getElementById('issue-modal').innerHTML = '';
}
```

The modal container will consist of a backdrop overlay div plus the inner panel. Clicking the backdrop (not the panel) closes the modal.

**This JS stays well under the 1KB target** and does not require any new library.

---

## 7. Board Column Refresh After Detail Actions

After a send or status change from the detail view, the issue moves to a new column. The board auto-polls every 5 seconds, so the column update will appear within 5s without any explicit OOB wiring. This is acceptable per SC-004 ("within 1 second" for status change is aspirational but board polling is the existing pattern). If a tighter refresh is needed, `HX-Trigger` response headers can trigger an immediate column poll — but this is an optimisation, not required for the initial implementation.

---

## 8. Template Approach for Detail Overlay

The modal structure in `board/index.html`:

```html
<!-- Modal container — populated by HTMX when a card is clicked -->
<div id="issue-modal"></div>
```

The `issue-detail.html` fragment will render the full overlay (backdrop + panel) as a single `th:fragment="issue-detail"` block. When HTMX injects it into `#issue-modal`, the backdrop and panel become visible. Clearing `#issue-modal`'s innerHTML removes the overlay cleanly.

This approach keeps the board layout untouched and the modal purely HTMX-managed.

---

## 9. Files To Create / Modify Summary

| File | Change |
|------|--------|
| `FragmentController.java` | Add `GET /fragments/issues/{id}/detail` |
| `board/index.html` | Add `#issue-modal` container + ~15 lines dismiss JS |
| `fragments/issue-card.html` | Add `hx-get`, `hx-target`, `hx-swap` to `.issue-card` div |
| `fragments/issue-detail.html` | **NEW** — full detail overlay fragment |
| `FragmentControllerTest.java` | Add test for detail endpoint |
| `IssueViewController.java` | Add `POST /api/issues/{id}/send/detail` and `/status/detail` returning detail fragment |

---

## 10. Known Pre-existing Issues (Not Blocking This Feature)

LSP errors were observed in unrelated files (`SecurityConfig.java`, `EmailServiceTest.java`, `IssueQueryServiceTest.java`, `UserTest.java`). These are pre-existing and not caused by this feature. They should be addressed separately.
