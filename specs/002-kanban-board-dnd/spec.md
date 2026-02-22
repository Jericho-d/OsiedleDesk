# Feature Specification: Kanban Board with Drag-and-Drop

**Feature Branch**: `002-kanban-board-dnd`  
**Created**: 2026-02-22  
**Status**: Draft  
**Input**: User description: "Build a Jira-like Kanban board web app with drag-and-drop functionality using HTMX + Thymeleaf, styled with Material Design."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Board and Issue Columns (Priority: P1)

A user opens the Kanban board and sees all issues organized across five status columns: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, and WON'T_DO. Each issue is displayed as a card with its title, priority badge, and assignee. The board gives an at-a-glance overview of all work in progress.

**Why this priority**: This is the foundation of the board — without being able to view issues in their columns, no other feature is meaningful. It establishes the core layout, styling, and data-rendering pipeline.

**Independent Test**: Can be fully tested by loading the board page with seeded data and verifying all issues appear in their correct columns with correct metadata displayed.

**Acceptance Scenarios**:

1. **Given** there are issues in various statuses, **When** a user navigates to the board, **Then** each issue appears under its corresponding status column with title, priority chip, and assignee visible.
2. **Given** a column has no issues, **When** the board loads, **Then** the column still renders with its header and an empty state indication.
3. **Given** issues with different priorities, **When** a user views the board, **Then** priority badges are visually distinct (e.g., color-coded chips for LOW, MEDIUM, HIGH, CRITICAL).

---

### User Story 2 - Drag an Issue Card to a New Column (Priority: P1)

A user grabs an issue card from one column and drops it onto a different column. The board immediately reflects the updated status without a full page reload. The change is persisted so that other users (or the same user after a refresh) see the new status.

**Why this priority**: Drag-and-drop is the defining interaction of a Kanban board. Without it, the board is read-only and provides no task management value.

**Independent Test**: Can be fully tested by dragging a card between two columns and verifying the card appears in the target column both visually and after a page refresh.

**Acceptance Scenarios**:

1. **Given** a card is in the PREPARED column, **When** the user drags it to IN_PROGRESS and drops it, **Then** the card moves to IN_PROGRESS, the PREPARED column no longer shows the card, and the change is saved.
2. **Given** a user drags a card, **When** the card is being dragged, **Then** it displays increased shadow depth and reduced opacity to signal it is being moved.
3. **Given** a user drags a card over a valid target column, **When** the card hovers over the column, **Then** the target column shows a highlighted tinted overlay to indicate it is a valid drop zone.
4. **Given** a user drags a card and releases it outside any column, **When** the drop is cancelled, **Then** the card returns to its original column with no change persisted.
5. **Given** a user drops a card on the same column it originated from, **When** the drop completes, **Then** no status change is made and no server request is triggered.

---

### User Story 3 - Create a New Issue (Priority: P2)

A user creates a new issue by entering a title, description, priority, and assignee. The new issue appears on the board in the PREPARED column immediately.

**Why this priority**: The ability to add new issues is essential for the board to be useful as a living task tracker, but the core viewing and drag-and-drop experience is independently valuable even without creation.

**Independent Test**: Can be fully tested by submitting a new issue form and verifying the card appears in PREPARED with the correct data.

**Acceptance Scenarios**:

1. **Given** the user accesses the issue creation form, **When** they fill in title, description, priority, and assignee and submit, **Then** a new issue card appears in the PREPARED column.
2. **Given** a user submits the form without a required field (title), **When** submission is attempted, **Then** an error message is shown and no issue is created.
3. **Given** a new issue is created, **When** the board reloads, **Then** the new issue persists and is still visible in PREPARED.

---

### User Story 4 - Send Issue to Administrative Company (Priority: P2)

An authorized user sends an issue to the Administrative Company via email. After sending, the issue's status automatically advances and a "sent" indicator appears on the card. This prevents accidental duplicate sends.

**Why this priority**: This is a key business action in the property management workflow — but it depends on the board and issue viewing working correctly first.

**Independent Test**: Can be fully tested by clicking the send button on a PREPARED issue and verifying the status change, the sent indicator, and that the button is no longer available for re-sending.

**Acceptance Scenarios**:

1. **Given** a PREPARED issue, **When** an admin clicks the "Send" button, **Then** an email is sent to the Administrative Company, the issue status changes to IN_PROGRESS, and the card shows a "sent" indicator.
2. **Given** an issue already marked as sent, **When** the card is viewed, **Then** the "Send" button is disabled or hidden to prevent duplicate sends.
3. **Given** a non-admin user, **When** they view an issue card, **Then** the "Send" button is not visible or is disabled.

---

### User Story 5 - Change Issue Status Manually (Priority: P3)

An authorized user changes an issue's status by selecting from the allowed transitions (e.g., ACKNOWLEDGED → RESOLVED, any → WON'T_DO). The board updates to reflect the new column placement immediately.

**Why this priority**: Manual status changes (outside of drag-and-drop) ensure the board can be managed without dragging, which is important for accessibility and edge-case workflows like rejection.

**Independent Test**: Can be fully tested by triggering a status change action on a card and verifying the card moves to the correct column.

**Acceptance Scenarios**:

