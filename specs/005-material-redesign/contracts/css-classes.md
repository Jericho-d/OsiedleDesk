# CSS Class Contract: Material Design UI Redesign

**Feature**: `005-material-redesign` | **Date**: 2026-02-22

> This feature introduces no new HTTP endpoints. The "contract" is the CSS class interface — the named classes that
> templates use and `material.css` must implement.

---

## Shared Stylesheet

**File**: `src/main/resources/static/css/material.css`  
**Linked from**: all authenticated templates via `<link rel="stylesheet" th:href="@{/css/material.css}">`

---

## Class Contract by Component

### App Bar

| Class                  | Element                 | Behaviour                                                         |
|------------------------|-------------------------|-------------------------------------------------------------------|
| `.md-app-bar`          | `<header>`              | Full-width sticky top bar, `64px` tall, `--md-primary` background |
| `.md-app-bar__title`   | `<span>` inside app bar | White text, headline weight                                       |
| `.md-app-bar__actions` | `<div>` inside app bar  | Right-aligned flex row for action buttons                         |

### Board Layout

| Class                | Element                    | Behaviour                                                        |
|----------------------|----------------------------|------------------------------------------------------------------|
| `.md-board`          | `<div>` containing columns | CSS grid, 5 equal columns, gap 16px                              |
| `.md-column`         | `<div>` column lane        | `--md-surface-variant` background, rounded, flex column          |
| `.md-column__header` | `<div>` column title       | `--md-primary-container` tint, headline typography, status label |
| `.md-column__list`   | `<div>` card container     | Scrollable, flex column, gap 8px                                 |
| `.md-column__empty`  | `<div>` empty state        | Centred, `--md-on-surface-variant`, italic, icon or text         |

### Issue Card

| Class                      | Element                | Behaviour                                                           |
|----------------------------|------------------------|---------------------------------------------------------------------|
| `.issue-card`              | `<div>` card root      | White surface, `--md-elevation-1`, `--md-radius-md`, cursor pointer |
| `.issue-card:hover`        | —                      | `--md-elevation-2`, translate `-2px` Y                              |
| `.issue-card__title`       | `<div>` title          | Body-medium weight, `--md-on-surface`, max 2 lines then ellipsis    |
| `.issue-card__description` | `<div>` preview        | Caption size, `--md-on-surface-variant`, max 2 lines                |
| `.issue-card__footer`      | `<div>` meta row       | Flex, space-between, align center                                   |
| `.issue-card__actions`     | `<div>` admin controls | Flex, gap 8px, top border divider                                   |

### Priority Badge

| Class                | Element             | Behaviour                                                   |
|----------------------|---------------------|-------------------------------------------------------------|
| `.priority`          | `<span>` base class | Label typography, rounded `--md-radius-sm`, padding 2px 8px |
| `.priority-LOW`      | —                   | `--md-priority-low-bg` / `--md-priority-low-text`           |
| `.priority-MEDIUM`   | —                   | `--md-priority-medium-bg` / `--md-priority-medium-text`     |
| `.priority-HIGH`     | —                   | `--md-priority-high-bg` / `--md-priority-high-text`         |
| `.priority-CRITICAL` | —                   | `--md-priority-critical-bg` / `--md-priority-critical-text` |

### Sent Badge

| Class         | Element  | Behaviour                                                              |
|---------------|----------|------------------------------------------------------------------------|
| `.sent-badge` | `<span>` | `--md-sent` background, `--md-on-sent` text, label typography, rounded |

### Send Button

| Class             | Element    | Behaviour                                                                             |
|-------------------|------------|---------------------------------------------------------------------------------------|
| `.btn-send`       | `<button>` | Filled button — `--md-success` background, white text, `--md-radius-md`, label weight |
| `.btn-send:hover` | —          | Darkened success colour, subtle shadow                                                |

### Issue Detail Modal

