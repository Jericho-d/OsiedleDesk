# Quickstart: Material Design UI Redesign

**Feature**: `005-material-redesign` | **Date**: 2026-02-22

---

## Prerequisites

- Application running locally: `./gradlew bootRun`
- Default credentials: `admin` / `admin`
- Base URL: `http://localhost:8080`

---

## Manual Test Scenarios

### Scenario 1 — Login Page (US3)

1. Open `http://localhost:8080/login` (unauthenticated)
2. **Expected**: Centred white card on grey background; "Issue Tracker" title in primary blue; labelled username and
   password fields; prominent blue "Log In" button; no gradient background from the old design.
3. Enter wrong credentials, submit.
4. **Expected**: Red error alert styled consistently (not a raw browser alert or unstyled text).
5. Enter `admin` / `admin`, submit.
6. **Expected**: Redirected to board.

---

### Scenario 2 — Board View (US1)

1. Log in and navigate to `http://localhost:8080/board`.
2. **Expected**:
    - Full-width sticky blue app bar at top with "Issue Tracker" title, "+ New Issue" button, and "Logout" button.
    - Board body has light grey page background.
    - Five columns visible side by side (no horizontal scrollbar at 1280 px wide).
    - Column headers have a light blue tint background and readable label text.
    - Each column lane has a slightly different background from the cards inside it.
3. Inspect issue cards.
4. **Expected**:
    - White card surface with subtle shadow (elevation).
    - Card lifts visibly on hover (shadow increases, slight Y translate).
    - Title readable; description preview truncated gracefully.
    - Priority badge colour: GREEN for LOW, AMBER for MEDIUM, ORANGE for HIGH, RED for CRITICAL.
    - Issue ID shown as secondary text.
5. Find a PREPARED issue not yet sent (admin controls visible).
6. **Expected**: Green "Send" button visible and styled as a filled Material button.
7. Find a sent issue.
8. **Expected**: Blue "SENT" badge visible; Send button absent.
9. Find a column with no issues.
10. **Expected**: Styled empty-state message (not bare unstyled text).

---

### Scenario 3 — Issue Detail Modal (US2)

1. Click any issue card on the board.
2. **Expected**:
    - Dark backdrop overlay appears.
    - White dialog panel slides/appears in centre with rounded corners and deep shadow.
    - Clear header: issue title + ✕ close button.
    - Description section with body text.
    - Metadata grid: Status badge, Priority badge, Assignee, Created, Updated — all well-spaced and labelled.
    - Admin Actions section visible to admin with "Send to Administration" button (green filled).
3. Press Escape or click the backdrop.
4. **Expected**: Modal closes, board visible and unchanged.
5. Click a sent issue.
6. **Expected**: Modal shows blue SENT badge; no Send button.

---

### Scenario 4 — Error Toast (US1/US2 edge case)

> Requires SMTP misconfigured so send fails. Set `SPRING_MAIL_HOST=invalid` or simulate via breakpoint.

1. Click "Send" on a PREPARED issue.
2. **Expected**: Toast notification appears **at the top** of the screen (not bottom); red left border; "The pigeon
   didn't make it." headline; dismisses automatically after 5 seconds; no close button; card remains in PREPARED column.

---

### Scenario 5 — Issue Create Form (US4)

1. Click "+ New Issue" in the app bar.
2. **Expected**:
    - Page with consistent app bar (or back link to board).
    - Form card on grey background.
    - Inputs have clear outlined style with Material focus ring (blue border on focus).
    - "Create Issue" button styled as filled primary button.
    - "Cancel" styled as secondary/outlined button.
3. Submit empty form.
4. **Expected**: Validation errors styled in red below each invalid field (not browser-native popups).

---

## Visual Consistency Checklist

After implementing all user stories, verify:

- [ ] Font is Roboto (or system fallback) on all pages
- [ ] Primary blue (`#1976D2`) appears on app bar, primary buttons, and focused inputs
- [ ] No purple/indigo `#667eea` remains anywhere (old brand colour)
- [ ] Page backgrounds are `#F5F5F5` on all authenticated pages
- [ ] All cards have consistent border-radius and shadow
- [ ] Priority badge colours match the defined palette (green/amber/orange/red family)
- [ ] No raw inline `style=` attributes left for colours that should use CSS variables
- [ ] All HTMX interactions still work: card swap, modal open/close, toast injection
