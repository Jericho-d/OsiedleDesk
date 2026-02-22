## 1. Backend — Status Move Endpoint

- [x] 1.1 Add `POST /api/issues/{id}/status` handler to `IssueViewController` (admin-only, `@PreAuthorize("hasRole('ADMINISTRATOR')")`)
- [x] 1.2 Tighten `IssueService.validateStatusTransition` to reject moves FROM PREPARED (must 400) and block illegal transitions
- [x] 1.3 Handler returns OOB issue-card fragment on success (same pattern as `/send`)
- [x] 1.4 Handler returns OOB detail fragment if `source=detail` request param is present
- [x] 1.5 Handler returns 400 with error fragment on invalid transition

## 2. Templates — Status Move Buttons

- [x] 2.1 Add status-move buttons to `issue-card.html` admin actions section (IN_PROGRESS → ACKNOWLEDGED/WONT_DO; ACKNOWLEDGED → RESOLVED/WONT_DO; RESOLVED → PREPARED/WONT_DO; WONT_DO → PREPARED) using `th:if` guards
- [x] 2.2 Add status-move buttons to `issue-detail.html` Admin Actions section using the same transition rules
- [x] 2.3 Wire status-move buttons with `hx-post` to `/api/issues/{id}/status?target=...` and `hx-swap="none"` (OOB handles refresh)

## 3. Templates — Send Button Redesign

- [x] 3.1 Replace `btn-send` class with `md-btn-send` on the send button in `issue-card.html`
- [x] 3.2 Replace `btn-send` class with `md-btn-send` on the send button in `issue-detail.html`
- [x] 3.3 Update button label to include ✉️ emoji and "SEND TO ADMINISTRATION" text

## 4. Templates — Column Headers and Emoji

- [x] 4.1 Add emoji prefixes to column headers in `board/index.html` (📋 PREPARED, 🔄 IN PROGRESS, 👀 ACKNOWLEDGED, ✅ RESOLVED, 🚫 WON'T DO)
- [x] 4.2 Update priority badge text in `issue-card.html` to include emoji prefix (🔴 CRITICAL, 🟠 HIGH, 🟡 MEDIUM, 🟢 LOW) using Thymeleaf expression

## 5. CSS — Send Button and Status Move Styles

- [x] 5.1 Add `.md-btn-send` style to `material.css`: full-width, `#1976D2` background, white text, 8dp shadow, hover `#1565C0`, uppercase, bold
- [x] 5.2 Add `.md-btn-status-move` style for status-move buttons: outlined or secondary style, small, pill-shaped with emoji label
- [x] 5.3 Update `.sent-badge` style to be more prominent (green filled chip)

## 6. CSS — Animations

- [x] 6.1 Add `@keyframes card-enter` (translateY(12px) + opacity 0 → translateY(0) + opacity 1, 250ms ease-out) to `material.css`
- [x] 6.2 Apply `animation: card-enter 0.25s ease-out` to `.issue-card` rule in `material.css`
- [x] 6.3 Add `@keyframes card-flash` (0% → 100% background flash with `#E3F2FD`) to `material.css`
- [x] 6.4 Apply `card-flash` animation to `.issue-card.htmx-settling` rule (500ms, ease-out)

## 7. Verification

- [x] 7.1 Run `./gradlew build` and confirm no compilation errors
- [ ] 7.2 Manually verify: PREPARED card shows no move buttons; other statuses show correct options
- [ ] 7.3 Manually verify: Send button renders as full-width blue button with emoji
- [ ] 7.4 Manually verify: Card entry animation fires on column load
- [ ] 7.5 Manually verify: Card flash fires after status move
