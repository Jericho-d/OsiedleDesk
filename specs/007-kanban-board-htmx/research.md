# Research: Kanban Board with HTMX and Material Design

**Branch**: `007-kanban-board-htmx`  
**Date**: 2026-02-22  
**Phase**: 0 — Research

All decisions below resolve implementation questions identified during Technical Context analysis.

---

## 1. HTMX Partial Swap Strategy for Cross-Column Drag

**Decision**: Use a single HTTP response containing the primary target column swap (inline) plus an out-of-band (OOB)
sibling element representing the source column swap (`hx-swap-oob="innerHTML"`). Both column fragments are returned from
one controller method in a single `th:block th:fragment` wrapper.

**Rationale**: A single round-trip for two DOM updates is the canonical HTMX approach. It eliminates the race condition
where the card would briefly appear in both columns simultaneously (as would happen with two sequential requests). HTMX
2.x processes OOB elements adjacent to the primary response body with `htmx.config.allowNestedOobSwaps` defaulting to
`true`. The existing `FragmentController` already uses this OOB pattern for the Send button response.

**Approach detail**:

- `POST /fragments/board/move` returns template `fragments/board :: move-response`
- The `move-response` fragment is a `<th:block>` containing:
    1. The re-rendered target column's card list (primary swap — HTMX puts this into the `hx-target` column list)
    2. A `<div id="{source-status-lower}-list" hx-swap-oob="innerHTML">` with the source column's re-rendered cards
- Column list divs use stable IDs: `prepared-list`, `in-progress-list`, `acknowledged-list`, `resolved-list`,
  `wont-do-list`

**Alternatives considered**:

- Two separate requests (one per column): requires JS coordination; race condition risk; double network load. Rejected.
- `HX-Trigger` header to fire column refresh events: fires two additional GETs after the POST; extra round-trips. Valid
  fallback but rejected as primary approach.
- `hx-swap-oob` on individual issue card elements: cannot express "remove from source column + add to target column"
  atomically at card level. Rejected for cross-column moves.

---

## 2. Triggering an HTMX Request Programmatically from a `drop` Event

**Decision**: Use `htmx.ajax('PATCH', url, { target, swap, values, source })` called from the native `drop` event
handler. Pass the drop-target column element as `source` so CSRF headers from ancestor `hx-headers` declarations
propagate correctly.

**Rationale**: `htmx.ajax()` is the stable, documented public API for programmatic HTMX requests. It fully participates
in the HTMX lifecycle: `htmx-request` class is applied, OOB swaps in the response are processed, `HX-Trigger` response
headers fire client events. A hidden-form + `htmx.trigger()` workaround would require per-column form instances and
dynamic `action` rewriting, adding DOM noise without benefit.

**Call pattern** (illustrative, not implementation code):

```
htmx.ajax('PATCH', `/fragments/board/move`,  {
    target:  `#{targetStatus}-list`,    // e.g. '#in-progress-list'
    swap:    'innerHTML',
    values:  { issueId, targetStatus },
    source:  dropZoneElement             // inherits hx-headers (CSRF)
})
```

**Alternatives considered**:

- `htmx.trigger()` on a hidden form: indirect; requires dynamic form setup per drag. Rejected.
- Raw `fetch()` + manual DOM manipulation: bypasses HTMX lifecycle; loses OOB processing. Rejected.

---

## 3. Thymeleaf Fragment Syntax for Two-Fragment Response

**Decision**: Controller returns `"fragments/board :: move-response"`. The fragment is a `<th:block>` containing two
child elements — the inline swap content and the OOB element. `<th:block>` is rendered as nothing by Thymeleaf (the tag
is stripped), leaving only the two child elements as adjacent siblings in the HTTP response body.

**Fragment structure**:

```html
<th:block th:fragment="move-response">
    <!-- Primary target: contents go into hx-target -->
    <th:block th:replace="~{fragments/issue-card :: column-content(${targetIssues})}"></th:block>
    <!-- OOB: HTMX swaps innerHTML of #prepared-list -->
    <div th:id="${sourceStatusId + '-list'}"
         hx-swap-oob="innerHTML"
         th:insert="~{fragments/issue-card :: column-content(${sourceIssues})}">
    </div>
