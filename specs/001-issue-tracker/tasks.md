# Tasks: Issue Tracker for Property Management

**Input**: Design documents from `/specs/001-issue-tracker/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Not explicitly requested in the feature specification - tests are excluded from this task list. Test coverage can be added later if needed.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Single Spring Boot project at repository root
- Java source: `src/main/java/com/administrativetool/`
- Resources: `src/main/resources/`
- Tests: `src/test/java/com/administrativetool/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create Gradle build.gradle.kts with Spring Boot 4.0.2 and Java 25 toolchain configuration
- [X] T002 Configure PostgreSQL dependency and Spring Data JDBC in build.gradle.kts
- [X] T003 [P] Add HTMX, Thymeleaf, and htmx-spring-boot dependencies to build.gradle.kts
- [X] T004 [P] Add JavaMail, Spring Security, Lombok, and Validation dependencies to build.gradle.kts
- [X] T005 Create AdministrativeToolApplication.java main class in src/main/java/com/administrativetool/
- [X] T006 Create application.properties with database and email configuration placeholders in src/main/resources/
- [X] T007 Create schema.sql with users and issues table DDL in src/main/resources/
- [X] T008 Create data.sql with admin user seed data in src/main/resources/
- [X] T009 Create docker-compose.yml with PostgreSQL 16 service configuration at repository root
- [X] T010 Create .gitignore with application-local.properties entry at repository root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T011 [P] Create Status enum in src/main/java/com/administrativetool/domain/model/Status.java
- [X] T012 [P] Create Priority enum in src/main/java/com/administrativetool/domain/model/Priority.java
- [X] T013 [P] Create Role enum in src/main/java/com/administrativetool/security/Role.java
- [X] T014 [P] Create User entity in src/main/java/com/administrativetool/domain/model/User.java
- [X] T015 [P] Create Issue entity in src/main/java/com/administrativetool/domain/model/Issue.java
- [X] T016 [P] Create UserRepository interface in src/main/java/com/administrativetool/repository/UserRepository.java
- [X] T017 [P] Create IssueRepository interface in src/main/java/com/administrativetool/repository/IssueRepository.java
- [X] T018 Create CustomUserDetailsService in src/main/java/com/administrativetool/security/CustomUserDetailsService.java
- [X] T019 Create SecurityConfig with BCrypt and form login in src/main/java/com/administrativetool/config/SecurityConfig.java
- [X] T020 [P] Create GlobalExceptionHandler in src/main/java/com/administrativetool/exception/GlobalExceptionHandler.java
- [X] T021 [P] Create ResourceNotFoundException in src/main/java/com/administrativetool/exception/ResourceNotFoundException.java
- [X] T022 Create WebConfig for HTMX and view controllers in src/main/java/com/administrativetool/config/WebConfig.java
- [X] T023 Create EmailConfig with JavaMailSender bean in src/main/java/com/administrativetool/config/EmailConfig.java
- [X] T024 Create AsyncConfig with thread pool executor in src/main/java/com/administrativetool/config/AsyncConfig.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 6 - User Authentication (Priority: P1) 🎯 FOUNDATIONAL

**Goal**: Enable users to register accounts and login securely with role-based access control

**Independent Test**: Register a new user account, login with credentials, verify redirect to board page, and confirm session expires after 30 minutes

**Why First**: Authentication is a prerequisite for all other functionality - all operations require authenticated users

### Implementation for User Story 6

- [X] T025 [P] [US6] Create UserService in src/main/java/com/administrativetool/service/UserService.java
- [X] T026 [P] [US6] Create login.html Thymeleaf template in src/main/resources/templates/auth/login.html
- [X] T027 [P] [US6] Create register.html Thymeleaf template in src/main/resources/templates/auth/register.html
- [X] T028 [US6] Create AuthController with login and register view endpoints in src/main/java/com/administrativetool/controller/AuthController.java
- [X] T029 [US6] Create UserController with register POST endpoint in src/main/java/com/administrativetool/controller/UserController.java
- [X] T030 [US6] Implement session timeout configuration (30 minutes) in application.properties
- [X] T031 [US6] Implement account lockout after 5 failed login attempts in CustomUserDetailsService
- [X] T032 [US6] Add username uniqueness validation in UserService

**Checkpoint**: At this point, users can register, login, and access the system with proper authentication

---

## Phase 4: User Story 1 - Create New Issue (Priority: P1) 🎯 MVP

**Goal**: Enable logged-in residents to create issues about building or street problems

