# Feature Specification: Issue Tracker for Property Management

**Feature Branch**: `001-issue-tracker`  
**Created**: 2026-02-07  
**Status**: Draft  
**Input**: User description: "I want to build a issue tracker like jira but in more usual way for people how leave on my area to create some issues with building, street etc. Each of the issue should conaint button send and clicking on that the issue description and title will be used to send email to administrative company to solve the issue. Also, the issue mustbe moved from CREATED/TODO column to SENT column. Backend must be build on java 25 and spring boot. Frontend must be lightweight so I can run it on my terminal machine(4Gb RAM and 8GB disk). I think htmx is a good choise but if you see anu other options just let me know"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create New Issue (Priority: P1)

As a logged-in resident, I want to create an issue about building or street problems so that the administrative company is aware of the problem.

**Why this priority**: Core functionality - without the ability to create issues, the system has no value. This is the primary entry point for all users.

**Independent Test**: Can be fully tested by logging in, creating an issue with title and description, and verifying it appears in the TODO column with status "PREPARED".

**Acceptance Scenarios**:

1. **Given** a logged-in user on the issue board, **When** they click "New Issue" and fill in title "Broken street light" and description "The light on Main Street is not working for 3 days", **Then** the issue appears in the PREPARED column.
2. **Given** a user not logged in, **When** they try to access the issue creation page, **Then** they are redirected to the login page.
3. **Given** a logged-in user creating an issue, **When** they try to submit without a title, **Then** they see an error message indicating title is required.

---

### User Story 2 - Send Issue to Administrative Company (Priority: P1)

As an administrator, I want to send an issue to the administrative company via email so they receive the full issue details and begin working on it.

**Why this priority**: Critical workflow step - this is the primary mechanism for communicating issues to the administrative company and triggers status progression.

**Independent Test**: Can be fully tested by logging in as administrator, clicking the "Send" button on a PREPARED issue, verifying an email is sent to the administrative company, and confirming the issue moves to the IN_PROGRESS column.

**Acceptance Scenarios**:

1. **Given** an issue exists with status PREPARED in the PREPARED column, **When** an administrator clicks the "Send" button, **Then** an email is sent to the administrative company containing the issue title and description, AND the issue status changes to IN_PROGRESS.
2. **Given** a logged-in regular user (non-administrator), **When** they view a PREPARED issue, **Then** they do not see the "Send" button.
3. **Given** an issue already has status IN_PROGRESS (was previously sent), **When** an administrator clicks "Send" again, **Then** the system prevents duplicate email sending and shows a message indicating the issue was already sent.

---

### User Story 3 - View Issues in Board Layout (Priority: P2)

As a logged-in user, I want to see all issues organized in columns by status so I can quickly understand what issues exist and their current state.

**Why this priority**: Essential for visibility and transparency - users need to see existing issues to avoid duplicates and track progress.

**Independent Test**: Can be fully tested by logging in, viewing the board, and verifying issues are displayed in appropriate columns based on their status.

**Acceptance Scenarios**:

1. **Given** a logged-in user and multiple issues exist with different statuses, **When** they view the board, **Then** they see columns for PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, and WON'T_DO with issues displayed in their respective columns.
2. **Given** a logged-in user viewing the board, **When** a new issue is created, **Then** it automatically appears in the PREPARED column without requiring a page refresh.
3. **Given** a user not logged in, **When** they try to access the board view, **Then** they are redirected to the login page.

---

### User Story 4 - Update Issue Status (Priority: P2)

As an administrator, I want to change the status of an issue as it progresses through the workflow so residents can track the resolution progress.

**Why this priority**: Important for communication and workflow management - keeps residents informed about issue resolution status.

**Independent Test**: Can be fully tested by logging in as administrator, changing an issue's status, and verifying it moves to the appropriate column on the board.

**Acceptance Scenarios**:

1. **Given** an issue with status IN_PROGRESS, **When** an administrator changes the status to ACKNOWLEDGED, **Then** the issue moves to the ACKNOWLEDGED column.
2. **Given** a logged-in regular user (non-administrator), **When** they view an issue, **Then** they cannot change the status.
3. **Given** an issue with any status, **When** an administrator marks it as WON'T_DO, **Then** the issue moves to the WON'T_DO column with a clear indication it will not be addressed.

---

### User Story 5 - Search and Filter Issues (Priority: P3)

As a logged-in user, I want to search for specific issues by keywords or filter by status so I can quickly find relevant issues.

**Why this priority**: Nice-to-have feature for usability, especially as the number of issues grows over time.

**Independent Test**: Can be fully tested by logging in, entering search terms, and verifying only matching issues are displayed.

**Acceptance Scenarios**:

1. **Given** multiple issues exist, **When** a logged-in user searches for "street", **Then** only issues containing "street" in the title or description are displayed.
2. **Given** issues with different statuses, **When** a logged-in user filters by "RESOLVED", **Then** only resolved issues are shown.

---

### User Story 6 - User Authentication (Priority: P1)

As a user, I want to create an account and log in so I can access the issue tracker and create issues securely.

**Why this priority**: Authentication is a prerequisite for all other functionality. All operations require authenticated users.

**Independent Test**: Can be fully tested by registering a new user account, logging in, and accessing the issue board.

**Acceptance Scenarios**:

1. **Given** a new user on the login page, **When** they click "Register" and provide username and password, **Then** a new account is created and they can log in.
2. **Given** an unauthenticated user, **When** they try to access any page except login or registration, **Then** they are redirected to the login page.
3. **Given** a logged-in user, **When** they are inactive for 30 minutes, **Then** their session expires and they must log in again.

---

### Edge Cases

