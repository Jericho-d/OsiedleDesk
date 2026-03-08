# Feature Specification: Openable Issue Detail View

**Feature Branch**: `003-openable-issue`
**Created**: 2026-02-22
**Status**: Draft
**Input**: User description: "create new feature 003-openable-issue. The main aim is to be able to click on the ticket
created before and see the popup or page with all the details as in jira"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Open Issue Detail View (Priority: P1)

As a logged-in user, I want to click on any issue card on the board and see a full detail view with all the information
about that issue, so I can understand its context without leaving the board.

**Why this priority**: This is the entire purpose of the feature. Without being able to open and read issue details, the
board cards are information-poor and users cannot act on or understand the full context of any issue.

**Independent Test**: Can be fully tested by clicking any existing issue card on the board and verifying the detail view
opens with all expected fields visible.

**Acceptance Scenarios**:

1. **Given** a logged-in user viewing the board with at least one issue, **When** they click on an issue card, **Then**
   an issue detail view opens showing the issue title, full description, status, priority, assignee, creation date, last
   updated date, and sent status.
2. **Given** an issue detail view is open, **When** the user presses Escape or clicks outside the detail area, **Then**
   the detail view closes and the board is visible again without a page reload.
3. **Given** a logged-in user on any device, **When** the detail view is open, **Then** the full description text is
   readable without truncation regardless of length.

---

### User Story 2 - View Issue Metadata in Detail (Priority: P1)

As a logged-in user, I want to see all structured metadata of an issue (status, priority, dates, who created it, whether
it was sent) in the detail view, so I have a complete picture of the issue lifecycle at a glance.

**Why this priority**: Metadata is what transforms a raw text report into a trackable work item. Users need this to
assess urgency, track progress, and avoid duplicate reports.

**Independent Test**: Can be fully tested by opening an issue detail view and verifying every metadata field is
displayed correctly, matching what was entered at creation time.

**Acceptance Scenarios**:

1. **Given** an issue was created with a specific priority and assigned to a user, **When** the detail view opens, *
   *Then** the priority label and assignee name are clearly visible as distinct fields.
2. **Given** an issue has been sent to the administrative company (is_send = true), **When** the detail view opens, *
   *Then** a visible indicator shows the issue has been sent (e.g., a badge or label).
3. **Given** an issue has a long description (500+ characters), **When** the detail view opens, **Then** the full
   description is displayed in a scrollable area without truncation.

---

### User Story 3 - Change Issue Status from Detail View (Priority: P2)

As an administrator, I want to update the status of an issue directly from the detail view, so I can manage issue
progression without navigating away or closing the detail panel.

**Why this priority**: Administrators frequently need to update status after reviewing details. Requiring them to close
the view, find a separate control, and act is unnecessary friction.

**Independent Test**: Can be fully tested by opening an issue as admin, changing its status via the detail view
dropdown, and verifying the board column updates accordingly.

**Acceptance Scenarios**:

1. **Given** a logged-in administrator viewing the detail of an IN_PROGRESS issue, **When** they change the status to
   ACKNOWLEDGED and confirm, **Then** the issue's status updates, the board column reflects the change, and the detail
   view shows the new status.
2. **Given** a logged-in regular user (non-administrator) viewing an issue detail, **Then** the status change control is
   not visible or is disabled.
3. **Given** an administrator changing status in the detail view, **When** the status is successfully changed, **Then**
   the board card moves to the correct column without requiring a full page reload.

---

### User Story 4 - Send Issue to Administration from Detail View (Priority: P2)

As an administrator, I want to send an issue to the administrative company directly from the detail view, so I can
review the full details before sending and act immediately if appropriate.

**Why this priority**: Admins should be able to review full content before sending an email. The detail view is the
natural place to confirm and act.

**Independent Test**: Can be fully tested by opening a PREPARED issue as admin, clicking Send in the detail view, and
verifying the email is dispatched and the status changes to IN_PROGRESS.

**Acceptance Scenarios**:

1. **Given** a logged-in administrator viewing the detail of a PREPARED issue, **When** they click the "Send to
   Administration" button, **Then** the email is sent, the issue status changes to IN_PROGRESS, and the sent indicator
   becomes visible in the detail view.
2. **Given** an issue already sent (is_send = true), **When** the detail view is open, **Then** the "Send to
   Administration" button is disabled or hidden with a label indicating it was already sent.
3. **Given** a logged-in regular user viewing the detail view, **Then** the "Send to Administration" button is not
   visible.

---

### Edge Cases

- What happens when the user opens an issue detail view and the issue is deleted by another session simultaneously? The
  system should show a "not found" message and close the detail view gracefully.