**Independent Test**: Login as a user, create an issue with title and description, verify it appears in the board with status "PREPARED"

### Implementation for User Story 1

- [X] T033 [P] [US1] Create IssueCreateRequest DTO in src/main/java/com/administrativetool/domain/dto/IssueCreateRequest.java
- [X] T034 [P] [US1] Create IssueUpdateRequest DTO in src/main/java/com/administrativetool/domain/dto/IssueUpdateRequest.java
- [X] T035 [US1] Create IssueService with create issue method in src/main/java/com/administrativetool/service/IssueService.java
- [X] T036 [US1] Create IssueController with POST /api/issues endpoint in src/main/java/com/administrativetool/controller/IssueController.java
- [X] T037 [US1] Add GET /api/issues endpoint to IssueController for listing all issues
- [X] T038 [US1] Add GET /api/issues/{id} endpoint to IssueController for single issue retrieval
- [X] T039 [US1] Implement title and description validation (@NotNull, @Size) in IssueCreateRequest
- [X] T040 [US1] Create form.html Thymeleaf template for issue creation in src/main/resources/templates/issues/form.html
- [X] T041 [US1] Add form submission with HTMX in form.html template
- [X] T042 [US1] Configure authentication check to redirect unauthenticated users to login in SecurityConfig

**Checkpoint**: At this point, User Story 1 should be fully functional - users can create issues and see them persisted

---

## Phase 5: User Story 3 - View Issues in Board Layout (Priority: P2)

**Goal**: Display all issues in a Kanban board organized by status columns

**Independent Test**: Login as a user, view the board page, verify issues are displayed in appropriate columns (PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO)

### Implementation for User Story 3

- [ ] T043 [P] [US3] Create main.html base layout template in src/main/resources/templates/layout/main.html
- [X] T044 [P] [US3] Create index.html board view template in src/main/resources/templates/board/index.html
- [X] T045 [P] [US3] Create issue-card.html fragment in src/main/resources/templates/fragments/issue-card.html
- [X] T046 [P] [US3] Create column.html fragment in src/main/resources/templates/fragments/column.html
- [X] T047 [US3] Create BoardController with GET /board endpoint in src/main/java/com/administrativetool/controller/BoardController.java
- [X] T048 [US3] Create FragmentController for HTMX fragments in src/main/java/com/administrativetool/controller/FragmentController.java
- [X] T049 [US3] Add GET /fragments/issues/{id}/card endpoint to FragmentController
- [X] T050 [US3] Add GET /fragments/board/column endpoint to FragmentController with status parameter
- [X] T051 [US3] Implement findByStatusOrderByCreatedAtDesc method in IssueRepository
- [X] T052 [US3] Add HTMX.org 2.0.x webjar dependency to build.gradle.kts
- [X] T053 [US3] Configure HTMX static resource serving in WebConfig
- [X] T054 [US3] Add CSS styling for board columns and issue cards in src/main/resources/static/css/board.css
- [X] T055 [US3] Implement auto-refresh on issue creation using HTMX hx-trigger in board/index.html

**Checkpoint**: At this point, User Stories 1 AND 3 should both work - users can create issues and see them on the board

---

## Phase 6: User Story 2 - Send Issue to Administrative Company (Priority: P1) 🎯 MVP

**Goal**: Enable administrators to send issues via email to the administrative company and transition status to IN_PROGRESS

**Independent Test**: Login as administrator, click "Send" button on a PREPARED issue, verify email is sent and issue status changes to IN_PROGRESS

### Implementation for User Story 2

- [X] T056 [P] [US2] Create EmailSendResponse DTO in src/main/java/com/administrativetool/domain/dto/EmailSendResponse.java
- [X] T057 [P] [US2] Create EmailRequest DTO in src/main/java/com/administrativetool/domain/dto/EmailRequest.java
- [X] T058 [US2] Create EmailService with sendIssueEmail method in src/main/java/com/administrativetool/service/EmailService.java
- [X] T059 [US2] Implement async email sending with @Async annotation in EmailService
- [X] T060 [US2] Create IssueEmailService with idempotent send logic in src/main/java/com/administrativetool/service/IssueEmailService.java
- [X] T061 [US2] Implement status check before sending (prevent duplicates) in IssueEmailService
- [X] T062 [US2] Add POST /api/issues/{id}/send endpoint to IssueController
- [X] T063 [US2] Implement ADMINISTRATOR role check for send endpoint using @PreAuthorize in IssueController
- [X] T064 [US2] Update issue status to IN_PROGRESS after successful email send in IssueEmailService
- [X] T065 [US2] Set is_sent flag and sent_at timestamp in IssueEmailService
- [X] T066 [US2] Add "Send" button with HTMX hx-post in issue-card.html fragment (visible only to admins)
- [X] T067 [US2] Implement retry logic for failed email sends in EmailService
- [X] T068 [US2] Add SMTP timeout configuration in application.properties
- [X] T069 [US2] Add admin company email environment variable in application.properties
- [X] T070 [US2] Create email template with issue title, description, and priority in EmailService
- [ ] T071 [US2] Add toast notification component for email send success/failure feedback using HTMX HX-Trigger headers

