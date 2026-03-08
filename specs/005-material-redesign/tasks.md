# Tasks: Material Design UI Redesign

**Input**: Design documents from `specs/005-material-redesign/`  
**Branch**: `005-material-redesign`  
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/css-classes.md ✅ quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1–US4)
- All file paths are relative to repository root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the single shared stylesheet with all design tokens. No template changes yet — build passes and no
visual change occurs until templates link the file.

- [x] T001 Create `src/main/resources/static/css/material.css` with full `:root` design token block: all colour tokens (
  `--md-primary` through `--md-on-sent`), all priority badge tokens, typography tokens, spacing tokens, elevation
  tokens, and shape tokens as defined in `specs/005-material-redesign/data-model.md`
- [x] T002 Add base reset and shared utility classes to `src/main/resources/static/css/material.css`: CSS reset (
  `*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }`), body base (
  `font-family: var(--md-font-family); background: var(--md-surface-variant); min-height: 100vh;`), and Google Fonts
  `@import` for Roboto weights 400/500/700
- [x] T003 Add all shared component classes to `src/main/resources/static/css/material.css`: `.md-btn-primary`,
  `.md-btn-secondary`, `.md-input`, `.md-form-group`, `.md-alert`, `.md-alert--error`, `.md-alert--success`, `.md-hint`,
  `.md-error-message` as specified in `specs/005-material-redesign/contracts/css-classes.md`

**Checkpoint**: `./gradlew build` passes. File exists at `src/main/resources/static/css/material.css`. No templates
changed yet.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Link the shared stylesheet in every template. No visual change yet (tokens not applied until classes are
used), but the link must exist before any story work begins.

- [x] T004 Add `<link rel="stylesheet" th:href="@{/css/material.css}">` to `<head>` in
  `src/main/resources/templates/board/index.html` (after the existing `<meta>` tags, before `<style>`)
- [x] T005 [P] Add `<link rel="stylesheet" th:href="@{/css/material.css}">` to `<head>` in
  `src/main/resources/templates/auth/login.html`
- [x] T006 [P] Add `<link rel="stylesheet" th:href="@{/css/material.css}">` to `<head>` in
  `src/main/resources/templates/issues/form.html`

**Checkpoint**: `./gradlew build` passes. All three authenticated templates now reference `/css/material.css`.

---

## Phase 3: User Story 1 — Board View Redesign (Priority: P1) 🎯 MVP

**Goal**: The board page (`/board`) renders with a Material Design-inspired sticky app bar, column layout with tinted
headers, elevated issue cards, colour-coded priority badges, and a styled empty-column state. The old inline `<style>`
block in `board/index.html` is removed in favour of `material.css` classes.

**Independent Test**: Log in, navigate to `/board`. Verify: sticky blue app bar, five columns with light-blue header
tints, white elevated cards that lift on hover, priority badges in green/amber/orange/red palette, SENT badge in blue,
green Send button, styled empty-state message. All HTMX interactions (card load, modal open, column poll) still work.

### Implementation for User Story 1

- [x] T007 [US1] Add app bar component classes to `src/main/resources/static/css/material.css`: `.md-app-bar` (
  full-width sticky, 64px, `--md-primary` bg), `.md-app-bar__title` (white headline text), `.md-app-bar__actions` (
  right-aligned flex row), and `body { padding-top: 64px; }` to clear the sticky bar
- [x] T008 [US1] Add board layout classes to `src/main/resources/static/css/material.css`: `.md-board` (CSS grid,
  `repeat(5, minmax(200px, 1fr))`, 16px gap), `.md-column` (`--md-surface-variant` bg, `--md-radius-md`, flex column,
  min-height 400px), `.md-column__header` (`--md-primary-container` bg, padding 12px 16px, `--md-type-headline` font,
  `--md-on-surface` colour, letter-spacing), `.md-column__list` (flex-1, overflow-y auto, padding 8px, flex column gap
  8px), `.md-column__empty` (centred, `--md-on-surface-variant`, font-style italic, padding 2rem)
- [x] T009 [US1] Add issue card classes to `src/main/resources/static/css/material.css`: `.issue-card` (white,
  `--md-elevation-1`, `--md-radius-md`, padding 12px, cursor pointer, transition), `.issue-card:hover` (
  `--md-elevation-2`, `translateY(-2px)`), `.issue-card__title` (font-weight 500, `--md-on-surface`, line-height 1.3,
  overflow hidden, max 2 lines via `-webkit-line-clamp`), `.issue-card__description` (caption size,
  `--md-on-surface-variant`, max 2 lines clamp, margin-bottom 8px), `.issue-card__footer` (flex, space-between,
  align-center), `.issue-card__id` (`--md-on-surface-variant`, caption), `.issue-card__actions` (flex, gap 8px,
  margin-top 12px, padding-top 12px, border-top `1px solid var(--md-outline)`)
