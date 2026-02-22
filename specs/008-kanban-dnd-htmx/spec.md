# Feature Specification: Kanban Board with Drag-and-Drop

**Feature Branch**: `008-kanban-dnd-htmx`  
**Created**: 2026-02-22  
**Status**: Draft  
**Input**: User description: "Build a Jira-like Kanban board web app with drag-and-drop functionality using HTMX + Thymeleaf, styled with Material Design."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Drag Card to New Column (Priority: P1)

A user views the Kanban board and drags an issue card from one status column to another. The card moves to the target column, the new status is persisted, and only the affected columns refresh — no full page reload.

**Why this priority**: This is the core interaction of the feature. Without working drag-and-drop with persistence, the board provides no value beyond read-only display.

**Independent Test**: Load the board with at least two issues in different columns, drag one card to a different column, and verify the card appears in the new column and the change survives a page refresh.

**Acceptance Scenarios**:

1. **Given** a board with issue cards in multiple columns, **When** a user drags a card to a valid target column, **Then** the card appears in the new column, its status is updated, and no full page reload occurs.
2. **Given** a user dragging a card, **When** the card is held over a valid target column, **Then** the column visually highlights to indicate it is a valid drop zone.
3. **Given** a user dragging a card, **When** the drag is cancelled (e.g., Escape key or drop outside any column), **Then** the card returns to its original column without any status change.

---

### User Story 2 - Visual Feedback While Dragging (Priority: P2)

While a user drags a card, it visually separates from the board surface — elevation increases and opacity drops — making it clear that the card is in motion. When hovering over a valid column, that column highlights to reinforce the drop target.

**Why this priority**: Clear drag feedback is essential for usability and prevents accidental drops; it directly supports the P1 drag interaction.

**Independent Test**: Drag any card and visually confirm the card shadow deepens and opacity decreases during drag, and the hovered column shows a tinted overlay.

**Acceptance Scenarios**:

1. **Given** a user starts dragging a card, **When** the drag begins, **Then** the card's visual elevation increases (deeper shadow) and its opacity decreases slightly.
2. **Given** a card is being dragged, **When** it is held over a valid target column, **Then** the column shows a primary-color tinted surface overlay.
3. **Given** a card is being dragged, **When** it leaves a column (no longer hovering), **Then** the column highlight is removed.

---

### User Story 3 - Board Overview and Column Layout (Priority: P3)

A user views the full Kanban board with all status columns displayed side by side. Each column shows its name, a count of its issues, and all cards belonging to that status. The layout follows Material Design 3 styling with consistent surface tones, typography, and chip-style priority badges.

**Why this priority**: A readable, well-styled board is the foundation for all interaction. Without it, the drag-and-drop feature lacks context.

**Independent Test**: Load the board and verify all five columns render with correct headers, issue counts, and cards styled with Material Design 3 conventions.

**Acceptance Scenarios**:

1. **Given** the board loads, **When** there are issues with different statuses, **Then** each issue card appears in the correct column.
2. **Given** a card is displayed, **When** a user reads it, **Then** it shows the issue title, priority badge, and assignee with correct Material type scale.
3. **Given** no issues exist in a column, **When** the board loads, **Then** the empty column still renders with its header and an empty state indicator.

---

### User Story 4 - Partial Fragment Re-render (Priority: P4)

After a successful drag-and-drop, only the source and target columns are re-rendered by the server. The rest of the page remains unchanged, preserving scroll position and the state of other columns.

**Why this priority**: Partial re-rendering is a key performance and UX quality requirement; it separates a reactive board from a naive full-page-reload approach.

**Independent Test**: Monitor network requests during a drag-and-drop and verify only column fragment responses are returned, not a full-page HTML response.

**Acceptance Scenarios**:

1. **Given** a successful card drop, **When** the server responds, **Then** only the affected columns are updated in the DOM.
2. **Given** other columns have cards, **When** a card is moved between two specific columns, **Then** the other columns are not re-rendered or reset.

---

### Edge Cases

- What happens when a card is dropped onto its own column (no status change)?
- How does the system handle a drop event while the server request is in flight (prevent double-submission)?
- What if the server returns an error for an invalid status transition — is the card visually reverted?
- What happens when two users drag the same card simultaneously (concurrent updates)?
- How does the board behave on touch devices where HTML5 drag-and-drop is not natively supported?
- What is shown when the board has no issues at all?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The board MUST display all status columns (PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO) side by side on a single screen.
- **FR-002**: Each column MUST show its name, total issue count, and all cards belonging to that status.
- **FR-003**: Each issue card MUST display the issue title, priority badge, and assignee.
- **FR-004**: Users MUST be able to drag a card from one column and drop it onto another column to change the issue's status.
- **FR-005**: On a successful drop, the system MUST persist the new status to the backend without a full page reload.
- **FR-006**: On a successful drop, the system MUST re-render only the source and target columns using server-returned partial fragments.
- **FR-007**: The dragged card MUST visually increase in elevation (deeper shadow) and decrease in opacity while being dragged.
- **FR-008**: A column being dragged over MUST display a primary-color tinted surface overlay to indicate it is a valid drop target.
- **FR-009**: If a card is dropped outside any valid column or the drag is cancelled, the card MUST return to its original column with no status change.
- **FR-010**: If the server returns an error for a drop (e.g., invalid status transition), the card MUST be visually reverted to its original column and an error indication shown.
- **FR-011**: All column headers MUST include an emoji prefix per the existing board convention.
- **FR-012**: Priority badges MUST be styled as Material Design chip components with emoji prefix per the existing convention.
- **FR-013**: All animations and transitions MUST use the Material Motion emphasized easing curve.
- **FR-014**: Dropping a card onto its own column MUST be a no-op (no server request, no visual change).

### Key Entities

- **Issue**: Represents a tracked item. Key attributes: id, title, description, status, priority, assignee, is_send. Status determines which column the card appears in.
- **Status**: An ordered set of states (PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO). Defines valid transitions and column placement.
- **Column**: A visual grouping of issues sharing the same status. Displays a count and renders as a server-side partial fragment.
- **Card**: The visual representation of an issue on the board. Draggable, showing title, priority, and assignee.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can drag a card from one column to another and see the change reflected within 1 second under normal network conditions.
- **SC-002**: After a drag-and-drop, the moved card persists in its new column after a full page refresh.
- **SC-003**: Only the two affected columns are updated in the DOM per drop action — verified by network and DOM inspection showing no full-page reload.
- **SC-004**: The board renders all five columns correctly with 0 layout or styling regressions compared to the existing board view.
- **SC-005**: All drag-related visual feedback (card elevation increase, column highlight, card opacity drop) is visible and consistent across supported browsers (Chrome, Firefox, Edge).
- **SC-006**: Invalid status transitions return a visible error to the user without corrupting the board state.

## Assumptions

- The existing status transition rules (PREPARED → IN_PROGRESS, IN_PROGRESS → ACKNOWLEDGED, etc.) remain in force; drag-and-drop respects the same validation logic already implemented on the backend.
- Only authenticated users can interact with the board; role-based restrictions (e.g., only ADMIN can send emails) are unchanged.
- Touch device support (mobile drag-and-drop) is out of scope for this feature; desktop browser support only.
- The board column order matches the existing status workflow order.
- Concurrent drag-and-drop conflicts (two users moving the same card) are handled by last-write-wins with no special UI warning for this iteration.
- The backend already exposes or will expose an endpoint to update issue status via drag-and-drop (reusing or extending the existing `POST /api/issues/{id}/status`).
