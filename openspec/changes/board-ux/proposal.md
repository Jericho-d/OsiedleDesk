## Why

The board currently has no way to manually move issues between columns — the only status transition is the Send Email flow (PREPARED → IN_PROGRESS). This forces admins through a rigid workflow and makes the board feel static. The UI also suffers from a poor Send button design and lacks the polish and feedback (animations, emoji cues) expected from a modern issue tracker.

## What Changes

- Add status-transition controls to issue cards and the detail modal so admins can move issues between columns directly
- PREPARED is the only status that cannot be manually moved forward — it can only advance to IN_PROGRESS via the existing Send Email flow (preserving the email-gate)
- Any status can be moved to WON'T_DO (rejection override stays)
- RESOLVED issues can be reopened to PREPARED
- Redesign the Send Email button — replace the plain `btn-send` style with a prominent, clearly branded Material button
- Add CSS entry/exit animations for cards when they move between columns
- Add emoji decorations to column headers and status badges for visual clarity
- General visual polish: priority chip styles, card hover states, detail panel improvements

## Capabilities

### New Capabilities
- `board-status-move`: Inline status-transition UI on issue cards and detail modal, with allowed-transition rules enforced on the server

### Modified Capabilities
- `issue-send`: Send Email button redesign — new visual style, same endpoint, same HTMX wiring

## Impact

- `src/main/resources/templates/fragments/issue-card.html` — add status-move controls (HTMX)
- `src/main/resources/templates/fragments/issue-detail.html` — add status-move controls + Send button redesign
- `src/main/resources/static/css/material.css` — new animation keyframes, Send button styles, status-move button styles
- `src/main/java/com/administrativetool/controller/api/IssueController.java` — verify/expose `POST /api/issues/{id}/status` endpoint accepts target status
- No schema changes, no new dependencies
