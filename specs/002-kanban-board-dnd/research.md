# Research: Kanban Board Drag-and-Drop

**Feature**: `002-kanban-board-dnd`  
**Date**: 2026-02-22  
**Phase**: 0 — Pre-implementation research

---

## Decision 1: Drag-and-Drop Integration Pattern

**Decision**: Use HTML5 Drag-and-Drop API with event delegation + `htmx.ajax()` (HTMX 2.x public API). No external DnD library.

**Rationale**: HTMX does not natively trigger on `dragstart`/`drop` events. A thin JS glue layer (~40 lines total) handles the four required native events (`dragstart`, `dragover`, `drop`, `dragend`) and calls `htmx.ajax('PATCH', ...)` on drop. This is the minimal-JS approach consistent with the spec requirement. Event delegation on `document` is used (single listener per event type) so no re-registration is needed after HTMX swaps.

**Alternatives Considered**:
- SortableJS: Adds ~30 KB dependency; unnecessary for this use case.
- `hx-trigger="drop"`: HTMX does not support DOM DnD events as triggers; requires JS bridge regardless.

---

## Decision 2: HTTP Method for Status Change Endpoint

**Decision**: `PATCH /api/issues/{id}/status` with `targetStatus` as a request parameter.

**Rationale**: `PATCH` is semantically correct for a partial update (one field changed). `PUT` would require re-sending the entire resource. `POST` is appropriate only for non-idempotent actions with side effects (like the existing `/send` endpoint). The Spring controller annotation is `@PatchMapping("/{id}/status")`.

**Alternatives Considered**:
- `POST /api/issues/{id}/status`: Acceptable but semantically weaker; `PATCH` is more precise.
- `PUT /api/issues/{id}`: Would require full issue body; increases payload and coupling.

---

## Decision 3: OOB Swap Strategy for Column Updates

**Decision**: The `PATCH` endpoint returns the **destination column content** as the primary response body, and the **source column content** as an `hx-swap-oob="innerHTML"` sibling in the same response. The `htmx.ajax()` call targets the destination column's list `<div>` by ID.

**Rationale**: A single HTTP round-trip updates both affected columns atomically. This matches the spec requirement (FR-005): "only affected columns' content updated." The `column-content` fragment in `issue-card.html` already renders a list of cards for a given status — it can be reused for both the primary body and the OOB element.

**Column ID → Status Mapping** (deterministic, used by JS and OOB targets):

| Status enum value | Column list `id` | HTMX target selector |
|---|---|---|
| `PREPARED` | `prepared-list` | `#prepared-list` |
| `IN_PROGRESS` | `in-progress-list` | `#in-progress-list` |
| `ACKNOWLEDGED` | `acknowledged-list` | `#acknowledged-list` |
| `RESOLVED` | `resolved-list` | `#resolved-list` |
| `WONT_DO` | `wont-do-list` | `#wont-do-list` |

**Alternatives Considered**:
- Update only the destination column (one-sided swap): Source column would show stale data until the 5-second poll fires. Violates SC-005 (no flicker outside affected columns).
- WebSocket push: Over-engineered for this scale; polling + OOB swap is sufficient.

---

## Decision 4: Error Recovery on Failed Drop

**Decision**: Use **pessimistic (server-authoritative) UI** — no optimistic DOM move. The card does not visually relocate until the server responds successfully. On error (4xx/5xx), HTMX's default behavior (no swap) leaves the card in its original column.

**Rationale**: The simplest correct behavior. The board already uses this model for send/status buttons (`hx-swap="none"`). Optimistic UI with rollback adds JS complexity and state management that is not worth it for this use case.

**Error rendering**: For invalid status transitions, the server returns HTTP 422 with the source column's `column-content` fragment. The HTMX meta-config tag enables 422 responses to trigger a swap (replacing the destination column target with the source column's content, effectively a visual no-op since the card was never moved). A toast notification is appended via `HX-Trigger` response header + `hx-swap-oob` into `#toast-container`.

**Alternatives Considered**:
- Optimistic move + rollback: More responsive feel, but ~80 extra lines of JS and state management.
- Full column re-fetch on error: Uses `htmx.ajax('GET', ...)` to reload source; achieves same result with one extra request.

---

## Decision 5: CSRF Token Handling for HTMX AJAX Calls

**Decision**: Inject CSRF token into every HTMX request via the `htmx:configRequest` event listener, reading from Thymeleaf-rendered `<meta>` tags in the `<head>`.

**Rationale**: Spring Security requires CSRF for `POST`/`PATCH`/`PUT`/`DELETE` requests. HTMX 2.x does **not** automatically include CSRF tokens. The existing codebase disables CSRF (`csrf.disable()` in `SecurityConfig`) — this decision needs verification during implementation, but the meta tag approach is defensive and correct for both configurations.

**Implementation**:
```html
<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>
```
```javascript
document.body.addEventListener('htmx:configRequest', function(event) {
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    if (token && header) event.detail.headers[header] = token;
});
```

**Alternatives Considered**:
- CSRF disabled globally: Already done in this project. The listener is still safe to include as a no-op when CSRF is disabled.

---

## Decision 6: Status Transition Rules for Drag-and-Drop