| Class                   | Element               | Behaviour                                                           |
|-------------------------|-----------------------|---------------------------------------------------------------------|
| `.issue-modal-backdrop` | `<div>` fixed overlay | Semi-transparent black backdrop                                     |
| `.issue-detail-panel`   | `<div>` dialog        | White surface, `--md-elevation-3`, `--md-radius-lg`, max 640px wide |
| `.detail-header`        | `<div>`               | Bottom border divider, flex space-between                           |
| `.detail-title`         | `<h2>`                | Headline typography, `--md-on-surface`                              |
| `.detail-close-btn`     | `<button>`            | Icon button, `--md-on-surface-variant`                              |
| `.detail-section`       | `<div>`               | Padded section with bottom divider                                  |
| `.detail-label`         | `<div>`               | Caption, `--md-on-surface-variant`, uppercase, letter-spaced        |
| `.detail-description`   | `<div>`               | Body, `--md-on-surface`, pre-wrap                                   |
| `.detail-meta`          | `<div>`               | 2-col grid of metadata rows                                         |
| `.detail-meta-row`      | `<div>`               | Flex column: label + value                                          |
| `.detail-status-badge`  | `<span>`              | Neutral chip, `--md-surface-variant`, label typography              |
| `.detail-actions`       | `<div>`               | Bottom action row, flex, gap                                        |
| `.detail-actions-label` | `<div>`               | Caption section label                                               |
| `.detail-action-btn`    | `<button>`            | Alias for `.btn-send` in modal context                              |

### Login Page

| Class                   | Element                             | Behaviour                                                                 |
|-------------------------|-------------------------------------|---------------------------------------------------------------------------|
| `.md-login-page`        | `<body>` or wrapper                 | Full-height, centred flex, `--md-surface-variant` background              |
| `.md-login-card`        | `<div>`                             | White card, `--md-elevation-2`, `--md-radius-lg`, max 400px, padding 32px |
| `.md-login-card__title` | `<h1>`                              | Display typography, `--md-primary`, centred                               |
| `.md-form-group`        | `<div>`                             | Label + input block, margin-bottom 16px                                   |
| `.md-input`             | `<input>`, `<textarea>`, `<select>` | Full width, outlined style, `--md-radius-md`, focus ring `--md-primary`   |
| `.md-input--error`      | modifier                            | Error border colour `--md-error`                                          |
| `.md-btn-primary`       | `<button>`                          | Filled button, full width, `--md-primary` background                      |
| `.md-alert`             | `<div>`                             | Alert base                                                                |
| `.md-alert--error`      | modifier                            | `--md-error` tint background, error text                                  |
| `.md-alert--success`    | modifier                            | `--md-success` tint background, success text                              |

### Issue Form

| Class               | Element                             | Behaviour                                                        |
|---------------------|-------------------------------------|------------------------------------------------------------------|
| `.md-form-page`     | `<body>` or wrapper                 | `--md-surface-variant` background, padding                       |
| `.md-form-card`     | `<div>` container                   | White card, `--md-elevation-1`, max 800px, centred               |
| `.md-form-group`    | `<div>`                             | Shared with login                                                |
| `.md-input`         | `<input>`, `<textarea>`, `<select>` | Shared with login                                                |
| `.md-btn-primary`   | `<button>`                          | Shared with login                                                |
| `.md-btn-secondary` | `<button>` or `<a>`                 | Outlined/text style button, `--md-outline` border                |
| `.md-error-message` | `<div>`                             | Validation error, `--md-error` text, caption size                |
| `.md-hint`          | `<div>`                             | Helper text below input, `--md-on-surface-variant`, caption size |

### Error Toast

| Class                 | Element  | Behaviour                                                                       |
|-----------------------|----------|---------------------------------------------------------------------------------|
| `.md-toast`           | `<div>`  | Snackbar/toast — left-bordered, white, `--md-elevation-2`, slide-down animation |
| `.md-toast__icon`     | `<span>` | Error icon glyph                                                                |
| `.md-toast__headline` | `<div>`  | Bold error headline, `--md-error`                                               |
| `.md-toast__body`     | `<div>`  | Body message, `--md-on-surface-variant`                                         |

### HTMX Loading Indicator (preserved class name)

| Class             | Element | Behaviour                                                                             |
|-------------------|---------|---------------------------------------------------------------------------------------|
| `.htmx-indicator` | `<div>` | Hidden by default; shown during HTMX request with a subtle spinner or "Loading…" text |