</th:block>
```

**Key syntax rules**:

- `hx-swap-oob="innerHTML"` (without a CSS selector suffix) → HTMX matches by the `id` attribute of the element itself
- The OOB element `id` must exactly match the live DOM element's `id` (e.g., `prepared-list`)
- `th:block` outer wrapper is stripped from output — the two children become top-level response nodes

**Alternatives considered**:

- `hx-swap-oob="innerHTML:#prepared-list"` form: more explicit and decouples the element's own `id` from the target
  selector. Valid alternative; slightly more verbose. Can be used if fragment reuse in other contexts requires different
  IDs.
- Returning two Strings concatenated in `@ResponseBody` with `StringWriter`: bypasses Thymeleaf rendering; not
  maintainable. Rejected.

---

## 4. HTML5 Drag and Drop API — Minimal JS Pattern

**Decision**: Use four event handlers (`dragstart`, `dragover`, `drop`, `dragend`) with module-scoped state for DOM
references. Encode `{ issueId, sourceStatus }` in `DataTransfer` using `application/json` MIME type. Use a sentinel MIME
type (`application/x-issue`) for `dragover` type-checking (since `DataTransfer` data cannot be read during `dragover`,
only its `types` array is available).

**Canonical event responsibilities**:

| Event       | Responsibility                                                                                                                                                                                                                                    |
|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `dragstart` | `setData('application/x-issue', '')` + `setData('application/json', JSON.stringify({issueId, sourceStatus}))`. Set `effectAllowed = 'move'`. Save `dragState = { card, originalParent, originalNextSibling }`. Apply `.card--dragging` CSS class. |
| `dragover`  | `e.preventDefault()` only if `types.includes('application/x-issue')`. Set `dropEffect = 'move'`. Apply `.column--drag-over` CSS class to column.                                                                                                  |
| `dragleave` | Remove `.column--drag-over` from column.                                                                                                                                                                                                          |
| `drop`      | `e.preventDefault()`. `getData('application/json')`. Fire `htmx.ajax()`. Apply `.card--pending` class.                                                                                                                                            |
| `dragend`   | Remove `.card--dragging`. If `dropEffect === 'none'` → drag was cancelled → `insertBefore()` revert + clear `dragState`.                                                                                                                          |

**Alternatives considered**:

- Temporary `id` attribute pattern (MDN Kanban example): pollutes the DOM globally; requires cleanup. Rejected.
- Encoding only `issueId` (not `sourceStatus`) in `DataTransfer`: requires extra DOM query to find source at drop time.
  Rejected.
- SortableJS library: idiomatic for HTMX cross-list sorting; adds a JS dependency. Decided against to honour the "plain
  vanilla JavaScript only" constraint from the feature description.

---

## 5. Error Recovery — DOM Revert Pattern

**Decision**: Store `{ originalParent, originalNextSibling }` in module-scope `dragState` at `dragstart`. On
`htmx:afterRequest` with `event.detail.failed === true`, call `originalParent.insertBefore(card, originalNextSibling)`
to restore exact position. Remove `.card--pending` class.

**Why `htmx:afterRequest` instead of `htmx:responseError`**: `htmx:afterRequest` with `event.detail.failed === true`
covers both HTTP error responses (4xx/5xx) and network-level failures (timeout, offline). `htmx:responseError` only
covers received HTTP errors, missing the network-failure case.

**Why module-scope state beats server re-render for revert**: A server re-render of both columns on every failure fires
an additional network request that can itself fail, and replaces the entire column DOM, destroying any in-flight
transitions on other cards in those columns.

**Alternatives considered**:

- Server re-render of both columns: appropriate for stale-state recovery (concurrent edits), not for single failed
  request. Retained as an optional fallback.
- `innerHTML` snapshot of the column: captures more state than needed; re-triggers HTMX on re-insertion; causes event
  listener leaks. Rejected.

---

## 6. Material Design 3 Elevation Shadows

**Decision**: Use the M3 dual-layer shadow model (key shadow + ambient shadow) at two levels:

- **Resting (Level 1)**: `0px 1px 2px rgba(0,0,0,0.3), 0px 1px 3px 1px rgba(0,0,0,0.15)`
- **Dragging (Level 3)**: `0px 1px 3px rgba(0,0,0,0.3), 0px 4px 8px 3px rgba(0,0,0,0.15)`

**Transition**: `box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1)` — this is the MDC-Web standard curve, not
`cubic-bezier(0.2, 0, 0, 1.0)`. The spec request references an older M3 "emphasized" easing that does not exist in the
MDC-Web source; the standard curve (`0.4, 0, 0.2, 1`) at 280ms is the authoritative value for elevation transitions.

**Note on existing `material.css`**: The project's existing elevation tokens use different opacity values and a
different shadow geometry. This feature's CSS additions will introduce M3-compliant values for card and drag states.
Existing tokens are not removed to avoid breaking other components.

**Alternatives considered**:

- M2-style three-layer shadows (umbra/penumbra/ambient at 0.20/0.14/0.12): technically accurate for M2 but M3 uses a
  simpler two-layer model. Rejected in favour of M3.
- Single `box-shadow` layer: visually flat; not spec-compliant. Rejected.

---

## 7. Material Design 3 Color and Typography Tokens

**Decision**: Introduce `--md-sys-color-*` tokens alongside the existing `--md-*` tokens. The feature's new components
will use the `--md-sys-color-*` namespace. Existing components are not migrated (out of scope).

**Token values** (M3 baseline light theme):

| Token                               | Value     |
|-------------------------------------|-----------|
| `--md-sys-color-primary`            | `#6750A4` |
| `--md-sys-color-on-primary`         | `#FFFFFF` |
| `--md-sys-color-surface`            | `#FFFBFE` |
| `--md-sys-color-surface-variant`    | `#E7E0EC` |
| `--md-sys-color-surface-container`  | `#F3EDF7` |
| `--md-sys-color-on-surface`         | `#1C1B1F` |
| `--md-sys-color-on-surface-variant` | `#49454F` |
| `--md-sys-color-error`              | `#B3261E` |
| `--md-sys-color-outline`            | `#79747E` |
| `--md-sys-color-outline-variant`    | `#CAC4D0` |
| `--md-sys-color-surface-tint`       | `#6750A4` |

