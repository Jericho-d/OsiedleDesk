# Feature Specification: Material Design UI Redesign

**Feature Branch**: `005-material-redesign`  
**Created**: 2026-02-22  
**Status**: Draft  
**Input**: User description: "I would like to change the design for this tool. Right now it looks ugly. It should be
more beautiful, modern and more experienced. Use material design as an entry point."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Board View Redesign (Priority: P1)

An administrator or user opens the application and is greeted by a polished, modern board view. Issue columns are
clearly distinguished, cards look clean and readable, and the overall layout feels professional rather than raw HTML.

**Why this priority**: The board is the primary screen users spend all their time on. Improving it first delivers the
most immediate visual impact and covers the core of the application.

**Independent Test**: Can be fully tested by logging in and viewing the board — all five columns and their cards must
render with the new Material Design visual style and deliver a noticeably more polished experience than the current
version.

**Acceptance Scenarios**:

1. **Given** a logged-in user on the board, **When** they view the page, **Then** they see a Material Design-inspired
   layout with a top app bar, clean typography, card-based issue tiles with subtle elevation/shadow, and a colour scheme
   that is consistent and intentional.
2. **Given** a board with issues in multiple columns, **When** the user scans the board, **Then** column headers are
   clearly readable, priority badges are colour-coded and visually distinct, and the SENT badge is clearly
   differentiated from priority badges.
3. **Given** a board on a typical desktop screen (1280 px wide), **When** the user views all five columns, **Then** all
   columns are visible without horizontal scrolling and the layout does not feel cramped.

---

### User Story 2 - Issue Detail Modal Redesign (Priority: P2)

An administrator clicks an issue card and the detail panel opens. The redesigned modal feels like a polished dialog —
well-spaced, readable, with clear section hierarchy.

**Why this priority**: The detail modal is the second most-used surface; it's where administrators read full
descriptions and trigger the send action.

**Independent Test**: Can be fully tested by clicking any issue card and verifying the detail overlay renders with the
new design.

**Acceptance Scenarios**:

1. **Given** a user clicks an issue card, **When** the detail modal opens, **Then** it has a clear header with title and
   close button, metadata rows are easy to scan, and the Send button is visually prominent.
2. **Given** an issue that has already been sent, **When** the detail modal opens, **Then** the SENT indicator is
   clearly visible and the Send button is absent.

---

### User Story 3 - Login Page Redesign (Priority: P3)

A user navigates to the login page. The redesigned page looks intentional and branded rather than a bare browser-default
form.

**Why this priority**: The login page is seen first but users spend minimal time there; visual improvement still matters
for first impressions.

**Independent Test**: Can be fully tested by visiting `/login` and verifying the page renders with the new design
without breaking authentication.

**Acceptance Scenarios**:

1. **Given** an unauthenticated user on `/login`, **When** they view the page, **Then** they see a centred login card
   with clearly labelled fields and a prominent sign-in button, consistent with the rest of the application's visual
   style.
2. **Given** a user submits incorrect credentials, **When** the error is shown, **Then** the error message is styled
   consistently with the new design.

---

### User Story 4 - Issue Create / Edit Form Redesign (Priority: P4)

An administrator creates or edits an issue. The form fields, labels, and buttons follow the same Material Design
vocabulary as the rest of the application.

**Why this priority**: The form is used less frequently than the board but must be visually consistent; an inconsistent
form would feel jarring.

**Independent Test**: Can be fully tested by navigating to the issue creation form and verifying all inputs, labels, and
buttons match the new design system.

**Acceptance Scenarios**:

1. **Given** an administrator on the issue form, **When** they view it, **Then** inputs have clear labels, consistent
   spacing, and the submit button matches the application's primary action style.

---

### Edge Cases

- What happens on very small screens (< 768 px)? Columns should stack or scroll horizontally without breaking the
  layout.
- What happens when an issue title is very long? The card title should truncate or wrap without overflowing its
  container.