- What happens when the email fails to send? The issue should remain in PREPARED status and display an error message.
- How does the system handle very long issue descriptions (1000+ characters) in the email? The email should include the full description regardless of length.
- What happens if two administrators try to send the same issue simultaneously? The system should prevent duplicate emails through atomic status checking.
- How are issues handled when the administrative company email is not configured? The system should display a configuration error and prevent sending.
- What happens when a user tries to access the system from a mobile device with limited bandwidth? The lightweight frontend should load within 3 seconds on slow connections.
- What happens when a user tries to register with an existing username? The system should display an error indicating the username is taken.
- What happens when a user enters wrong credentials multiple times? The system should implement account lockout after 5 failed attempts for 15 minutes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST require user authentication for all operations except login and registration pages.
- **FR-002**: System MUST support user registration with username (unique, 3-50 characters) and password (minimum 8 characters).
- **FR-003**: System MUST distinguish between regular USER role and ADMINISTRATOR role.
- **FR-004**: System MUST expire user sessions after 30 minutes of inactivity.
- **FR-005**: System MUST allow users to create issues with a title (required, max 200 characters) and description (required, max 2000 characters).
- **FR-006**: System MUST assign the status "PREPARED" to all newly created issues.
- **FR-007**: System MUST display issues in a board view with columns representing different statuses.
- **FR-008**: System MUST provide a "Send" button visible only to ADMINISTRATOR role on PREPARED issues.
- **FR-009**: When "Send" is clicked, System MUST send an email to the configured administrative company email address containing the issue title, description, and unique identifier.
- **FR-010**: After successful email sending, System MUST change the issue status from "PREPARED" to "IN_PROGRESS".
- **FR-011**: System MUST prevent duplicate email sending by checking the current status before sending.
- **FR-012**: System MUST support the following status values: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO.
- **FR-013**: System MUST allow only ADMINISTRATOR role to manually change issue status between any valid states.
- **FR-014**: System MUST validate that title and description are non-empty before creating an issue.
- **FR-015**: System MUST persist all issue data including creation timestamp and last updated timestamp.
- **FR-016**: System MUST provide visual feedback when email is successfully sent or fails to send.
- **FR-017**: System MUST support searching issues by keywords in title or description.
- **FR-018**: System MUST support filtering issues by status.
- **FR-019**: System MUST lock user accounts for 15 minutes after 5 consecutive failed login attempts.
- **FR-020**: System MUST prevent registration with duplicate usernames.

### Key Entities *(include if feature involves data)*

- **User**: Represents a registered user of the system. Contains username (unique), password (hashed), role (USER or ADMINISTRATOR), creation timestamp, and last login timestamp. Users with ADMINISTRATOR role have elevated permissions including sending issues and changing status.
- **Issue**: Represents a problem report submitted by a resident. Contains title, description, status (enum), creator (reference to User), creation timestamp, last updated timestamp, and sent flag. Status transitions follow the workflow: PREPARED → IN_PROGRESS → ACKNOWLEDGED → RESOLVED, with WON'T_DO available from any status.
- **Status**: Enumeration representing the lifecycle stage of an issue. Values: PREPARED (initial), IN_PROGRESS (sent to admin company), ACKNOWLEDGED (company acknowledged receipt), RESOLVED (issue completed), WON'T_DO (issue rejected).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a new account and log in within 2 minutes.
- **SC-002**: Users can create a new issue and have it appear on the board in under 10 seconds.
- **SC-003**: The "Send" action successfully delivers email to the administrative company within 30 seconds in 95% of attempts.
- **SC-004**: Issues automatically move to the correct column on the board within 2 seconds of status change without page refresh.
- **SC-005**: The application loads the board view in under 3 seconds on a machine with 4GB RAM and standard internet connection.
- **SC-006**: 100% of attempts to send an already-sent issue are prevented (no duplicate emails).
- **SC-007**: Users can search and filter issues with results displaying in under 1 second for up to 1000 issues.
- **SC-008**: The application frontend bundle size is under 500KB (excluding images) to support low-resource environments.
- **SC-009**: Session timeout occurs exactly at 30 minutes of inactivity (±1 minute tolerance).
- **SC-010**: Account lockout activates after exactly 5 failed login attempts and prevents login for 15 minutes.

## Assumptions

1. **Administrative Company Email**: The email address for the administrative company (Zarządca osiedla) is configured via environment variables and remains constant.
2. **Single Administrative Company**: The system supports sending issues to one administrative company email address. Multiple recipients or company selection is out of scope.
3. **No Attachments**: Issues consist of text only (title and description). File attachments are not supported in this version.
4. **Simple User Management**: Users can self-register with username and password. No email verification or password reset via email in initial version.
5. **Email Configuration**: SMTP settings are configured via environment variables and are assumed to be valid. Email deliverability is handled by the external SMTP provider.
6. **Single Deployment**: The application runs on a single server instance (no clustering or horizontal scaling required).
7. **Moderate Issue Volume**: The system is designed to handle up to 1000 active issues efficiently. Archiving old issues is out of scope for initial version.
8. **Administrator Role Assignment**: Initial administrator accounts are created via database seeding or manual database insertion. No admin promotion interface in initial version.

## Out of Scope

- Multi-tenancy support (multiple residential areas/companies)
- Email templates customization
- Real-time notifications (WebSockets)
- Mobile native applications
- Advanced analytics and reporting dashboards
- Issue assignment to specific users
- Comments and discussion threads on issues
- File attachments and images
- Email threading and reply tracking
- Integration with external ticketing systems
- Automated escalation rules
- Email verification during registration
- Password reset via email
- User profile management (change password, update details)
- API for third-party integrations
- Email encryption (TLS is assumed to be handled by SMTP provider)