**Checkpoint**: At this point, administrators can send issues to the administrative company via email

---

## Phase 7: User Story 4 - Update Issue Status (Priority: P2)

**Goal**: Enable administrators to manually change issue status as it progresses through the workflow

**Independent Test**: Login as administrator, change an issue's status from IN_PROGRESS to ACKNOWLEDGED, verify it moves to the appropriate column

### Implementation for User Story 4

- [X] T072 [US4] Add POST /api/issues/{id}/status endpoint to IssueController
- [X] T073 [US4] Implement status update method in IssueService with validation
- [X] T074 [US4] Add status transition validation (enforce valid transitions) in IssueService
- [X] T075 [US4] Implement ADMINISTRATOR role check for status update using @PreAuthorize in IssueController
- [X] T076 [US4] Add status dropdown with HTMX hx-post in issue-card.html fragment (visible only to admins)
- [X] T077 [US4] Implement HTMX swap to update issue card after status change in issue-card.html
- [X] T078 [US4] Add timestamp update (updated_at) on status change in IssueService
- [X] T079 [US4] Implement WON'T_DO override (from any status) in status transition validation

**Checkpoint**: At this point, administrators can manage issue lifecycle through status updates

---

## Phase 8: User Story 5 - Search and Filter Issues (Priority: P3)

**Goal**: Enable users to search for issues by keywords and filter by status

**Independent Test**: Login as a user, enter "street" in search box, verify only matching issues are displayed; filter by "RESOLVED" status and verify results

### Implementation for User Story 5

- [ ] T080 [US5] Add GET /api/issues/search endpoint to IssueController with keyword and status parameters
- [ ] T081 [US5] Implement searchByKeyword method in IssueRepository using ILIKE query
- [ ] T082 [US5] Add search input field with HTMX hx-get in board/index.html
- [ ] T083 [US5] Add status filter dropdown with HTMX hx-get in board/index.html
- [ ] T084 [US5] Implement HTMX target swap to update board results in board/index.html
- [ ] T085 [US5] Create search results fragment template in src/main/resources/templates/fragments/search-results.html
- [ ] T086 [US5] Add debounce to search input using HTMX hx-trigger="keyup changed delay:500ms" in board/index.html

**Checkpoint**: All user stories should now be independently functional - complete feature set delivered

---

## Phase 9: Additional Features & API Endpoints

**Purpose**: Complete remaining API endpoints and user management features