- [x] T010 [US1] Add priority badge and sent badge classes to `src/main/resources/static/css/material.css`:
  `.priority` (base — label font, `--md-radius-sm`, padding 2px 8px, font-weight 600, text-transform uppercase,
  font-size 0.7rem), `.priority-LOW` through `.priority-CRITICAL` using their token pairs from `data-model.md`,
  `.sent-badge` (`--md-sent` bg, `--md-on-sent` text, same base shape as `.priority`)
- [x] T011 [US1] Add send button classes to `src/main/resources/static/css/material.css`: `.btn-send` (filled —
  `--md-success` bg, `--md-on-success` text, `--md-radius-md`, padding 4px 12px, font-weight 500, font-size 0.8rem,
  border none, cursor pointer, transition), `.btn-send:hover` (darken `--md-success` to `#1b5e20`, `--md-elevation-1`)
- [x] T012 [US1] Rewrite `<body>` content of `src/main/resources/templates/board/index.html`: replace `.header` div with
  `<header class="md-app-bar">` containing title span and actions div; replace `.board` div with `class="md-board"`;
  replace each `.column` div with `class="md-column"`, `.column-header` with `class="md-column__header"`, `.issue-list`
  with `class="md-column__list"` — preserve all `id`, `hx-get`, `hx-trigger` attributes unchanged
- [x] T013 [US1] Remove the entire inline `<style>` block from `src/main/resources/templates/board/index.html` (lines
  containing `.header`, `.board`, `.column`, `.issue-card`, `.priority`, `.logout`, `.htmx-indicator`, `@media` rules
  for board — **keep** the modal and toast CSS for now as it is used by fragments loaded via HTMX)
- [x] T014 [US1] Update `src/main/resources/templates/fragments/issue-card.html`: add `class="issue-card__title"` to
  title div, `class="issue-card__description"` to description div, wrap meta span in `class="issue-card__footer"`, add
  `class="issue-card__id"` to the `#N` span, replace `class="admin-controls"` with `class="issue-card__actions"` —
  preserve all `th:*` and `hx-*` attributes; `.priority`, `.priority-*`, `.sent-badge`, and `.btn-send` class names are
  unchanged (already in stylesheet)
- [x] T015 [US1] Update `src/main/resources/templates/fragments/column.html`: change `class="no-issues"` to
  `class="md-column__empty"` on the empty-state div

**Checkpoint**: Board view at `/board` renders with full Material Design styling. All five columns and cards visible at
1280 px. No horizontal scrollbar. HTMX column polling and card clicks still work. Priority badges are colour-coded.

---

## Phase 4: User Story 2 — Issue Detail Modal Redesign (Priority: P2)

**Goal**: The issue detail modal renders with a polished Material Design dialog: clear header hierarchy, readable
metadata grid, prominent Send button at the bottom. Modal CSS is moved from `board/index.html` inline styles into
`material.css`.

**Independent Test**: Click any issue card. Verify: deep-shadow dialog appears centred, clear title + close button
header, metadata in two-column grid with uppercase labels, Send button green and prominent, SENT badge visible on sent
issues. Escape/backdrop-click closes modal.

### Implementation for User Story 2

- [x] T016 [US2] Add modal and detail panel classes to `src/main/resources/static/css/material.css`:
  `.issue-modal-backdrop` (fixed inset 0, `rgba(0,0,0,0.5)`, z-index 1000, flex centred), `.issue-detail-panel` (white,
  `--md-elevation-3`, `--md-radius-lg`, max-width 640px, max-height 90vh, overflow-y auto, flex column),
  `.detail-header` (flex space-between, padding `var(--md-space-md)`, border-bottom `1px solid var(--md-outline)`),
  `.detail-title` (`--md-type-headline`, `--md-on-surface`), `.detail-close-btn` (icon button — bg none, border none,
  `--md-on-surface-variant`, cursor pointer, hover `--md-on-surface`), `.detail-section` (padding `var(--md-space-md)`,
  border-bottom `1px solid var(--md-outline)`), `.detail-label` (caption, `--md-on-surface-variant`, uppercase,
  letter-spacing 0.05em, margin-bottom 4px), `.detail-description` (body, `--md-on-surface`, line-height 1.6, pre-wrap,
  word-break break-word), `.detail-meta` (padding `var(--md-space-md)`, border-bottom, grid 2 cols, gap 12px),
  `.detail-meta-row` (flex column, gap 4px), `.detail-status-badge` (inline-block, `--md-surface-variant`,
  `--md-on-surface`, `--md-radius-sm`, padding 2px 8px, label font), `.detail-actions` (padding `var(--md-space-md)`,
  flex wrap, gap 12px), `.detail-actions-label` (caption, `--md-on-surface-variant`, uppercase, letter-spacing, width
  100%), `.detail-action-btn` (alias — same styles as `.btn-send`)