**Decision**: Drag-and-drop honors the same transition rules as the existing `IssueService.validateStatusTransition()` with one relaxation: **drag from `PREPARED` to `IN_PROGRESS` is permitted** (representing the scenario where a user moves a card forward without using the email send flow). The existing code currently blocks all transitions out of `PREPARED`, which must be relaxed for DnD.

**Transition table for drag-and-drop**:

| From \ To | PREPARED | IN_PROGRESS | ACKNOWLEDGED | RESOLVED | WONT_DO |
|---|---|---|---|---|---|
| PREPARED | — (no-op) | ✅ | ✗ | ✗ | ✅ |
| IN_PROGRESS | ✗ | — (no-op) | ✅ | ✗ | ✅ |
| ACKNOWLEDGED | ✗ | ✗ | — (no-op) | ✅ | ✅ |
| RESOLVED | ✅ (reopen) | ✗ | ✗ | — (no-op) | ✅ |
| WONT_DO | ✅ (reopen) | ✗ | ✗ | ✗ | — (no-op) |

**Note**: `PREPARED → IN_PROGRESS` via DnD does NOT trigger email sending. The `is_sent` flag remains false. Sending email is a separate action (the Send button).

**Alternatives Considered**:
- Block PREPARED → IN_PROGRESS on DnD: More restrictive, but contradicts spec Assumption: "direct drag to IN_PROGRESS from PREPARED is permitted."

---

## Decision 7: Material Design 3 Implementation Approach

**Decision**: Hand-crafted CSS following MD3 guidelines (existing approach). Upgrade `material.css` in place with: exact elevation shadow values, MD3 motion easing curves, `::before` pseudo-element state layers, and corrected outline color tokens.

**Rationale**: Material Web Components (MWC) are incompatible with HTMX due to Shadow DOM blocking HTMX attribute discovery (`hx-*` inside shadow roots are invisible to HTMX's DOM traversal). The 5-second polling also causes Lit re-renders resulting in visual flicker. MDBootstrap is MD2-era, not MD3. Hand-crafted CSS has zero HTMX compatibility issues and is already in use.

**MD3 shadow values (exact)**:
```css
/* Level 1 — cards at rest (2dp) */
box-shadow: 0 1px 2px 0 rgba(0,0,0,0.30), 0 1px 3px 1px rgba(0,0,0,0.15);

/* Level 2 — cards hovered */
box-shadow: 0 1px 2px 0 rgba(0,0,0,0.30), 0 2px 6px 2px rgba(0,0,0,0.15);

/* Level 3 — cards being dragged (8dp equivalent in MD3) */
box-shadow: 0 4px 8px 3px rgba(0,0,0,0.15), 0 1px 3px 0 rgba(0,0,0,0.30);
```

**MD3 motion easing curves**:
```css
--md-motion-standard:   cubic-bezier(0.2, 0, 0, 1);       /* within-view transitions */
--md-motion-decelerate: cubic-bezier(0.05, 0.7, 0.1, 1.0); /* elements entering */
--md-motion-accelerate: cubic-bezier(0.3, 0, 0.8, 0.15);  /* elements exiting */
```

**State layer opacities** (using `::before` pseudo-element at `opacity`):
- Hover: `0.08`
- Focus-visible: `0.12`
- Active/pressed: `0.12`
- Dragged: `0.16`

**Alternatives Considered**:
- Material Web Components: Shadow DOM + HTMX incompatibility is a blocker.
- MDBootstrap: Not MD3; rejected.

---

## Decision 8: Polling vs Event-Driven Refresh During DnD

**Decision**: Keep the existing 5-second polling for general column freshness. **Disable polling on a column while a drag operation is active** to prevent a poll from overwriting the optimistic (empty slot) state mid-drag. Re-enable on `dragend`.

**Implementation**:
```javascript
let dragActive = false;

document.addEventListener('dragstart', () => { dragActive = true; });
document.addEventListener('dragend',   () => { dragActive = false; });

// Intercept HTMX polling requests during drag
document.body.addEventListener('htmx:beforeRequest', function(evt) {
    if (dragActive && evt.detail.requestConfig.triggerSpec?.trigger === 'every') {
        evt.preventDefault(); // cancel the poll
    }
});
```

**Alternatives Considered**:
- Event-driven refresh via `HX-Trigger` header: More elegant but requires replacing the polling trigger on all five columns. Deferred to a future improvement.
- No mitigation: A poll firing mid-drag can move a card back to its "sent" position visually (if the server state changes between drag start and drop). Low probability but possible.

---

## Resolved NEEDS CLARIFICATION items

All items from spec were clear or resolved via research. No NEEDS CLARIFICATION markers remain.

**Status Transition Relaxation**: `IssueService.validateStatusTransition()` blocks all moves from `PREPARED`. This must be updated to allow `PREPARED → IN_PROGRESS` and `PREPARED → WONT_DO` via the new DnD endpoint. This is a targeted service-layer change, not a schema change.

**CSRF state**: `SecurityConfig` has `csrf.disable()` — confirmed by code exploration. The CSRF meta tag + listener is included as a defensive measure in case CSRF is re-enabled.