- [ ] T087 [P] Add PUT /api/issues/{id} endpoint to IssueController for issue updates
- [ ] T088 [P] Add DELETE /api/issues/{id} endpoint to IssueController with admin-only access
- [ ] T089 [P] Add GET /api/users/me endpoint to UserController for current user details
- [ ] T090 [P] Add GET /api/users endpoint to UserController with admin-only access
- [ ] T091 [P] Create detail.html template for issue details in src/main/resources/templates/issues/detail.html
- [ ] T092 [P] Create list.html template for issue list view in src/main/resources/templates/issues/list.html
- [ ] T093 Implement update issue authorization (user can update own issues, admin can update any) in IssueService
- [ ] T094 Implement delete issue authorization (admin only) in IssueController
- [ ] T095 Add GET /api/users/{id} endpoint to UserController with authentication check

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T096 [P] Add comprehensive error messages for validation failures in GlobalExceptionHandler
- [ ] T097 [P] Implement proper logging with SLF4J in all service classes
- [ ] T098 [P] Add security headers configuration in SecurityConfig
- [ ] T099 [P] Create application-test.properties for test environment in src/test/resources/
- [ ] T100 Implement CSRF token handling for HTMX requests in WebConfig
- [ ] T101 Add visual feedback for successful/failed operations using HTMX response headers
- [ ] T102 Implement email configuration validation on startup in EmailConfig
- [ ] T103 Add database migration numbering and versioning in schema.sql
- [ ] T104 Create README.md with quickstart instructions at repository root
- [ ] T105 Verify all endpoints match contracts/api.md specification
- [ ] T106 Run quickstart.md validation steps to ensure setup works
- [ ] T107 Performance optimization: add database indexes per data-model.md
- [ ] T108 Security hardening: verify BCrypt strength is 12 rounds in SecurityConfig
- [ ] T109 Add HTTP response status code consistency per contracts/api.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 6 - Authentication (Phase 3)**: Depends on Foundational - BLOCKS all other user stories (all features require auth)
- **User Story 1 - Create Issue (Phase 4)**: Depends on Authentication
- **User Story 3 - Board View (Phase 5)**: Depends on Authentication and US1 (needs issues to display)
- **User Story 2 - Send Email (Phase 6)**: Depends on Authentication and US1 (needs issues to send)
- **User Story 4 - Update Status (Phase 7)**: Depends on Authentication and US1 (needs issues to update)
- **User Story 5 - Search/Filter (Phase 8)**: Depends on Authentication and US3 (enhances board view)
- **Additional Features (Phase 9)**: Can start after Authentication - independent of user stories
- **Polish (Phase 10)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 6 (Authentication - P1)**: MUST complete first - blocks all other stories
- **User Story 1 (Create Issue - P1)**: Can start after US6 - No dependencies on other stories
- **User Story 2 (Send Email - P1)**: Can start after US1 - Needs issues to send
- **User Story 3 (Board View - P2)**: Can start after US1 - Needs issues to display
- **User Story 4 (Update Status - P2)**: Can start after US1 - Needs issues to update
- **User Story 5 (Search/Filter - P3)**: Can start after US3 - Enhances board view

### Within Each User Story

- DTOs and entities before services
- Services before controllers
- Controllers before templates
- Templates before HTMX integration
- Core implementation before authorization checks

### Parallel Opportunities

- **Setup (Phase 1)**: T003, T004, T006-T010 can run in parallel
- **Foundational (Phase 2)**: T011-T013, T014-T015, T016-T017, T020-T021, T023-T024 can run in parallel
- **User Story 6**: T025-T027 can run in parallel
- **User Story 1**: T033-T034, T039-T040 can run in parallel
- **User Story 3**: T043-T046, T052-T054 can run in parallel
- **User Story 2**: T056-T057 can run in parallel
- **Phase 9**: T087-T092 can run in parallel
- **Polish (Phase 10)**: T096-T099 can run in parallel

---

## Parallel Example: Foundational Phase

```bash
# Launch all enums together:
Task: "Create Status enum in src/main/java/com/administrativetool/domain/model/Status.java"
Task: "Create Priority enum in src/main/java/com/administrativetool/domain/model/Priority.java"
Task: "Create Role enum in src/main/java/com/administrativetool/security/Role.java"

# Launch both entities together:
Task: "Create User entity in src/main/java/com/administrativetool/domain/model/User.java"
Task: "Create Issue entity in src/main/java/com/administrativetool/domain/model/Issue.java"

# Launch both repositories together:
Task: "Create UserRepository interface in src/main/java/com/administrativetool/repository/UserRepository.java"
Task: "Create IssueRepository interface in src/main/java/com/administrativetool/repository/IssueRepository.java"
```

---

## Implementation Strategy

### MVP First (Recommended for Initial Release)

**MVP = Authentication + Create Issues + Send to Admin Company**

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 6 (Authentication) - BLOCKS all features
4. Complete Phase 4: User Story 1 (Create Issue)
5. Complete Phase 5: User Story 3 (Board View) - Enables visualization
6. Complete Phase 6: User Story 2 (Send Email) - Core workflow complete
7. **STOP and VALIDATE**: Test complete workflow - register → login → create issue → view board → send email
8. Deploy/demo MVP

**Why This Order**: Authentication is foundational, then create issues (data entry), then visualize them (board), finally enable the key workflow (send email). This delivers the core value proposition.

### Incremental Delivery (Full Feature Set)

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 6 (Authentication) → Test independently → REQUIRED for all other features
3. Add User Story 1 (Create Issue) → Test independently → Basic functionality working
4. Add User Story 3 (Board View) → Test independently → Visualization complete
5. Add User Story 2 (Send Email) → Test independently → Core workflow complete → **Deploy MVP**
6. Add User Story 4 (Update Status) → Test independently → Full workflow management
7. Add User Story 5 (Search/Filter) → Test independently → Enhanced usability
8. Add Phase 9 (Additional Features) → Complete API surface
9. Add Phase 10 (Polish) → Production ready

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Team completes User Story 6 (Authentication) together - BLOCKS all other work
3. Once Authentication is done:
   - Developer A: User Story 1 (Create Issue)
   - Developer B: User Story 3 (Board View - parallel to US1)
   - Developer C: User Story 2 (Send Email - depends on US1 completing)