- [x] T017 [US2] Add mobile responsive rules for modal to `src/main/resources/static/css/material.css`:
  `@media (max-width: 600px)` — `.issue-detail-panel` (max-width 100%, border-radius top-only),
  `.issue-modal-backdrop` (align-items flex-end, padding 0), `.detail-meta` (grid-template-columns 1fr)
- [x] T018 [US2] Remove all modal-related CSS from the inline `<style>` block in
  `src/main/resources/templates/board/index.html` (classes `.issue-modal-backdrop`, `.issue-detail-panel`,
  `.detail-*`) — the `<style>` block should now be empty or removed entirely; verify `link` to `material.css` is present
- [x] T019 [US2] Verify `src/main/resources/templates/fragments/issue-detail.html` class names match the stylesheet:
  `.issue-modal-backdrop`, `.issue-detail-panel`, `.detail-header`, `.detail-title`, `.detail-close-btn`,
  `.detail-section`, `.detail-label`, `.detail-description`, `.detail-meta`, `.detail-meta-row`, `.detail-status-badge`,
  `.detail-actions`, `.detail-actions-label`, `.detail-action-btn` — update any mismatches; preserve all `th:*`, `hx-*`,
  `sec:*`, and `id` attributes unchanged

**Checkpoint**: Clicking a card opens a polished Material Design modal. All metadata visible and well-spaced. Send
button prominent. Escape and backdrop-click close modal. Mobile viewport (< 600 px) shows sheet from bottom.

---

## Phase 5: User Story 3 — Login Page Redesign (Priority: P3)

**Goal**: The login page (`/login`) renders a centred white card on a grey background with clearly labelled inputs,
primary blue button, and consistently styled error/success alerts. The old purple gradient background is replaced.

**Independent Test**: Visit `/login`. Verify: grey page background (no purple gradient), centred white card with shadow,
blue "Log In" button, error alert styled in red on bad credentials, success alert styled in green after logout.

### Implementation for User Story 3

- [x] T020 [US3] Add login page layout classes to `src/main/resources/static/css/material.css`: `.md-login-page` (
  full-height flex centred, `--md-surface-variant` bg, padding `var(--md-space-md)`), `.md-login-card` (white,
  `--md-elevation-2`, `--md-radius-lg`, max-width 400px, width 100%, padding `var(--md-space-xl)`),
  `.md-login-card__title` (display font, `--md-primary`, text-align centre, margin-bottom `var(--md-space-lg)`)
- [x] T021 [US3] Add input and form classes to `src/main/resources/static/css/material.css` (if not already covered by
  Phase 1 T003): `.md-input` (width 100%, padding 12px, border `1px solid var(--md-outline)`, `--md-radius-md`,
  font-size 1rem, font-family inherit, outline none, transition border-color), `.md-input:focus` (border-color
  `var(--md-primary)`, box-shadow `0 0 0 2px rgba(25,118,210,0.15)`), `.md-input--error` (border-color
  `var(--md-error)`), `.md-form-group` (margin-bottom `var(--md-space-md)`), `.md-form-group label` (display block,
  `--md-on-surface-variant`, font-weight 500, margin-bottom `var(--md-space-xs)`, font-size 0.875rem)
- [x] T022 [US3] Rewrite `src/main/resources/templates/auth/login.html`: remove entire inline `<style>` block; add
  `class="md-login-page"` to `<body>`; replace `.login-container` with `class="md-login-card"`; replace `<h1>` class
  with `class="md-login-card__title"`; replace each `.form-group` with `class="md-form-group"`; replace `input` elements
  with `class="md-input"`; replace `<button>` with `class="md-btn-primary"` (full width); replace `.alert.alert-error`
  with `class="md-alert md-alert--error"`; replace `.alert.alert-success` with `class="md-alert md-alert--success"`;
  remove `.register-link` block if present (register link is not in scope); preserve `th:if`, `th:action`, `th:href`
  attributes unchanged