**Typography**:

- `title-medium`: `font-weight: 500; font-size: 1rem; line-height: 1.5rem` — used for issue card titles
- `label-small`: `font-weight: 500; font-size: 0.6875rem; line-height: 1rem` — used for metadata (assignee, dates)
- Priority chips use `label-large`: `font-weight: 500; font-size: 0.875rem; line-height: 1.25rem` (M3 Filter Chip
  typography role)

**Alternatives considered**:

- Using Material Theme Builder to generate a custom palette: appropriate for production; out of scope for this feature.
  Baseline purple palette used.
- Keeping existing `--md-*` tokens only: avoids introducing a parallel token set but means new components diverge
  visually from M3 spec. Rejected; feature explicitly requires M3 compliance.

---

## 8. Material Design 3 State Layers

**Decision**: Implement state layers as a `::after` pseudo-element overlay on interactive cards and chips. Use
`--md-sys-color-on-surface` as the layer color with opacity modulated by state class:

| State   | Opacity | CSS trigger                                |
|---------|---------|--------------------------------------------|
| Hover   | 8%      | `:hover::after { opacity: 0.08 }`          |
| Pressed | 12%     | `:active::after { opacity: 0.12 }`         |
| Dragged | 16%     | `.card--dragging::after { opacity: 0.16 }` |

The `::after` pseudo-element is
`position: absolute; inset: 0; border-radius: inherit; background: var(--md-sys-color-on-surface); pointer-events: none; opacity: 0; transition: opacity 15ms linear`.

**No animated ripple** — the ripple effect from `<md-ripple>` is not implemented in plain CSS alone; the static overlay
approximates the M3 state layer contract sufficiently for this feature.

**Alternatives considered**:

- `<md-ripple>` web component from `@material/web`: adds a JS dependency and potentially the entire `@material/web` npm
  package. Overengineered for this feature. Rejected.
- Modifying `background-color` on hover: interferes with existing surface colour; does not translate to
  `border-radius: inherit` shape. Rejected.

---

## 9. Material Design 3 Filter Chip (Priority Badge)

**Decision**: Priority badges are styled as M3-inspired chips (not interactive Filter Chips, as they carry no toggle
state). Use:

- `border-radius: 8px` (M3 `corner-small` shape token)
- `height: 32px; padding-inline: 12px`
- Typography: `label-large` (14sp, weight 500)
- Background: static, per-priority colour using M3 tonal surface variants

Priority colour mapping (using M3 tonal palette approach):
| Priority | Background | Text |
|----------|-----------|------|
| LOW | `#E8F5E9` (green tonal surface) | `#1B5E20` |
| MEDIUM | `#FFF8E1` (amber tonal surface) | `#E65100` |
| HIGH | `#FBE9E7` (deep-orange tonal surface) | `#BF360C` |
| CRITICAL | `#FCE4EC` (pink/red tonal surface) | `#880E4F` |

**Alternatives considered**:

- `<md-filter-chip>` web component: requires `@material/web` package. Rejected (plain CSS constraint).
- Fully round badge (`border-radius: 9999px`): softer look, valid M3 `corner-full`. Functional; `8px` chosen to match M3
  Filter Chip spec.

---

## Summary of All Decisions

| Question                 | Decision                                                                     | Key Source                       |
|--------------------------|------------------------------------------------------------------------------|----------------------------------|
| HTMX swap strategy       | Single response: inline primary + OOB sibling                                | HTMX 2.x `hx-swap-oob` docs      |
| Programmatic HTMX call   | `htmx.ajax()` from `drop` handler                                            | HTMX public API docs             |
| Thymeleaf multi-fragment | `<th:block th:fragment>` with OOB sibling                                    | Thymeleaf 3.1 `th:block` docs    |
| DnD JS pattern           | Module-scope state; 5 event handlers                                         | HTML5 DnD spec (MDN)             |
| Error revert             | `insertBefore()` with stored `originalNextSibling`; `htmx:afterRequest`      | HTMX event model                 |
| M3 elevation             | Dual-layer shadows at level 1 (resting) and level 3 (dragging)               | MDC-Web `_elevation-theme.scss`  |
| M3 easing curve          | `cubic-bezier(0.4, 0, 0.2, 1)` at 280ms (standard curve, not 0.2,0,0,1.0)    | MDC-Web `_animation.scss`        |
| M3 color tokens          | `--md-sys-color-*` baseline purple scheme alongside existing `--md-*`        | M3 baseline `_md-sys-color.scss` |
| M3 typography            | `title-medium` (1rem/500), `label-small` (0.6875rem/500), chip=`label-large` | M3 `_md-sys-typescale.scss`      |
| M3 state layers          | `::after` pseudo overlay; hover 8%, pressed 12%, dragged 16%                 | MDC-Web `_ripple.scss`           |
| Priority chips           | 8px radius, label-large, tonal backgrounds per priority                      | M3 chip spec                     |