4. After core stories:
   - Developer A: User Story 4 (Update Status)
   - Developer B: User Story 5 (Search/Filter)
   - Developer C: Phase 9 (Additional Features)

---

## Task Summary

**Total Tasks**: 109
**Phases**: 10

### Tasks by User Story

- **Setup (Phase 1)**: 10 tasks
- **Foundational (Phase 2)**: 14 tasks (BLOCKS all user stories)
- **US6 - Authentication (P1)**: 8 tasks (BLOCKS all features)
- **US1 - Create Issue (P1)**: 10 tasks
- **US3 - Board View (P2)**: 13 tasks
- **US2 - Send Email (P1)**: 16 tasks
- **US4 - Update Status (P2)**: 8 tasks
- **US5 - Search/Filter (P3)**: 7 tasks
- **Additional Features (Phase 9)**: 9 tasks
- **Polish (Phase 10)**: 14 tasks

### Parallel Opportunities Identified

- **Setup Phase**: 7 parallel tasks (T003-T004, T006-T010)
- **Foundational Phase**: 12 parallel tasks (T011-T013, T014-T015, T016-T017, T020-T021, T023-T024)
- **User Story 6**: 3 parallel tasks (T025-T027)
- **User Story 1**: 4 parallel tasks (T033-T034, T039-T040)
- **User Story 3**: 7 parallel tasks (T043-T046, T052-T054)
- **User Story 2**: 2 parallel tasks (T056-T057)
- **Phase 9**: 6 parallel tasks (T087-T092)
- **Phase 10**: 4 parallel tasks (T096-T099)

### Independent Test Criteria

- **US6 (Authentication)**: Register new user → Login → Verify redirect to board → Wait 30 min → Verify session expires
- **US1 (Create Issue)**: Login → Create issue with title/description → Verify appears in database with PREPARED status
- **US3 (Board View)**: Login → Navigate to /board → Verify issues displayed in correct columns → Create new issue → Verify auto-refresh
- **US2 (Send Email)**: Login as admin → Click Send on PREPARED issue → Verify email sent → Verify status changes to IN_PROGRESS
- **US4 (Update Status)**: Login as admin → Change issue from IN_PROGRESS to ACKNOWLEDGED → Verify moves to correct column
- **US5 (Search/Filter)**: Login → Search "street" → Verify only matching issues → Filter by RESOLVED → Verify only resolved issues

### Suggested MVP Scope

**Minimum Viable Product** = Authentication + Create + View + Send

- Phase 1: Setup (10 tasks)
- Phase 2: Foundational (14 tasks)
- Phase 3: US6 - Authentication (8 tasks)
- Phase 4: US1 - Create Issue (10 tasks)
- Phase 5: US3 - Board View (13 tasks)
- Phase 6: US2 - Send Email (16 tasks)

**Total MVP Tasks**: 71 out of 109 tasks (65%)

This delivers the complete core workflow: users can register, login, create issues, view them on a board, and administrators can send them to the administrative company via email.

---

## Format Validation

✅ ALL tasks follow the required checklist format:
- Checkbox: `- [ ]`
- Task ID: Sequential numbers (T001-T109)
- [P] marker: Present on parallelizable tasks only
- [Story] label: Present on user story phase tasks (US1-US6)
- Description: Clear action with exact file path
- No story label on Setup, Foundational, or Polish phases

✅ Tasks are organized by user story for independent implementation and testing

✅ Each user story phase includes independent test criteria

✅ Dependency graph shows clear story completion order

✅ Parallel execution examples provided per phase

---

## Notes

- Tests are NOT included in this task list (not explicitly requested in spec)
- Test coverage can be added later following AGENTS.md guidelines (JUnit 5 + AssertJ)
- All file paths follow the project structure from plan.md
- Technology stack matches research.md decisions: Java 25, Spring Boot 4.0.2, HTMX 2.0.x, PostgreSQL 16+
- Security follows AGENTS.md: BCrypt, form auth, role-based access, no plaintext passwords
- Email configuration uses environment variables per security best practices
- HTMX chosen specifically for 4GB RAM / 8GB disk constraint (14KB vs 500KB+ for React/Vue)
