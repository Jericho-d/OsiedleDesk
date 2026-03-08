# Feature Specification: Issue Email Notification to Administration

**Feature Branch**: `004-issue-email`  
**Created**: 2026-02-22  
**Status**: Draft  
**Input**: User description: "add send email button for each of the issue. Clicking the button the email should be send
to administration company with all details"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Send Issue Email from Board Card (Priority: P1)

As an administrator, I want a clearly visible "Send to Administration" button on every issue card on the board so I can
immediately dispatch an issue report by email to the administrative company without opening any extra view.

**Why this priority**: Sending an issue to the administrative company is the core workflow action of the application.
The button must be discoverable and accessible directly on the card for quick action.

**Independent Test**: Can be fully tested by logging in as an admin, viewing the board with a PREPARED issue that has
not been sent, clicking the Send button on the card, and verifying an email is dispatched to the configured
administrative company address.

**Acceptance Scenarios**:

1. **Given** a logged-in administrator viewing the board, **When** they see an issue in PREPARED status that has not
   been sent, **Then** a "Send to Administration" button is visible on the issue card.
2. **Given** an administrator clicks the "Send to Administration" button on an issue card, **When** the action succeeds,
   **Then** an email is sent to the administrative company containing all issue details (title, description, priority,
   status, assignee, dates), the issue status changes to IN_PROGRESS, and the card displays a "Sent" indicator.
3. **Given** an issue has already been sent, **When** an administrator views that issue card, **Then** the send button
   is not shown and a "Sent" badge is displayed instead.

---

### User Story 2 - Email Contains Full Issue Details (Priority: P1)

As the administrative company recipient, I want the email notification to contain all relevant details about the issue
so I can understand and act on it without needing to access the issue tracker.

**Why this priority**: The email is the primary communication channel to the external administrative company. If the
email is incomplete, the recipient cannot process the issue.

**Independent Test**: Can be fully tested by triggering a send action and verifying the received email contains title,
description, priority, status, assignee, creation date, and issue identifier.

**Acceptance Scenarios**:

1. **Given** an issue with all fields filled, **When** the email is sent, **Then** the email body contains: issue
   identifier, title, full description, priority level, current status, assignee name (or "Unassigned"), and creation
   date.
2. **Given** an issue with no assignee set, **When** the email is sent, **Then** the email body shows "Unassigned" in
   the assignee field rather than a blank or error.
3. **Given** an issue with a long description, **When** the email is sent, **Then** the full description is included in
   the email without truncation.

---

### User Story 3 - Send Issue Email from Detail View (Priority: P2)

As an administrator, I want a "Send to Administration" button available in the issue detail view so I can review all
details before sending and dispatch the email in the same flow.

**Why this priority**: Reviewing full details before sending is a secondary but important workflow. Administrators
should be able to confirm content before dispatching the email notification.

**Independent Test**: Can be fully tested by opening any PREPARED, unsent issue detail view as admin, clicking Send, and
verifying the same outcome as sending from the card.

**Acceptance Scenarios**:

1. **Given** an administrator has the detail view open for a PREPARED, unsent issue, **When** they click "Send to
   Administration", **Then** the email is dispatched, the status changes to IN_PROGRESS, and the detail view updates to
   show the sent indicator and hide the send button.
2. **Given** an administrator has the detail view open for an already-sent issue, **Then** the "Send to Administration"
   button is absent and a sent indicator is displayed.
3. **Given** a regular user (non-administrator) has the detail view open, **Then** no "Send to Administration" button is
   visible.

---

### User Story 4 - Prevent Duplicate Sends (Priority: P2)

As an administrator, I want the system to prevent an issue from being sent more than once so that the administrative
company does not receive duplicate notifications for the same issue.

**Why this priority**: Duplicate emails cause confusion and undermine trust. The system must enforce that each issue is
reported exactly once.

**Independent Test**: Can be fully tested by sending an issue successfully and then attempting to trigger the send
action again, verifying the email is only dispatched once and the UI shows no active send button.

**Acceptance Scenarios**:

1. **Given** an issue has already been sent, **When** any user attempts to trigger the send action again, **Then** no
   new email is dispatched and the system returns a success-equivalent response.
2. **Given** two simultaneous send requests for the same issue, **When** both are processed, **Then** only one email is
   sent and the issue is marked as sent exactly once.

---

### Edge Cases

