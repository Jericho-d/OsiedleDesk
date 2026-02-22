## Context

The board has 5 columns (PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WONT_DO). Currently there is no manual status-transition UI — the only state change is the Send Email flow which gates PREPARED → IN_PROGRESS behind an email delivery. `IssueService.updateIssueStatus()` already exists and has a `validateStatusTransition` stub, but there is no HTTP endpoint or UI that calls it.

The issue card fragment (`issue-card.html`) and detail modal fragment (`issue-detail.html`) both render admin controls. The view controller (`IssueViewController`) handles the send flow and returns HTMX fragment swaps. There is no existing `/api/issues/{id}/status` endpoint on either the API or view controller.

## Goals / Non-Goals

**Goals:**
- Expose a server-side status-change endpoint (`POST /api/issues/{id}/status`) on the view controller, returning an updated card fragment (OOB swap) and refreshing the modal if open
- Display status-move buttons in the issue card footer (admin only) and in the detail modal admin actions section
- Show only the valid next statuses for each issue — PREPARED shows no move buttons (send-only), all others show their legal transitions
- Redesign the Send Email button to be visually prominent and clearly branded
- Add CSS keyframe animations for card entry (slide-in) and a "moving" flash when a card is updated out-of-band
- Add emoji decorations to column headers and status/priority badges
- Keep all existing HTMX attributes, fragment names, and ids untouched

**Non-Goals:**
- Drag-and-drop between columns (mouse drag API)
- Real-time multi-user sync beyond the existing 5-second HTMX poll
- Changing the send email flow itself (endpoints, email content, logic)
- USER role getting status-move capability (admin-only)

## Decisions

### 1 — Status-move endpoint on the view controller (not API controller)

The view controller already owns fragment-returning endpoints (`/send`, `/send/detail`). Adding `POST /api/issues/{id}/status?target=IN_PROGRESS` here lets us return an OOB card fragment directly, matching the existing pattern. The API controller (`IssueController`) is for JSON REST consumers; mixing Thymeleaf fragment returns there would be confusing.

Alternative: add it to `IssueController` as a JSON endpoint and use client-side JS to reload the column. Rejected — adds client complexity and breaks the HTMX-first pattern.

### 2 — Allowed transitions enforced server-side only; UI shows only valid buttons

The server's `validateStatusTransition` already guards illegal moves. The UI layer shows only legal next-state buttons (computed in the Thymeleaf template via `th:if` expressions), providing a clean UX. This means no extra client-side validation script is needed.

Allowed moves (UI buttons shown):
- PREPARED → (none via move button; send-only)
- IN_PROGRESS → ACKNOWLEDGED, WONT_DO
- ACKNOWLEDGED → RESOLVED, WONT_DO
- RESOLVED → PREPARED (reopen), WONT_DO
- WONT_DO → PREPARED (reopen)

### 3 — OOB swap strategy for status moves

When a status-move request succeeds, the server returns the updated card fragment with `hx-swap-oob="outerHTML:#issue-{id}"`. This causes the card to re-render in its new column on the next 5-second poll. The modal, if open, will be refreshed by including a second OOB target (`innerHTML:#issue-modal`) containing the updated detail fragment. This matches the established pattern from the send flow.

Alternative: trigger a full column refresh via `HX-Trigger` response header. Viable but adds JS event listener complexity. OOB swap is simpler.

### 4 — Send button redesign: Material "contained" button with icon

Replace `.btn-send` (small green inline style) with a full-width `.md-btn-send` that uses the primary blue (`#1976D2`), a paper-plane emoji prefix, uppercase label, box shadow, and ripple hover effect. This sits at the bottom of the admin actions area as a primary CTA.

### 5 — CSS animations: `@keyframes card-enter` + `.card-flash`

- All issue cards get `animation: card-enter 0.25s ease-out` on mount (CSS class on `.issue-card`)
- When a card is replaced via OOB swap, HTMX adds `.htmx-swapping` / `.htmx-settling` classes — we style these with a brief blue highlight flash (`card-flash` keyframe on `.issue-card.htmx-settling`)
- No JS required

## Risks / Trade-offs

- [Column poll vs OOB swap race] If the 5-second poll fires just after a status move, the card may flash twice. Mitigation: the poll is idempotent (re-renders same HTML), so the flash is cosmetic only.
- [Thymeleaf th:if complexity] Multiple nested `th:if` expressions for allowed transitions can become hard to read. Mitigation: use Thymeleaf local variables (`th:with`) to keep logic tidy.
- [Modal refresh OOB] The detail modal OOB swap only works if `#issue-modal` is in the DOM. If the modal is closed, the OOB swap is a no-op (HTMX ignores missing targets), which is the desired behaviour.
