## ADDED Requirements

### Requirement: Status move endpoint
The system SHALL expose `POST /api/issues/{id}/status?target={STATUS}` on the view controller, accessible only to users with role ADMINISTRATOR. On success it SHALL return an HTMX fragment response containing the updated issue card (OOB swap) and optionally the updated detail modal (OOB swap).

#### Scenario: Valid status transition from IN_PROGRESS to ACKNOWLEDGED
- **WHEN** an ADMINISTRATOR posts to `/api/issues/{id}/status?target=ACKNOWLEDGED` for an issue in IN_PROGRESS
- **THEN** the issue status is updated to ACKNOWLEDGED, HTTP 200 is returned, and the response includes the updated `issue-card` fragment with `hx-swap-oob`

#### Scenario: Attempt to move a PREPARED issue via status endpoint
- **WHEN** any user posts to `/api/issues/{id}/status?target=IN_PROGRESS` for a PREPARED issue
- **THEN** the server returns HTTP 400 (bad request) because PREPARED issues MUST only advance via the Send Email flow

#### Scenario: Invalid transition rejected
- **WHEN** an ADMINISTRATOR posts to `/api/issues/{id}/status?target=RESOLVED` for an issue in PREPARED
- **THEN** the server returns HTTP 400 and the issue status is unchanged

#### Scenario: WON'T_DO allowed from any non-PREPARED status
- **WHEN** an ADMINISTRATOR posts `?target=WONT_DO` for an issue in any status
- **THEN** the issue status is updated to WONT_DO and the response includes the updated card fragment

#### Scenario: Non-admin user blocked
- **WHEN** a USER (non-admin) posts to the status endpoint
- **THEN** the server returns HTTP 403 Forbidden and no status change occurs

### Requirement: Status move buttons in issue card
The issue card fragment SHALL display status-move buttons in the admin actions section for all statuses except PREPARED. Only the legal next statuses for the current status SHALL be shown as buttons.

#### Scenario: IN_PROGRESS card shows ACKNOWLEDGED and WONT_DO buttons
- **WHEN** an ADMINISTRATOR views a board card for an IN_PROGRESS issue
- **THEN** the card admin actions section contains a button for ACKNOWLEDGED and a button for WONT_DO, and no other status-move buttons

#### Scenario: PREPARED card shows no status-move buttons
- **WHEN** an ADMINISTRATOR views a board card for a PREPARED issue
- **THEN** the card admin actions section contains only the Send Email button (no status-move buttons)

#### Scenario: Status-move buttons hidden for non-admin
- **WHEN** a USER views any issue card
- **THEN** no status-move buttons are visible

### Requirement: Status move buttons in detail modal
The issue detail modal SHALL display the same status-move buttons as the card, inside the Admin Actions section, for all statuses except PREPARED.

#### Scenario: ACKNOWLEDGED issue detail shows RESOLVED and WONT_DO buttons
- **WHEN** an ADMINISTRATOR opens the detail modal for an ACKNOWLEDGED issue
- **THEN** the Admin Actions section contains a button for RESOLVED and a button for WONT_DO

#### Scenario: RESOLVED issue detail shows PREPARED (reopen) and WONT_DO buttons
- **WHEN** an ADMINISTRATOR opens the detail modal for a RESOLVED issue
- **THEN** the Admin Actions section contains a button labelled to reopen (targeting PREPARED) and a WONT_DO button

### Requirement: Card entry animation
Every issue card SHALL animate into view when it is first inserted into the DOM using a CSS keyframe animation (slide-in from below with fade-in), lasting no more than 300ms.

#### Scenario: Card animates on column load
- **WHEN** a column loads its issues via HTMX
- **THEN** each issue card visually slides in from slightly below its final position while fading in

### Requirement: Card update flash animation
When an issue card is updated via HTMX OOB swap (e.g., after a status move or send), the card SHALL briefly flash a highlight colour (primary blue tint) to signal the change, using the `.htmx-settling` class hook.

#### Scenario: Card flashes after status move
- **WHEN** an issue card is replaced via OOB swap following a status move
- **THEN** the card briefly displays a blue highlight background that fades to normal within 500ms

### Requirement: Emoji column headers and status badges
Column headers on the board SHALL include an emoji that visually identifies the column. Status badges in issue cards and detail modals SHALL include a corresponding emoji prefix.

#### Scenario: Column headers display emoji
- **WHEN** the board page is loaded
- **THEN** each column header displays its emoji prefix (e.g., 📋 PREPARED, 🔄 IN PROGRESS, 👀 ACKNOWLEDGED, ✅ RESOLVED, 🚫 WON'T DO)

#### Scenario: Priority badges display emoji
- **WHEN** an issue card or detail modal is rendered
- **THEN** the priority badge displays an emoji prefix matching the priority level (e.g., 🔴 CRITICAL, 🟠 HIGH, 🟡 MEDIUM, 🟢 LOW)