- What happens when the email server is unavailable at send time? The issue status and sent flag must still be
  persisted; email delivery should be retried in the background without requiring user intervention.
- What happens if the send action is triggered for a non-existent issue? The system returns a "not found" response and
  no email is dispatched.
- What happens when the administrative company email address is not configured? Email sending fails gracefully with a
  logged error; the issue status update should still persist.
- What happens when an issue is not in PREPARED status at the time of send? The system must not regress the status; the
  send action may proceed but must not move the status backwards.
- What happens if a user rapidly double-clicks the send button? The system must not send duplicate emails; the button
  should become non-interactive after the first click.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST display a "Send to Administration" button on issue cards for issues in PREPARED status
  that have not yet been sent, visible only to ADMINISTRATOR role users.
- **FR-002**: When the "Send to Administration" button is clicked, the system MUST send an email to the configured
  administrative company address containing all issue details.
- **FR-003**: The email MUST include the following fields: issue identifier, title, full description, priority level,
  current status, assignee (showing "Unassigned" if none), and creation date.
- **FR-004**: After a successful send, the system MUST update the issue status to IN_PROGRESS and mark the issue as
  sent.
- **FR-005**: After a successful send, the issue card MUST display a visible "Sent" indicator and the send button MUST
  no longer be shown.
- **FR-006**: The system MUST prevent an issue from being sent more than once; subsequent send requests for an
  already-sent issue MUST NOT dispatch a new email.
- **FR-007**: The "Send to Administration" action MUST also be available in the issue detail view for ADMINISTRATOR role
  users, under the same conditions (PREPARED status, not yet sent).
- **FR-008**: After sending from the detail view, the detail view MUST update to reflect the sent status and new issue
  status without a full page reload.
- **FR-009**: Regular (non-administrator) users MUST NOT see the "Send to Administration" button on issue cards or in
  the detail view.
- **FR-010**: Email delivery failures MUST NOT prevent the issue's status and sent flag from being persisted; email
  delivery MUST be retried in the background.
- **FR-011**: The board card MUST reflect the new status and sent indicator within 1 second of a successful send action,
  without a full page reload.

### Key Entities

- **Issue**: Existing entity — relevant fields are `id`, `title`, `description`, `status`, `priority`, `assignee`,
  `created_at`, `updated_at`, `is_sent`, `sent_at`. No new fields are required for this feature.
- **Email Notification**: A transient outbound message sent to the administrative company email address. Not persisted
  as a separate entity.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An administrator can send an issue to the administrative company in 1 click from the board, with the card
  reflecting the sent state within 1 second of the action.
- **SC-002**: 100% of sent emails contain all required fields (identifier, title, description, priority, status,
  assignee, creation date) with no truncation.
- **SC-003**: No issue is emailed to the administrative company more than once; duplicate send rate is 0%.
- **SC-004**: Email delivery failures do not result in data inconsistency — issue status and sent flag remain correct
  regardless of email server availability.
- **SC-005**: The send button is never visible to non-administrator users; role enforcement has 0% bypass rate.
- **SC-006**: The board card and detail view reflect updated status and sent indicator within 1 second of a successful
  send action, without a full page reload.

## Assumptions

1. **Administrator-only action**: Sending an issue to the administrative company is restricted to the ADMINISTRATOR
   role. Regular users can view issue details but cannot initiate the email dispatch.
2. **PREPARED status prerequisite**: The send button is only shown for issues in PREPARED status that have not been
   sent. Issues already sent or in other statuses do not show the button.
3. **Single administrative company recipient**: There is one configured email recipient (the administrative company).
   Multi-recipient or CC/BCC functionality is out of scope.
4. **Email infrastructure already exists**: The application already has SMTP configuration and email sending capability.
   This feature builds on the existing infrastructure.
5. **Status transitions are enforced server-side**: The UI controls are a convenience — the server validates all state
   changes independently.
6. **No email template customization**: The email format is fixed (plain text with all required fields). Rich HTML or
   user-configurable templates are out of scope.

## Out of Scope

- Allowing regular (non-administrator) users to send emails to the administration company
- Sending emails for issues in statuses other than PREPARED
- Multiple recipients, CC, or BCC functionality
- HTML-formatted email bodies
- User-configurable email templates
- Email delivery status tracking (open rates, bounce handling)
- Scheduled or bulk sending of multiple issues at once
- Resending an already-sent issue (intentional re-notification)
- Attachment of files or images in the email
