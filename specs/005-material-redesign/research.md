# Research: Material Design UI Redesign

**Feature**: `005-material-redesign` | **Date**: 2026-02-22

---

## Decision 1: CSS Architecture — Single Shared Stylesheet vs Inline Per-Template

**Decision**: Extract all CSS into a single shared file at `src/main/resources/static/css/material.css`, linked from every template via `<link rel="stylesheet">`. CSS custom properties (variables) defined in `:root` provide the design token layer.

**Rationale**: The current approach embeds a full `<style>` block in every template. This duplicates rules, makes global changes expensive (must edit every file), and is the primary reason the design feels incoherent. A single stylesheet enforces consistency and satisfies FR-013.

**Alternatives considered**:
- Keep per-template inline styles — rejected: does not satisfy FR-013, changes must be replicated across 6 files.
- Use a CSS-in-JS or build-step approach — rejected: the spec explicitly rules out new frameworks; Thymeleaf serves static files natively.

---

## Decision 2: Material Design Library — Full MUI/MDC vs Reference-Only Vanilla CSS

**Decision**: Write vanilla CSS inspired by Material Design 3 principles (elevation, colour tokens, typography scale, shape system). Do **not** import Google's Material Web Components or MUI. Link only Google Fonts (Roboto) from CDN for typography.

**Rationale**: The spec assumption states "Material Design is used as a visual reference — not as a strict component library import." Importing MDC/MUI would add hundreds of KB of JS, introduce a component API that conflicts with existing Thymeleaf fragment structure, and risk breaking HTMX interactions (FR-012). Vanilla CSS gives full control with zero runtime overhead.

**Alternatives considered**:
- Material Web (lit-based web components) — rejected: conflicts with HTMX's DOM-swap model; adds JS bundle weight.
- Materialize CSS — rejected: last meaningful release 2018; not Material Design 3.
- Pure vanilla CSS from scratch — this is what is chosen; guided by MD3 spec for tokens and layout.

---

## Decision 3: Typography — Google Fonts CDN vs System Font Stack

**Decision**: Link Roboto (weights 400, 500, 700) from Google Fonts CDN. Fall back to `system-ui, -apple-system, sans-serif` if CDN is unavailable.

**Rationale**: Roboto is the reference typeface for Material Design and meaningfully upgrades the visual quality. The application already references Roboto in its system font stack fallback chain, so the intent was always there. CDN load is ~15 KB; acceptable for a LAN/intranet admin tool.

**Alternatives considered**:
- System font stack only — acceptable fallback but doesn't deliver the "polished" outcome the user requested.
- Self-hosted Roboto — more robust for offline use; deferred as the spec does not require offline capability.

---

## Decision 4: Colour Palette

**Decision**: Use the following design token set, defined as CSS custom properties in `:root`:

| Token | Value | Usage |
|-------|-------|-------|
| `--md-primary` | `#1976D2` | App bar, primary buttons, links, focus rings |
| `--md-primary-dark` | `#1565C0` | Hover state for primary |
| `--md-primary-container` | `#E3F2FD` | Column header background |
| `--md-on-primary` | `#FFFFFF` | Text on primary-coloured surfaces |
| `--md-surface` | `#FFFFFF` | Card and modal backgrounds |
| `--md-surface-variant` | `#F5F5F5` | Page background, column background |
| `--md-outline` | `#E0E0E0` | Dividers, input borders |
| `--md-on-surface` | `#212121` | Primary body text |
| `--md-on-surface-variant` | `#757575` | Secondary text, labels |
| `--md-error` | `#D32F2F` | Error toast, error states |
| `--md-success` | `#2E7D32` | Send button, success indicators |
| `--md-sent` | `#0277BD` | SENT badge |

Priority badge colours (accessible contrast on light background):

| Priority | Background | Text |
|----------|-----------|------|
| LOW | `#E8F5E9` | `#2E7D32` |
| MEDIUM | `#FFF8E1` | `#F57F17` |
| HIGH | `#FFF3E0` | `#E65100` |
| CRITICAL | `#FFEBEE` | `#C62828` |

**Rationale**: MD3 tonal palette derived from primary blue. Warm amber/orange/red for priority levels gives intuitive severity gradient. All combinations pass WCAG AA 4.5:1 for normal text.

**Alternatives considered**:
- Saturated/filled priority badge backgrounds (current HIGH = `#f8d7da` filled) — kept for CRITICAL; desaturated for LOW/MEDIUM/HIGH to reduce visual noise on a card-heavy board.

---

## Decision 5: Elevation System

**Decision**: Use a 3-level box-shadow scale:

| Level | CSS Value | Usage |
|-------|-----------|-------|
| 1 (resting card) | `0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.08)` | Issue cards at rest |
| 2 (hover card) | `0 4px 8px rgba(0,0,0,0.15), 0 2px 4px rgba(0,0,0,0.10)` | Issue cards on hover |
| 3 (dialog) | `0 8px 32px rgba(0,0,0,0.20), 0 4px 12px rgba(0,0,0,0.12)` | Detail modal |

**Rationale**: MD3 elevation is conveyed via shadow + surface tint. Since we're not using the full MD3 colour system tint overlay, clean layered shadows achieve the same depth signal with simpler CSS.

---

## Decision 6: App Bar

**Decision**: Replace the existing `.header` div with a full-width fixed top app bar (`position: sticky; top: 0`), `64px` tall, primary blue background, white text. Contains: app name (left), "+ New Issue" button (right), logout button (right). Board content gets `padding-top` to clear the bar.

**Rationale**: A sticky app bar is the most recognisable Material Design element and the single biggest visual upgrade. Making it full-width (not a rounded card) follows MD3 top app bar spec.

---

## Decision 7: HTMX Compatibility Constraints

**Decision**: Do not change any `hx-*` attributes, HTMX trigger selectors, or fragment IDs/names. CSS class renames are the only structural change; existing class names that HTMX relies on (e.g., `.issue-card`, `.htmx-indicator`) are preserved and re-styled.

**Rationale**: FR-012 is non-negotiable. Any rename of fragment selectors would require coordinated changes across controller return values and templates. The risk is not justified for a purely visual change.
