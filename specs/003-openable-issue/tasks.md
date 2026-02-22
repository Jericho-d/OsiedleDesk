# Tasks: Openable Issue Detail View

**Change**: `003-openable-issue`  
**Date**: 2026-02-22  
**Branch**: `003-openable-issue`

## Implementation Checklist

Tasks are ordered by dependency. Complete each task before starting the next group.

---

### Group 1 — Backend: New Fragment Endpoint

- [x] **T-01** Add `GET /fragments/issues/{id}/detail` to `FragmentController`
  - Method: `getIssueDetailFragment(@PathVariable Long id, Model model)`
  - Calls `issueService.getIssueById(id)` → adds `issue` to model
  - Returns `"fragments/issue-detail :: issue-detail"`
  - If issue is null: return `"fragments/issue-detail :: issue-not-found"`
  - File: `src/main/java/com/administrativetool/controller/view/FragmentController.java`

- [x] **T-02** Add `POST /api/issues/{id}/send/detail` to `IssueViewController`
  - `@PreAuthorize("hasRole('ADMIN')")`
  - Same logic as existing `/send` but returns `"fragments/issue-detail :: issue-detail"`
  - File: `src/main/java/com/administrativetool/controller/view/IssueViewController.java`

- [x] **T-03** Add `POST /api/issues/{id}/status/detail` to `IssueViewController`
  - `@PreAuthorize("hasRole('ADMIN')")`
  - Same logic as existing `/status` but returns `"fragments/issue-detail :: issue-detail"`
  - File: `src/main/java/com/administrativetool/controller/view/IssueViewController.java`

---

### Group 2 — Frontend: Detail Fragment Template

- [x] **T-04** Create `src/main/resources/templates/fragments/issue-detail.html`
  - Fragment `issue-detail`: full overlay with backdrop + panel
  - Panel sections: header (title + close button), description, metadata, admin actions
  - Metadata fields: status badge, priority badge, assignee (or "Unassigned"), created date, updated date, sent indicator
  - Admin actions (`sec:authorize="hasRole('ADMIN')"` wrapper):
    - Status dropdown → `POST /api/issues/{id}/status/detail`, `hx-trigger="change"`, `hx-target="#issue-modal"`, `hx-swap="innerHTML"`
    - Send button (only if `PREPARED` and `!issue.sent()`) → `POST /api/issues/{id}/send/detail`, `hx-target="#issue-modal"`, `hx-swap="innerHTML"`
  - Fragment `issue-not-found`: simple error message + close button
  - All dates formatted with Thymeleaf `#temporals.format(issue.createdAt(), 'yyyy-MM-dd HH:mm')`

- [x] **T-05** Add modal CSS to `board/index.html` `<style>` block
  - `.issue-modal-backdrop`: fixed, full-screen, semi-transparent dark overlay, `z-index: 1000`
  - `.issue-detail-panel`: centered card, max-width 640px, white background, border-radius, overflow-y auto, max-height 90vh
  - `.detail-header`: flex row, space-between, border-bottom
  - `.detail-description`: `white-space: pre-wrap`, full text display
  - `.detail-meta`: label–value grid layout
  - Responsive: on `max-width: 600px`, panel uses full width with small margin
  - Total new CSS: aim for < 80 lines

---

### Group 3 — Frontend: Wire Up Card Click + Modal Container

- [x] **T-06** Add `#issue-modal` container div to `board/index.html`
  - Place just before `</body>`
  - `<div id="issue-modal"></div>`

- [x] **T-07** Add click-to-open HTMX attributes to `.issue-card` in `issue-card.html`
  - On the outer `th:fragment="issue-card"` div, add:
    - `th:hx-get="@{'/fragments/issues/' + ${issue.id()} + '/detail'}"`
    - `hx-target="#issue-modal"`
    - `hx-swap="innerHTML"`
  - Ensure child element HTMX events (send button, status dropdown) are not blocked — HTMX handles this natively

- [x] **T-08** Add JS dismiss logic to `board/index.html`
  - Inline `<script>` block (< 20 lines)
  - Clear `#issue-modal` innerHTML on:
    - Escape key (`keydown` listener)
    - Click on backdrop element (event delegation: check `e.target.id === 'issue-modal-backdrop'`)
  - Close button in the detail fragment already sets `onclick="document.getElementById('issue-modal').innerHTML=''"` or uses `hx-on:click`

---

### Group 4 — Tests

- [x] **T-09** Add tests to `FragmentControllerTest` (create if it doesn't exist)
  - `@ExtendWith(MockitoExtension.class)` unit test (Spring Boot 4 removed `@WebMvcTest`)
  - Test: detail endpoint returns correct view name and model when issue exists
  - Test: detail endpoint returns not-found view name when issue is null
  - Test: issue with null assignee is populated in model correctly
  - File: `src/test/java/com/administrativetool/controller/view/FragmentControllerTest.java`

---

### Group 5 — Acceptance Verification

- [x] **T-10** Manual verification against spec acceptance scenarios
  - US1-AC1: Click card → detail opens with all fields
  - US1-AC2: Escape / click outside → closes without page reload
  - US1-AC3: Long description visible in full
  - US2-AC1: Priority and assignee visible as distinct fields
  - US2-AC2: Sent indicator visible when `sent=true`
  - US2-AC3: Long description scrollable
  - US3-AC1: Admin changes status → board and detail both reflect new status
  - US3-AC2: Regular user sees no status control
  - US3-AC3: Board updates without page reload (within 5s poll)
  - US4-AC1: Admin sends PREPARED issue → status IN_PROGRESS + sent badge in detail
  - US4-AC2: Already sent issue → Send button disabled/hidden
  - US4-AC3: Regular user sees no Send button

- [x] **T-11** Run `./gradlew build` — verify no compilation errors
- [x] **T-12** Run `./gradlew test` — all new tests pass (4 pre-existing failures unrelated to this feature)

---

## Notes

- Keep total new JS under 20 lines (modal dismiss script in `board/index.html`)
- Keep total new CSS under 80 lines (modal styles in `board/index.html`)
- New Thymeleaf fragment must not exceed ~100 lines
- All existing HTMX behaviour on the board cards must continue to work unchanged