**Checkpoint**: `/login` renders with grey background, centred white card, blue button. Error alert is red. Success
alert is green. Authentication still works.

---

## Phase 6: User Story 4 — Issue Create/Edit Form Redesign (Priority: P4)

**Goal**: The issue creation form (`/issues/new`) renders consistently with the board — same app bar, grey background,
card-contained form, outlined inputs with focus ring, primary button, secondary cancel button.

**Independent Test**: Navigate to `/issues/new`. Verify: app bar present (matching board), form centred in white card on
grey background, inputs with outlined style and blue focus ring, green "Create Issue" primary button, grey "Cancel"
secondary button. Validation errors styled in red below inputs.

### Implementation for User Story 4

- [x] T023 [US4] Add form page layout classes to `src/main/resources/static/css/material.css` (if not already from
  T020): `.md-form-page` (padding `var(--md-space-xl) var(--md-space-md)`, min-height `calc(100vh - 64px)`),
  `.md-form-card` (background white, `--md-elevation-1`, `--md-radius-lg`, max-width 800px, margin `0 auto`, padding
  `var(--md-space-xl)`)
- [x] T024 [US4] Add secondary button and error/hint classes to `src/main/resources/static/css/material.css` (if not
  already from T003): `.md-btn-secondary` (background transparent, border `1px solid var(--md-outline)`,
  `--md-on-surface`, `--md-radius-md`, padding 10px 24px, font-weight 500, cursor pointer, text-decoration none, display
  inline-block, transition), `.md-btn-secondary:hover` (`--md-surface-variant` bg), `.md-error-message` (`--md-error`
  colour, font-size 0.8rem, margin-top `var(--md-space-xs)`), `.md-hint` (`--md-on-surface-variant`, font-size 0.8rem,
  margin-top `var(--md-space-xs)`)
- [x] T025 [US4] Rewrite `src/main/resources/templates/issues/form.html`: remove entire inline `<style>` block; add
  `class="md-form-page"` to `<body>`; replace outer `.container` with `class="md-form-card"`; replace each `.form-group`
  with `class="md-form-group"`; replace `input[type="text"]`, `textarea`, and `select` with `class="md-input"` (keep
  `th:class` for error state using `md-input--error`); replace `class="error-message"` with `class="md-error-message"`;
  replace `class="requirements"` with `class="md-hint"`; replace `class="btn-primary"` with `class="md-btn-primary"`;
  replace `class="btn-secondary"` with `class="md-btn-secondary"`; add `<header class="md-app-bar">` at top with back
  link instead of `.header` back-link div; preserve all `th:*`, `hx-*` attributes and HTMX redirect logic unchanged

**Checkpoint**: `/issues/new` renders with app bar, white card form, outlined inputs, blue primary button, secondary
cancel button. Submitting empty form shows red validation errors. Submitting valid form redirects to `/board`.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Update error toast to use stylesheet classes, final consistency pass, remove any remaining old brand colour
references.

- [x] T026 Add toast classes to `src/main/resources/static/css/material.css`: `.md-toast-container` (position fixed, top
  1.25rem, left 50%, transform translateX(-50%), z-index 2000, width 100%, max-width 560px, padding 0 1rem,
  pointer-events none), `.md-toast` (pointer-events all, background white, border-left `4px solid var(--md-error)`,
  `--md-radius-md`, `--md-elevation-2`, padding `var(--md-space-md)`, display flex, align-items flex-start, gap 12px,
  animation slideDown 0.3s ease), `.md-toast__icon` (font-size 1.4rem, flex-shrink 0), `.md-toast__headline` (
  font-weight 600, `--md-error`, font-size 0.9rem, margin-bottom 4px), `.md-toast__body` (`--md-on-surface-variant`,
  font-size 0.82rem, line-height 1.5), `@keyframes slideDown` (from opacity 0 translateY(-12px) to opacity 1 translateY(
  0))
- [x] T027 Rewrite `src/main/resources/templates/fragments/send-error.html`: remove all inline `style=` attributes;
  apply `class="md-toast-container"` to the fragment root div (keep `id="toast-container"` and `hx-swap-oob="true"`);
  apply `class="md-toast"` to the inner alert div; apply `class="md-toast__icon"` to the emoji span; apply
  `class="md-toast__headline"` to the headline div; apply `class="md-toast__body"` to the message div; remove `<style>`
  block from fragment (keyframes now live in `material.css`)
