# Data Model: Material Design UI Redesign

**Feature**: `005-material-redesign` | **Date**: 2026-02-22

> This feature introduces no new backend entities. The "model" for a UI redesign is the **design token system** — the named, reusable values that define the visual language across all templates.

---

## Design Tokens (CSS Custom Properties)

Defined once in `:root` inside `src/main/resources/static/css/material.css`.

### Colour Tokens

| Token | Value | Description |
|-------|-------|-------------|
| `--md-primary` | `#1976D2` | Primary brand colour — app bar, primary buttons, links |
| `--md-primary-dark` | `#1565C0` | Hover/active state for primary surfaces |
| `--md-primary-container` | `#E3F2FD` | Column header background tint |
| `--md-on-primary` | `#FFFFFF` | Text/icons on primary-coloured surfaces |
| `--md-surface` | `#FFFFFF` | Card backgrounds, modal background |
| `--md-surface-variant` | `#F5F5F5` | Page background, column lane background |
| `--md-outline` | `#E0E0E0` | Dividers, input field borders |
| `--md-on-surface` | `#212121` | Primary body text |
| `--md-on-surface-variant` | `#757575` | Secondary/label text |
| `--md-error` | `#D32F2F` | Error toast accent, error input borders |
| `--md-on-error` | `#FFFFFF` | Text on error-coloured surfaces |
| `--md-success` | `#2E7D32` | Send button background |
| `--md-on-success` | `#FFFFFF` | Text on success-coloured surfaces |
| `--md-sent` | `#0277BD` | SENT badge background |
| `--md-on-sent` | `#FFFFFF` | Text on sent badge |

### Priority Badge Tokens

| Token (bg) | Value | Token (text) | Value | Priority |
|-----------|-------|-------------|-------|----------|
| `--md-priority-low-bg` | `#E8F5E9` | `--md-priority-low-text` | `#2E7D32` | LOW |
| `--md-priority-medium-bg` | `#FFF8E1` | `--md-priority-medium-text` | `#F57F17` | MEDIUM |
| `--md-priority-high-bg` | `#FFF3E0` | `--md-priority-high-text` | `#E65100` | HIGH |
| `--md-priority-critical-bg` | `#FFEBEE` | `--md-priority-critical-text` | `#C62828` | CRITICAL |

### Typography Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--md-font-family` | `'Roboto', system-ui, -apple-system, sans-serif` | All text |
| `--md-type-display` | `600 1.5rem/2rem var(--md-font-family)` | Page titles |
| `--md-type-headline` | `500 1.1rem/1.5rem var(--md-font-family)` | Modal title, column header |
| `--md-type-body` | `400 0.9rem/1.5rem var(--md-font-family)` | Card descriptions, body text |
| `--md-type-label` | `500 0.75rem/1rem var(--md-font-family)` | Badges, metadata labels |
| `--md-type-caption` | `400 0.75rem/1.1rem var(--md-font-family)` | Issue IDs, hints |

### Spacing Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--md-space-xs` | `4px` | Tight gaps within components |
| `--md-space-sm` | `8px` | Inner component padding |
| `--md-space-md` | `16px` | Standard component padding |
| `--md-space-lg` | `24px` | Section spacing |
| `--md-space-xl` | `32px` | Page-level spacing |

### Elevation Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--md-elevation-1` | `0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.08)` | Resting card |
| `--md-elevation-2` | `0 4px 8px rgba(0,0,0,0.15), 0 2px 4px rgba(0,0,0,0.10)` | Hover card |
| `--md-elevation-3` | `0 8px 32px rgba(0,0,0,0.20), 0 4px 12px rgba(0,0,0,0.12)` | Modal dialog |

### Shape Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--md-radius-sm` | `4px` | Badges, small chips |
| `--md-radius-md` | `8px` | Cards, inputs, buttons |
| `--md-radius-lg` | `12px` | Modal dialog corners |

---

## UI Component Inventory

Each UI component maps to one or more template files.

| Component | Template File(s) | User Story |
|-----------|-----------------|------------|
| Top App Bar | `board/index.html`, `issues/form.html` | US1, US4 |
| Board Column Lane | `board/index.html`, `fragments/column.html` | US1 |
| Issue Card | `fragments/issue-card.html` | US1 |
| Priority Badge | `fragments/issue-card.html`, `fragments/issue-detail.html` | US1, US2 |
| Send Button | `fragments/issue-card.html`, `fragments/issue-detail.html` | US1, US2 |
| Sent Badge | `fragments/issue-card.html`, `fragments/issue-detail.html` | US1, US2 |
| Issue Detail Modal | `fragments/issue-detail.html` | US2 |
| Login Card | `auth/login.html` | US3 |
| Issue Form | `issues/form.html` | US4 |
| Error Toast | `fragments/send-error.html` | US1, US2 |
| Empty Column State | `fragments/column.html` | US1 |