- What happens when a column has no issues? The empty state should be styled attractively, not as bare unstyled text.
- What happens when the error toast fires? It must remain visually consistent with the new design (correct colours,
  typography, top positioning).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The board page MUST include a persistent top application bar containing the application name and a visible
  logout control.
- **FR-002**: Issue cards MUST display title, description preview, priority badge, and issue ID with consistent
  typography and spacing derived from Material Design principles.
- **FR-003**: Issue cards MUST use elevation (box shadow) to visually separate them from the column background.
- **FR-004**: Column headers MUST be visually prominent and clearly separated from card content.
- **FR-005**: Priority badges MUST use a distinct colour per priority level (LOW, MEDIUM, HIGH, CRITICAL) that is
  consistent across cards and the detail modal.
- **FR-006**: The Send button MUST be visually distinguishable as a primary action (e.g., filled button style).
- **FR-007**: The SENT badge MUST be visually distinct from priority badges and action buttons.
- **FR-008**: The issue detail modal MUST have clear visual hierarchy: title at top, metadata in a structured list,
  admin actions at the bottom.
- **FR-009**: The login page MUST present a centred card layout with clearly labelled fields and a primary action
  button.
- **FR-010**: The issue form MUST use consistent input styling aligned with the overall design system.
- **FR-011**: The error toast notification MUST be visually consistent with the new design (positioned at top, correct
  colour palette, auto-dismiss, no close button).
- **FR-012**: The redesign MUST NOT break existing dynamic behaviours — all HTMX interactions (card swap on send, modal
  open, toast injection) must continue to work correctly after the redesign.
- **FR-013**: All colours, spacing, and typography MUST be defined in one centralised location (CSS custom properties)
  to enable consistent theming.
- **FR-014**: The empty-column state MUST be styled (e.g., a subtle placeholder message) rather than bare unstyled text.

### Key Entities

- **Top App Bar**: Fixed header present on all authenticated pages; contains app name and logout action.
- **Board Column**: Named vertical lane for each status; has a header and a scrollable card list.
- **Issue Card**: Compact tile showing title, description excerpt, priority badge, and issue ID; clickable to open the
  detail modal.
- **Issue Detail Modal**: Overlay showing all issue fields and admin actions.
- **Priority Badge**: Colour-coded label indicating LOW / MEDIUM / HIGH / CRITICAL.
- **Send Button**: Primary action button visible to administrators on PREPARED issues not yet sent.
- **Error Toast**: Top-centre fixed notification injected on email send failure.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All five board columns and their cards render with the new design without layout breakage on a standard
  1280 × 800 desktop viewport.
- **SC-002**: The login, board, detail modal, and issue form pages are visually consistent — a reviewer can identify
  they belong to the same design system without being told.
- **SC-003**: All existing functional behaviours (login, create issue, send email, view detail, error toast) continue to
  work correctly after the redesign.
- **SC-004**: No horizontal scrollbar appears on the board at viewport widths of 1024 px and above.
- **SC-005**: Each priority level is represented by a unique, distinguishable colour that passes a basic visual contrast
  check against its background.
- **SC-006**: A first-time user can identify the primary action (Send) on an issue card without any instructions.

## Assumptions

- The existing Thymeleaf + HTMX + Spring Boot stack is unchanged; only HTML and CSS are modified.
- No new JavaScript framework is introduced; vanilla CSS and minimal vanilla JS are acceptable.
- Material Design is used as a visual reference (elevation, typography scale, colour system, component vocabulary) — not
  as a strict component library import.
- Google Fonts (e.g., Roboto) may be linked from a CDN for typography.
- Default colour palette: primary blue `#1976D2`, surface white `#FFFFFF`, background light grey `#F5F5F5`, error red
  `#D32F2F` — adjustable if the user provides brand colours.
- Reasonable contrast ratios will be applied as a default good practice; no specific WCAG level is mandated.
- The redesign covers six template files: `board/index.html`, `auth/login.html`, `issues/form.html`,
  `fragments/issue-card.html`, `fragments/issue-detail.html`, and `fragments/send-error.html`.
