# Data Model: Openable Issue Detail View

**Change**: `003-openable-issue`  
**Date**: 2026-02-22

## Summary

**No database schema changes are required for this feature.** The `Issue` entity already contains all fields needed to
display the detail view. This document confirms the existing model is sufficient and records the UI data model (what the
Thymeleaf template receives).

---

## Existing Entity: Issue

```
issues table
├── id             BIGINT PK
├── creator_id     BIGINT
├── title          VARCHAR
├── description    TEXT
├── status         VARCHAR  (PREPARED | IN_PROGRESS | ACKNOWLEDGED | RESOLVED | WONT_DO)
├── priority       VARCHAR  (LOW | MEDIUM | HIGH | CRITICAL)
├── assignee       VARCHAR  (nullable)
├── is_sent        BOOLEAN
├── sent_at        TIMESTAMP (nullable)
├── created_at     TIMESTAMP
└── updated_at     TIMESTAMP
```

All fields are mapped and available via the `IssueResponse` record DTO.

---

## DTO: IssueResponse (existing, no changes needed)

```java
public record IssueResponse(
    Long id,
    String title,
    String description,
    Status status,
    Priority priority,
    String assignee,      // nullable — show "Unassigned" in template
    boolean sent,
    Long creatorId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
)
```

**Note**: `sentAt` is not in `IssueResponse`. The detail view shows only the `sent` boolean indicator ("Sent" badge or "
Not sent"). If a "sent on [date]" display is desired in the future, `sentAt` would need to be added to this record — but
that is out of scope for this feature.

**Thymeleaf access pattern** (record accessors, NOT JavaBean properties):

- `${issue.title()}` — NOT `${issue.title}`
- `${issue.status()}` — NOT `${issue.status}`
- `${issue.sent()}` — NOT `${issue.sent}`
- etc.

---

## UI Model: Detail Fragment Template Variables

The detail fragment (`fragments/issue-detail.html`) receives a single model attribute:

| Variable | Type            | Source                          | Notes      |
|----------|-----------------|---------------------------------|------------|
| `issue`  | `IssueResponse` | `IssueService.getIssueById(id)` | All fields |

No additional model attributes are needed. Status enum values for the dropdown are rendered via `th:each` over a fixed
set, or via `Status.values()` passed in the model if dynamic iteration is preferred. The simpler approach (hardcoded
option values matching the existing card pattern) avoids adding a second model attribute.

---

## Enums (existing, no changes)

**Status** (valid values and display labels):
| Enum Value | Display Label |
|------------|---------------|
| `PREPARED` | Prepared |
| `IN_PROGRESS` | In Progress |
| `ACKNOWLEDGED` | Acknowledged |
| `RESOLVED` | Resolved |
| `WONT_DO` | Won't Do |

**Priority** (valid values and display labels):
| Enum Value | Display Label | CSS class |
|------------|---------------|-----------|
| `LOW` | Low | `priority-LOW` |
| `MEDIUM` | Medium | `priority-MEDIUM` |
| `HIGH` | High | `priority-HIGH` |
| `CRITICAL` | Critical | `priority-CRITICAL` |

CSS classes are already defined in `board/index.html` and can be reused in the detail fragment.

---

## No Migrations Required

- No new tables
- No new columns
- No new indexes
- No changes to existing columns