- What happens when the detail view is opened on a very narrow screen (mobile)? The layout should adapt to remain
  readable without horizontal scrolling.
- What happens when an issue has no assignee set? The assignee field should display a placeholder such as "Unassigned"
  rather than a blank or error.
- What happens when the user rapidly clicks multiple issue cards? Only one detail view should be open at a time; opening
  a new one replaces the previous.
- What happens when the status change fails due to a server error? The detail view should display an inline error
  message and the status should not change visually until confirmed by the server.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST open an issue detail view when a user clicks on any issue card on the board.
- **FR-002**: The issue detail view MUST display the following fields: title, full description, status, priority,
  assignee, creation date, last updated date, and sent-to-administration indicator.
- **FR-003**: The issue detail view MUST be dismissible by pressing Escape, clicking a close button, or clicking outside
  the detail panel.
- **FR-004**: Dismissing the detail view MUST NOT require a full page reload; the board MUST remain in its current
  state.
- **FR-005**: The detail view MUST display the full, untruncated description regardless of its length, with scrolling if
  necessary.
- **FR-006**: If the issue has been sent to the administrative company, the detail view MUST display a visible sent
  indicator.
- **FR-007**: For ADMINISTRATOR role only, the detail view MUST provide a status change control that allows
  transitioning to valid next states.
- **FR-008**: When an administrator changes status from the detail view, the board MUST reflect the new column placement
  without a full page reload.
- **FR-009**: For ADMINISTRATOR role only, the detail view MUST provide a "Send to Administration" button on PREPARED
  issues that have not yet been sent.
- **FR-010**: The "Send to Administration" action from the detail view MUST follow the same rules and produce the same
  outcome as sending from the board card.
- **FR-011**: Only one issue detail view MUST be open at a time; opening a new detail view MUST close any previously
  open one.
- **FR-012**: The detail view MUST be accessible on both desktop and mobile screen sizes without horizontal scrolling.
- **FR-013**: When the assignee field has no value, the detail view MUST display "Unassigned" as a placeholder.
- **FR-014**: If a status change or send action fails on the server, the detail view MUST display an inline error
  message and the displayed state MUST NOT change until the server confirms success.

### Key Entities *(include if feature involves data)*

- **Issue**: Existing entity — the detail view reads and presents all existing Issue fields: id, title, description,
  status, priority, assignee, created_at, updated_at, is_send. No new fields are required for this feature.
- **Issue Detail View**: A UI component (overlay panel or dedicated section) that presents the full Issue entity. It is
  a read-and-act surface, not a standalone data entity.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can open an issue detail view by clicking a card within 300ms of the click (perceived as instant).
- **SC-002**: 100% of issue fields (title, description, status, priority, assignee, dates, sent indicator) are visible
  in the detail view without additional navigation.
- **SC-003**: Closing the detail view takes under 200ms and leaves the board in exactly the same scroll position as
  before opening.
- **SC-004**: Status changes made from the detail view are reflected on the board within 1 second without a full page
  reload.
- **SC-005**: The detail view is fully usable (no horizontal scroll, no truncation) on screens as narrow as 375px (
  standard mobile width).
- **SC-006**: 100% of "Send to Administration" actions initiated from the detail view correctly update the issue status
  and sent indicator in the same session.
- **SC-007**: The detail view adds no more than 20KB to the total page weight (keeping the application lightweight for
  low-resource hardware).

## Assumptions

1. **Overlay/Modal approach**: The detail view opens as an overlay panel on top of the board (not a separate page
   navigation), preserving board context. This matches the Jira-style interaction described by the user.
2. **No inline editing**: The detail view is read-only for regular fields (title, description, priority, assignee).
   Editing these fields is out of scope for this feature.
3. **Existing data model is sufficient**: The Issue entity already contains all fields needed for the detail view. No
   schema migrations are required.
4. **Board remains interactive behind the overlay**: The board is visible but not interactive while the detail view is
   open, using a visual backdrop to focus attention.
5. **Same permissions model**: The existing ADMINISTRATOR / USER role distinction applies to actions in the detail view
   without any new roles or permissions.
6. **No comments or activity log**: The detail view shows current state only. A history/audit log or comments section is
   explicitly out of scope for this feature.

## Out of Scope

- Inline editing of issue title, description, priority, or assignee from the detail view
- Issue activity history or audit log
- Comments and discussion threads within the detail view
- Linking or relating issues to one another
- Attachments or file uploads within the detail view
- Sharing a deep-link URL that opens a specific issue detail directly
- Printing or exporting issue details
- Keyboard navigation between issues while the detail view is open
