# Quickstart: Openable Issue Detail View

**Change**: `003-openable-issue`  
**Date**: 2026-02-22

## Prerequisites

- Docker and Docker Compose installed
- No local JDK needed — Gradle toolchain downloads Java 25 automatically

## Start the Application

```bash
# Start PostgreSQL
docker compose up -d

# Run the application (auto-downloads Java 25 on first run)
./gradlew bootRun
```

Open `http://localhost:8080` and log in with:

- Username: `admin`
- Password: `admin`

## Verify the Feature Works

1. **Open the board** — you should see the Kanban columns.
2. **Create an issue** if none exist: click `+ New Issue`, fill in the form, submit.
3. **Click any issue card** — the detail overlay should appear with all fields visible.
4. **Dismiss the overlay** by pressing Escape, clicking outside the panel, or clicking the × button.
5. **Admin actions** (as admin):
    - Change status via the dropdown in the detail view.
    - For a PREPARED unsent issue: click "Send to Administration" — status should change to IN_PROGRESS and the sent
      badge should appear.

## Run Tests

```bash
./gradlew test
```

To run only the new controller test for this feature:

```bash
./gradlew test --tests FragmentControllerTest
```

## Key Files for This Feature

| File                                                            | Role                                              |
|-----------------------------------------------------------------|---------------------------------------------------|
| `src/main/java/.../controller/view/FragmentController.java`     | Add `GET /fragments/issues/{id}/detail`           |
| `src/main/java/.../controller/view/IssueViewController.java`    | Add `/send/detail` and `/status/detail` endpoints |
| `src/main/resources/templates/fragments/issue-detail.html`      | New overlay fragment (create this file)           |
| `src/main/resources/templates/fragments/issue-card.html`        | Add `hx-get` click handler to `.issue-card`       |
| `src/main/resources/templates/board/index.html`                 | Add `#issue-modal` container + JS dismiss logic   |
| `src/test/java/.../controller/view/FragmentControllerTest.java` | New test for detail endpoint                      |

## Development Tips

- **HTMX debugging**: Add `htmx.logAll()` in the browser console to trace requests/swaps.
- **Template hot-reload**: With `spring-boot-devtools` on the classpath, Thymeleaf templates reload without restarting (
  if devtools is configured).
- **CSS**: All styles live inline in `board/index.html` `<style>` block. Add modal styles there.
- **Security**: The `sec:authorize="hasRole('ADMIN')"` attribute controls what admin users see in templates. Test both
  roles.
- **Record accessor syntax**: All `IssueResponse` fields are accessed as method calls in Thymeleaf — `${issue.title()}`
  not `${issue.title}`.
