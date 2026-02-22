# Feature Specification: Kanban Board with HTMX and Material Design

**Feature Branch**: `007-kanban-board-htmx`  
**Created**: 2026-02-22  
**Status**: Draft  
**Input**: User description: "Build a Jira-like Kanban board web app with drag-and-drop functionality using HTMX + Thymeleaf, styled with Material Design."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View and Navigate the Kanban Board (Priority: P1)

A user opens the application and sees all issues organized in columns representing their current workflow status. The board displays all issues across the five status columns: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, and WON'T_DO. Each issue card shows its title, priority badge, and assignee at a glance.

**Why this priority**: This is the foundational view — without being able to see the board, no other feature has value. Every other story builds on this.

**Independent Test**: Can be fully tested by loading the board page and verifying that issues appear in the correct columns with visible titles, priorities, and assignees. Delivers immediate value as a read-only board overview.

**Acceptance Scenarios**:

1. **Given** the application is running and issues exist in the database, **When** a user navigates to the board page, **Then** five columns are displayed (PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO) each showing the correct issues
2. **Given** a board with issues of varying priorities, **When** the user views a column, **Then** each card shows the issue title, priority badge (LOW/MEDIUM/HIGH/CRITICAL), and assignee name
3. **Given** a column with no issues, **When** the user views that column, **Then** the column displays an empty state indication rather than being hidden
4. **Given** the board is loaded, **When** a user views any issue card, **Then** the card's priority is visually distinguishable from other priority levels via colored chips/badges

---

### User Story 2 - Drag and Drop Issue Between Columns (Priority: P1)

A user grabs an issue card and drags it from one column to another. Upon releasing the card into the target column, the board updates to reflect the new status of the issue without a full page reload. The change is persisted to the backend.

**Why this priority**: Drag-and-drop is the primary interaction mechanic of a Kanban board. Without it, users cannot manage workflow state, which is the core value of the feature.

**Independent Test**: Can be tested by dragging a card from PREPARED to IN_PROGRESS and confirming the card appears in IN_PROGRESS and the database status is updated. Delivers the core Kanban workflow management value.

**Acceptance Scenarios**:

1. **Given** an issue card in the PREPARED column, **When** a user drags it and drops it onto the IN_PROGRESS column, **Then** the card moves to IN_PROGRESS, the backend is notified, and the issue status updates without a full page reload
2. **Given** a user is dragging a card, **When** the card is in motion, **Then** the card's visual elevation increases (deeper shadow) and its opacity decreases slightly to indicate it is being dragged
3. **Given** a user drags a card over a valid target column, **When** the card hovers over the column, **Then** the target column displays a visual highlight to indicate it will accept the drop
4. **Given** a drag-and-drop action completes successfully, **When** the backend responds, **Then** only the affected columns (source and destination) re-render — not the full page
5. **Given** a user drops a card onto the same column it came from, **When** the drop occurs, **Then** no status change is triggered and the card remains in place
6. **Given** a drag operation is cancelled (e.g., Escape key or drop outside any column), **When** the drag ends, **Then** the card returns to its original column with no backend request made

---

### User Story 3 - Visual Feedback During Drag Interactions (Priority: P2)

As a user drags an issue card, they receive clear Material Design-compliant visual cues: the dragged card rises in elevation, the target column highlights with a tinted overlay, and transitions use smooth motion following Material Motion principles.

**Why this priority**: Visual feedback makes the drag-and-drop interaction feel responsive and professional. It does not block functionality but significantly improves usability and perceived quality.

**Independent Test**: Can be tested by dragging a card and observing: card shadow increases, card opacity drops, and the hovered column shows a primary-color tinted background. Delivers a polished user experience independently of back-end logic.

**Acceptance Scenarios**:

1. **Given** a user starts dragging a card, **When** the drag begins, **Then** the card's elevation shadow transitions from resting (2dp) to raised (8dp) using the emphasized easing curve
2. **Given** a user drags a card over a target column, **When** the card enters the column's drop zone, **Then** the column background transitions to a primary-color tinted surface overlay
3. **Given** a user moves a card away from a column without dropping, **When** the card leaves the column's drop zone, **Then** the column highlight fades away using the same emphasized easing curve
4. **Given** any interactive element on the board (buttons, cards, chips), **When** a user hovers or presses it, **Then** the element shows a Material state layer (hover overlay or pressed ripple)

---

### User Story 4 - Identify Issues by Priority (Priority: P3)

A user can visually identify the priority of each issue through clearly differentiated priority badges on each card. The badges follow Material Chip design patterns and use color coding consistent with the design system.