- [x] T028 Update `src/main/resources/templates/board/index.html`: update the `#toast-container` div to use
  `class="md-toast-container"` (removing inline `style=`) — ensure this matches the OOB fragment target
  `id="toast-container"`
- [x] T029 [P] Search all templates for any remaining `#667eea` (old brand purple) references and replace with
  `var(--md-primary)` or remove — files to check: `board/index.html`, `auth/login.html`, `issues/form.html`,
  `fragments/issue-card.html`, `fragments/issue-detail.html`, `fragments/send-error.html`
- [x] T030 [P] Search all templates for remaining inline `style=` attributes that contain hardcoded colour values or
  typography not covered by CSS classes — replace with appropriate `material.css` classes or add targeted utility
  classes to `material.css`
- [x] T031 Run `./gradlew build` and verify clean build; manually walk through quickstart.md Scenario 1–5 and tick off
  the Visual Consistency Checklist at the bottom of `specs/005-material-redesign/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 (stylesheet must exist before linking)
- **Phase 3–6 (User Stories)**: Each depends on Phase 2; stories are otherwise independent of each other
- **Phase 7 (Polish)**: Depends on all desired user stories being complete

### User Story Dependencies

- **US1 (P1 — Board)**: Can start after Phase 2 — no dependency on US2/US3/US4
- **US2 (P2 — Modal)**: Can start after Phase 2 — independent; T018 removes CSS from `board/index.html` which was added
  by T013 (US1), so US2 should follow US1 in practice
- **US3 (P3 — Login)**: Can start after Phase 2 — fully independent
- **US4 (P4 — Form)**: Can start after Phase 2 — fully independent; app bar class from US1 (T007) must be in stylesheet
  first

### Within Each User Story

- CSS classes in `material.css` must be written (T007–T011 for US1) before the template is updated (T012–T015)
- Template update tasks within a story can proceed in parallel once their CSS tasks are done

### Parallel Opportunities

Within Phase 2: T005 and T006 can run in parallel (different files).  
Within Phase 3: T007–T011 (all CSS additions to same file — sequential); T012 and T014–T015 can run in parallel once
T007–T011 done.  
Phase 5 (US3) and Phase 6 (US4) can run in parallel once Phase 3 is done and the base classes are in `material.css`.  
Phase 7: T029 and T030 can run in parallel.

---

## Parallel Example: Phase 3 (User Story 1)

```text
# Sequential CSS additions (all go to material.css — do in order):
T007: Add app bar classes to material.css
T008: Add board/column layout classes to material.css
T009: Add issue card classes to material.css
T010: Add priority + sent badge classes to material.css
T011: Add send button classes to material.css

# Once T007–T011 are done, these template updates can run in parallel:
T012: Rewrite board/index.html body structure
T014: Update fragments/issue-card.html class names      [P]
T015: Update fragments/column.html empty state class    [P]

# Sequential cleanup (T013 touches board/index.html after T012):
T013: Remove inline <style> block from board/index.html
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Create `material.css` with tokens and base classes (T001–T003)
2. Complete Phase 2: Link stylesheet in templates (T004–T006)
3. Complete Phase 3: Board view redesign (T007–T015)
4. **STOP and VALIDATE**: Walk Scenario 2 in `quickstart.md`
5. Board is now visually complete and independently demoable

### Incremental Delivery

1. Phase 1 + 2 → stylesheet wired up, no visual change
2. Phase 3 (US1) → Board redesigned ✓ **MVP**
3. Phase 4 (US2) → Modal redesigned ✓
4. Phase 5 (US3) → Login redesigned ✓
5. Phase 6 (US4) → Form redesigned ✓
6. Phase 7 (Polish) → Toast + final pass ✓ **Done**

---

## Notes

- **Do not rename or remove any `hx-*` attributes, `id` attributes used by HTMX, or fragment names** — these are
  functional contracts, not styling.
- **Preserve `th:*`, `sec:*` Thymeleaf/security attributes** on all elements.
- The `.issue-card` class name is preserved (used as HTMX OOB swap target `#issue-{id}` and hover selector) — only child
  element class names change.
- The `.htmx-indicator` class is preserved for HTMX loading state behaviour.
- `#toast-container` `id` is preserved for OOB injection by `send-error.html`.
- After each phase, run `./gradlew build` to confirm no template syntax errors.