1. **Given** an IN_PROGRESS issue, **When** an admin selects ACKNOWLEDGED as the new status, **Then** the card moves to the ACKNOWLEDGED column.
2. **Given** any issue, **When** an admin selects WON'T_DO, **Then** the card moves to the WON'T_DO column regardless of current status.
3. **Given** a RESOLVED issue, **When** an admin selects PREPARED (reopen), **Then** the card moves back to the PREPARED column and the is_send flag is reset.
4. **Given** a non-admin user, **When** they attempt an unauthorized status change, **Then** the action is rejected and the card stays in its current column.

---

### Edge Cases

- What happens when two users drag the same card simultaneously? The last-write-wins outcome is acceptable; no optimistic locking is required for this initial version.
- What happens when a user drags a card to a column that represents an invalid status transition? The system should reject the drop and return the card to its original position; an inline error message should inform the user.
- What happens when the server returns an error during a drag-and-drop save? The card should return to its original column and a non-blocking error notification should be shown.
- What happens when the board has a very large number of cards (50+) in a single column? The column should scroll independently without breaking the overall board layout.
- What happens if a user's session expires mid-drag? On drop, the server will return a 401/403 response; the UI should redirect to the login page or show an authentication error.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The board MUST display all issues organized into five status columns: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, and WON'T_DO.
- **FR-002**: Each issue card MUST display at minimum: title, priority badge, and assignee name.
- **FR-003**: Users MUST be able to drag an issue card from one column and drop it onto another column to change the issue's status.
- **FR-004**: On a successful drag-and-drop, the system MUST persist the new status immediately without a full page reload.
- **FR-005**: The system MUST update only the affected columns' content after a status change, not the entire page.
- **FR-006**: While a card is being dragged, it MUST display elevated visual treatment (deeper shadow, reduced opacity) to distinguish it from resting cards.
- **FR-007**: A column being hovered over during a drag MUST display a highlighted overlay to indicate it is a valid drop target.
- **FR-008**: Dropping a card outside a valid column MUST cancel the operation and return the card to its original position.
- **FR-009**: Dropping a card onto the same column it came from MUST be a no-op (no server request, no visual change).
- **FR-010**: The system MUST prevent sending the same issue to the Administrative Company more than once (idempotency via is_send flag).
- **FR-011**: Sending an issue MUST automatically update the issue status from PREPARED to IN_PROGRESS.
- **FR-012**: The "Send to Administrative Company" action MUST be restricted to users with the ADMIN role.
- **FR-013**: All interactive card and column elements MUST provide visual feedback for hover, pressed, and active states.
- **FR-014**: Animations and transitions MUST use smooth, consistent motion curves for drag events, column highlights, and card movements.
- **FR-015**: The board MUST be accessible without horizontal scroll on standard desktop viewport widths (1280px and above).
- **FR-016**: An empty column MUST still render with its header and an empty state placeholder.
- **FR-017**: Invalid status transitions attempted via drag-and-drop MUST be rejected by the server; the card MUST return to its original column and an error notification MUST be displayed.
- **FR-018**: Users MUST be able to create new issues with a title, description, priority, and assignee; new issues default to PREPARED status.
- **FR-019**: Issue creation MUST validate that the title field is not empty; submission with a missing title MUST show an inline error.

### Key Entities

- **Issue**: Represents a task or problem in the property management workflow. Key attributes: title, description, status (workflow stage), priority (urgency level), assignee (responsible party), sent flag (whether the administrative company has been notified).
- **Status**: An enumerated workflow stage for an issue. Values and valid transitions govern how issues move through the board columns.
- **Priority**: An enumerated urgency level for an issue. Values: LOW, MEDIUM, HIGH, CRITICAL. Used for visual differentiation on the board.
- **Column**: A visual grouping on the board representing a single status value. Contains zero or more issue cards.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can drag an issue card from one column to another and see the card in the new column within 1 second of releasing the mouse button.
- **SC-002**: After a drag-and-drop status change, a page refresh shows the issue in the correct (new) column, confirming persistence.
- **SC-003**: 90% of first-time users can successfully drag a card to a new column on their first attempt without instruction.
- **SC-004**: The board loads all issues across all five columns in under 2 seconds on a standard broadband connection.
- **SC-005**: After a drag-and-drop operation completes, the page content outside the affected columns remains unchanged — headers, navigation, and other columns do not re-render or flicker.
- **SC-006**: An issue can only be sent to the Administrative Company once per lifecycle — subsequent attempts to send result in no additional email and no status change.
- **SC-007**: Drag-and-drop operations work correctly with 20 or more cards distributed across columns without visual overlap or layout breakage.
- **SC-008**: Visual feedback (card elevation change, column highlight) appears within 100ms of initiating a drag gesture.

## Assumptions

- The board is intended for desktop use; mobile/touch drag-and-drop is out of scope for this iteration.
- All five status columns are always visible on the board; column visibility is not configurable.
- Issue cards display summary information only; a click on a card may navigate to a detail view (out of scope for this iteration — assumed to be a future enhancement).
- The is_send flag can only be reset when an issue is manually reopened (status set back to PREPARED by an admin).
- The board displays all issues regardless of assignee by default; filtering and sorting are out of scope for this iteration.
- Authentication and user roles (ADMIN, USER) are already in place and this feature integrates with the existing security configuration.
- Valid status transitions enforced on drag-and-drop: any column is a valid target except that the send action (PREPARED → IN_PROGRESS) is reserved for the email send button; direct drag to IN_PROGRESS from PREPARED is permitted.