**Why this priority**: Priority visibility is important for triage decisions but does not affect core workflow management. Users can still move issues without this differentiation.

**Independent Test**: Can be tested by viewing the board and confirming each card's priority chip uses distinct styling (color or label) for LOW, MEDIUM, HIGH, and CRITICAL levels.

**Acceptance Scenarios**:

1. **Given** an issue with CRITICAL priority, **When** its card is displayed on the board, **Then** a chip labeled "CRITICAL" is shown with styling that clearly distinguishes it from MEDIUM and LOW
2. **Given** an issue with LOW priority, **When** its card is displayed, **Then** the priority chip's visual treatment is noticeably less prominent than HIGH and CRITICAL chips
3. **Given** a user views the board, **When** scanning multiple cards, **Then** priority levels are visually distinguishable without needing to open each issue

---

### Edge Cases

- What happens when a user drops a card outside all columns? The card animates back to its original column and no backend request is made.
- What happens when the backend returns an error on status update? The card reverts to its original column and a user-friendly error message is displayed.
- What happens when a column has many issues (overflow)? The column scrolls independently within its bounds without affecting other columns.
- What happens during slow network conditions? The UI shows an in-progress indicator on the affected columns while the backend request is pending.
- What happens when two users move the same issue simultaneously? The last write wins; the board reflects the state returned by the backend after each request.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The board MUST display all issues organized into five columns corresponding to statuses: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, and WON'T_DO
- **FR-002**: Each issue card MUST display at minimum: issue title, priority badge, and assignee
- **FR-003**: Users MUST be able to drag an issue card from one column and drop it onto a different column to change its status
- **FR-004**: On a successful drop, the system MUST send a request to the backend to persist the new status without reloading the full page
- **FR-005**: On a successful status update, the backend MUST return updated partial views that replace only the source and destination columns
- **FR-006**: The dragged card MUST visually indicate it is being dragged by increasing its elevation shadow and reducing its opacity
- **FR-007**: The target column MUST visually highlight when a dragged card hovers over it, and the highlight MUST clear when the card leaves or is dropped
- **FR-008**: If the backend rejects a status update or an error occurs, the card MUST revert to its original column and a user-visible error indication MUST be shown
- **FR-009**: Dropping a card onto its current column MUST NOT trigger a backend request or change any state
- **FR-010**: Priority chips MUST visually differentiate between LOW, MEDIUM, HIGH, and CRITICAL priority levels
- **FR-011**: All animations and transitions MUST use the Material Motion emphasized easing curve
- **FR-012**: All interactive elements (cards, buttons, chips) MUST display Material state layers on hover and press interactions
- **FR-013**: Columns with no issues MUST remain visible and accept drops (act as valid drop targets)
- **FR-014**: Each column MUST support independent vertical scrolling when its content overflows

### Key Entities

- **Issue**: Represents a tracked work item. Key attributes: id, title, description, status (workflow column), priority (LOW/MEDIUM/HIGH/CRITICAL), assignee, is_send flag
- **Status Column**: A visual grouping of issues sharing the same workflow status. One column per status value; always visible regardless of issue count
- **Priority Badge**: A visual chip displayed on each issue card reflecting the issue's priority level. Distinct visual style per priority level

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can move an issue from one column to another and see the result reflected on the board in under 1 second from the moment of drop (on a standard broadband connection)
- **SC-002**: After a drag-and-drop action, no more than two column fragments are re-rendered — the source column and the destination column — leaving the rest of the page untouched
- **SC-003**: 100% of drag-and-drop actions that succeed on the backend result in the card appearing in the correct target column without a full page reload
- **SC-004**: 100% of drag-and-drop actions that fail on the backend result in the card returning to its original column with a visible error indication
- **SC-005**: All five status columns are visible and correctly labeled on initial page load, regardless of whether they contain any issues
- **SC-006**: Priority badges are visually distinguishable for all four priority levels when scanning the board without needing to read labels
- **SC-007**: All drag, hover, and press transitions complete their animations within 300ms, consistent with Material Motion timing guidelines

## Assumptions

- The existing `Issue` entity and its five `Status` enum values (PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO) are already defined and will be reused without modification
- The backend already exposes or will expose an endpoint to update an issue's status (e.g., a status-change endpoint for issues)
- No sorting or ordering within columns is required beyond the default order returned by the backend
- The board is accessible to authenticated users; authorization rules follow the existing role-based access control (ADMIN/USER roles)
- Touch and mobile drag-and-drop support is out of scope for this feature; desktop browser support only
- The board displays all issues across all users without filtering by the current user by default
- Issue cards are not expandable or clickable to open detail views within this feature scope; that is handled by a separate feature
