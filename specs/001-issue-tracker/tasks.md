---

description: "Task list for Issue Tracker implementation"

---

# Tasks: Issue Tracker for Property Management

**Input**: Design documents from `/specs/001-issue-tracker/`  
**Branch**: `001-issue-tracker`  
**Date**: 2026-02-07

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

**Format**: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Project initialization and basic structure

**Goal**: Establish project structure with Spring Boot 4.0.2, Java 25, and all required dependencies

- [X] T001 Initialize Spring Boot 4.0.2 project structure in `src/main/java/com/administrativetool/` and `src/test/java/com/administrativetool/`
- [X] T002 [P] Configure Gradle 9.x build with Java 25 toolchain in `build.gradle` (dependencies: Spring Boot Web, Data JDBC, Security, Mail, Thymeleaf, Validation, Lombok, PostgreSQL)
- [X] T003 [P] Create main application class `AdministrativeToolApplication.java` in `src/main/java/com/administrativetool/`
- [X] T004 Create `application.properties` in `src/main/resources/` with database, email, and security configuration templates
- [X] T005 [P] Create `.gitignore` entries for `application-local.properties`, build artifacts, and IDE files
- [X] T006 Setup Docker Compose configuration in `docker-compose.yml` for PostgreSQL development database

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database & Shared Enums

- [X] T007 Create `Status.java` enum in `src/main/java/com/administrativetool/domain/model/` with values: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO
- [X] T008 [P] Create `Priority.java` enum in `src/main/java/com/administrativetool/domain/model/` with values: LOW, MEDIUM, HIGH, CRITICAL
- [X] T009 Create `Role.java` enum in `src/main/java/com/administrativetool/security/` with values: USER, ADMINISTRATOR
- [X] T010 Create `User.java` entity in `src/main/java/com/administrativetool/domain/model/` with fields: id, username, password, role, createdAt, updatedAt
- [X] T011 Create `Issue.java` entity in `src/main/java/com/administrativetool/domain/model/` with fields: id, creatorId, title, description, status, priority, assignee, isSent, sentAt, createdAt, updatedAt
- [X] T012 Create `UserRepository.java` in `src/main/java/com/administrativetool/repository/` extending CrudRepository with methods: findByUsername, existsByUsername
- [X] T013 [P] Create `IssueRepository.java` in `src/main/java/com/administrativetool/repository/` extending CrudRepository with methods: findByStatus, findByCreatorId, searchByKeyword
- [X] T014 Create database schema SQL in `src/main/resources/schema.sql` with users and issues tables, indexes, and triggers

### Security Infrastructure

- [X] T015 Create `CustomUserDetailsService.java` in `src/main/java/com/administrativetool/security/` implementing UserDetailsService
- [X] T016 Create `SecurityConfig.java` in `src/main/java/com/administrativetool/config/` with SecurityFilterChain, BCryptPasswordEncoder, form login, and role-based access
- [X] T017 [P] Create `WebConfig.java` in `src/main/java/com/administrativetool/config/` for CORS and static resource handling

### Error Handling

- [X] T018 Create `ResourceNotFoundException.java` in `src/main/java/com/administrativetool/exception/`
- [X] T019 Create `GlobalExceptionHandler.java` in `src/main/java/com/administrativetool/exception/` with @ControllerAdvice for REST API error responses

### Email Infrastructure

- [X] T020 Create `AsyncConfig.java` in `src/main/java/com/administrativetool/config/` with ThreadPoolTaskExecutor for async email sending
- [X] T021 Create `EmailConfig.java` in `src/main/java/com/administrativetool/config/` with JavaMailSender bean and timeout configuration

**Checkpoint**: Foundation ready - database schema, security, and infrastructure are in place. User story implementation can now begin.

---

## Phase 3: User Story 6 - User Authentication (Priority: P1) 🎯 MVP

**Goal**: Enable user registration and login so authenticated users can access the system

**Independent Test**: Can register a new user at `/register`, login at `/login`, and access protected pages. Session expires after 30 minutes of inactivity.

### DTOs

- [X] T022 [P] Create `UserRegistrationRequest.java` in `src/main/java/com/administrativetool/domain/dto/` with username and password fields, validation annotations
- [X] T023 [P] Create `UserResponse.java` in `src/main/java/com/administrativetool/domain/dto/` for user data serialization

### Service Layer

- [X] T024 Create `UserService.java` in `src/main/java/com/administrativetool/service/` with registerUser, findByUsername, and existsByUsername methods

### Controller

- [X] T025 Create `AuthController.java` in `src/main/java/com/administrativetool/controller/` with endpoints: GET /login, GET /register, POST /api/users/register
- [X] T026 Create login form template `login.html` in `src/main/resources/templates/auth/`
- [X] T027 [P] Create registration form template `register.html` in `src/main/resources/templates/auth/`

### Validation

- [X] T028 Add username uniqueness validation in `UserRegistrationRequest` with custom validator
- [X] T029 Add password strength validation (min 8 characters) in `UserRegistrationRequest`

**Checkpoint**: Users can now register and login. All subsequent user stories depend on this.

---

## Phase 4: User Story 1 - Create New Issue (Priority: P1) 🎯 MVP

**Goal**: Allow logged-in users to create issues with title and description

**Independent Test**: Login as user, create issue with title "Test" and description "Description", verify it appears in database with status PREPARED

### DTOs

- [X] T030 [P] Create `IssueCreateRequest.java` in `src/main/java/com/administrativetool/domain/dto/` with title, description, priority fields and validation
- [X] T031 [P] Create `IssueResponse.java` in `src/main/java/com/administrativetool/domain/dto/` for issue data serialization

### Service Layer

- [X] T032 Create `IssueService.java` in `src/main/java/com/administrativetool/service/` with createIssue method that sets status to PREPARED and links to current user

### Controller

- [X] T033 Create `BoardController.java` in `src/main/java/com/administrativetool/controller/` with endpoints for board view and issue creation
- [X] T034 Create issue form template `form.html` in `src/main/resources/templates/issues/`

### UI Components

- [X] T035 Create board view template `index.html` in `src/main/resources/templates/board/` with "New Issue" button

**Checkpoint**: Users can create issues. This is the core functionality - without it, system has no value.

---

## Phase 5: User Story 3 - View Issues in Board Layout (Priority: P2)

**Goal**: Display all issues in kanban-style columns organized by status

**Independent Test**: Login, view /board, see issues organized in columns (PREPARED, IN_PROGRESS, etc.). Creating new issue updates board without full page refresh.

### Board View

- [X] T036 Create `BoardController.java` in `src/main/java/com/administrativetool/controller/` with GET /board endpoint
- [X] T037 Create board view template `index.html` in `src/main/resources/templates/board/` with columns for each status
- [X] T038 [P] Create issue card fragment `issue-card.html` in `src/main/resources/templates/fragments/` showing title, priority badge, and action buttons
- [X] T039 [P] Create column fragment `column.html` in `src/main/resources/templates/fragments/` for each status column

### HTMX Integration

- [X] T040 Create `FragmentController.java` in `src/main/java/com/administrativetool/controller/` with HTMX endpoints: GET /fragments/board/column, GET /fragments/issues/{id}/card
- [X] T041 Add HTMX library inclusion via CDN in board template
- [X] T042 Add HTMX polling for real-time board updates (polling every 5s)

### Service Layer

- [X] T043 [P] Add `getIssuesByStatus` method to `IssueService.java` for filtering issues by status
- [X] T044 [P] Add `getAllIssues` method to `IssueService.java` for retrieving all issues ordered by creation date

**Checkpoint**: Board view is functional. Users can see all issues organized by status. Issues appear in real-time (or near real-time with polling).

---

## Phase 6: User Story 2 - Send Issue to Administrative Company (Priority: P1)

**Goal**: Enable administrators to send issues via email and automatically update status

**Independent Test**: Login as admin, click "Send" on PREPARED issue, verify email is sent to configured address, issue status changes to IN_PROGRESS, and duplicate sends are prevented.

### DTOs

- [ ] T045 [P] Create `EmailSendResponse.java` in `src/main/java/com/administrativetool/domain/dto/` with issueId, status, message fields

### Email Service

- [ ] T046 Create `EmailService.java` in `src/main/java/com/administrativetool/service/` with @Async sendIssueEmail method using JavaMailSender
- [ ] T047 [P] Create email template builder in `EmailService.java` for HTML email content with issue details
- [ ] T048 Add retry logic with @Retryable for transient email failures in `EmailService.java`

### Idempotent Email Sending

- [ ] T049 Create `IssueEmailService.java` in `src/main/java/com/administrativetool/service/` with idempotent sendIssueToAdmin method that checks isSent flag, updates status to IN_PROGRESS, and triggers async email
- [ ] T050 Add TransactionSynchronization in `IssueEmailService.java` to send email only after database commit succeeds

### Controller

- [ ] T051 Add POST /api/issues/{id}/send endpoint to `IssueController.java` with @PreAuthorize("hasRole('ADMIN')")
- [ ] T052 Add "Send" button to issue card fragment with HTMX attributes (only visible to ADMIN role)
- [ ] T053 Add success/error toast notifications using HTMX response headers (HX-Trigger)

### Configuration

- [ ] T054 Add admin company email property to `application.properties`: `admin.company.email=${ADMIN_COMPANY_EMAIL}`

**Checkpoint**: Critical workflow complete. Admins can send issues to administrative company via email.

---

## Phase 7: User Story 4 - Update Issue Status (Priority: P2)

**Goal**: Allow administrators to manually update issue status as it progresses

**Independent Test**: Login as admin, change issue status from IN_PROGRESS to ACKNOWLEDGED, verify issue moves to correct column on board.

### DTOs

- [ ] T055 [P] Create `StatusUpdateRequest.java` in `src/main/java/com/administrativetool/domain/dto/` with status field and validation

### Service Layer

- [ ] T056 Add `updateStatus` method to `IssueService.java` with status transition validation
- [ ] T057 Add `canTransition` helper method in `IssueService.java` to validate allowed status changes

### Controller

- [ ] T058 Add POST /api/issues/{id}/status endpoint to `IssueController.java` with @PreAuthorize("hasRole('ADMIN')")
- [ ] T059 Create status dropdown component in `src/main/resources/templates/fragments/` for admin users
- [ ] T060 Update board view to show status change controls (dropdown) only for ADMIN role

### HTMX Integration

- [ ] T061 Add HTMX endpoint in `FragmentController.java` for status update that returns updated issue card
- [ ] T062 Implement out-of-band swap (HX-Trigger with HX-Swap-OOB) to move issue card between columns on status change

**Checkpoint**: Status workflow is complete. Admins can track issue progress through all stages.

---

## Phase 8: User Story 5 - Search and Filter Issues (Priority: P3)

**Goal**: Enable users to search issues by keyword and filter by status

**Independent Test**: Login, search for "street" keyword, see only matching issues. Filter by RESOLVED status, see only resolved issues.

### Service Layer

- [ ] T063 Add `searchIssues` method to `IssueService.java` with keyword search in title and description
- [ ] T064 [P] Add `filterByStatus` method to `IssueService.java` for status filtering

### Controller

- [ ] T065 Add GET /api/issues/search endpoint to `IssueController.java` with keyword and status query parameters
- [ ] T066 Create search bar component in `src/main/resources/templates/fragments/` with HTMX integration
- [ ] T067 [P] Create filter dropdown component in `src/main/resources/templates/fragments/` for status filtering

### UI Components

- [ ] T068 Integrate search bar into board view template `src/main/resources/templates/board/index.html`
- [ ] T069 Add HTMX debounce for search input to prevent excessive requests
- [ ] T070 Implement clear search/filter functionality

**Checkpoint**: Search and filter complete. Users can find issues efficiently even with large volume.

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

### Testing

- [ ] T071 [P] Create `IssueControllerTest.java` in `src/test/java/com/administrativetool/controller/` with unit tests for CRUD endpoints
- [ ] T072 [P] Create `IssueControllerWebTest.java` in `src/test/java/com/administrativetool/controller/` with @WebMvcTest for web layer
- [ ] T073 Create `IssueServiceTest.java` in `src/test/java/com/administrativetool/service/` with business logic tests
- [ ] T074 [P] Create `EmailIntegrationTest.java` in `src/test/java/com/administrativetool/integration/` using GreenMail for email testing
- [ ] T075 Create `application-test.properties` in `src/test/resources/` with test configuration

### Security & Validation

- [ ] T076 [P] Add @PreAuthorize annotations to all admin-only endpoints in controllers
- [ ] T077 Implement account lockout after 5 failed login attempts in `CustomUserDetailsService.java`
- [ ] T078 Add CSRF protection configuration in `SecurityConfig.java` for form submissions
- [ ] T079 Add input sanitization for issue title and description to prevent XSS

### Performance & UX

- [ ] T080 [P] Add database indexes for frequently queried columns (status, creator_id, created_at)
- [ ] T081 Configure HTTP caching headers for static resources in `WebConfig.java`
- [ ] T082 Add loading indicators (spinner) for HTMX requests in base layout template
- [ ] T083 [P] Add error toast notifications using HTMX response headers
- [ ] T084 Implement mobile-responsive CSS for board view

### Documentation

- [ ] T085 [P] Add Javadoc comments to all public methods in service layer
- [ ] T086 Create README.md in project root with setup instructions
- [ ] T087 Validate quickstart.md steps work correctly

### Configuration

- [ ] T088 Create `application-prod.properties` template for production deployment
- [ ] T089 Add logging configuration in `application.properties` with appropriate levels
- [ ] T090 [P] Configure actuator endpoints for health checks

**Checkpoint**: All polish items complete. Application is production-ready.

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup
    ↓
Phase 2: Foundational (BLOCKS all user stories)
    ↓
Phase 3: US6 Authentication (P1) - Can start after Phase 2
    ↓
Phase 4: US1 Create Issue (P1) - Depends on US6
    ↓
Phase 5: US3 Board View (P2) - Depends on US1
    ↓
Phase 6: US2 Send Email (P1) - Depends on US1 and US3
    ↓
Phase 7: US4 Update Status (P2) - Depends on US2 and US3
    ↓
Phase 8: US5 Search/Filter (P3) - Depends on US3
    ↓
Phase 9: Polish - Depends on all stories
```

### User Story Dependencies

| Story | Priority | Depends On | Can Start After |
|-------|----------|------------|-----------------|
| US6 Authentication | P1 | Phase 2 | Phase 2 complete |
| US1 Create Issue | P1 | US6 | US6 complete |
| US3 Board View | P2 | US1 | US1 complete |
| US2 Send Email | P1 | US1, US3 | US3 complete |
| US4 Update Status | P2 | US2, US3 | US2 complete |
| US5 Search/Filter | P3 | US3 | US3 complete |

### Parallel Opportunities

**Within Phase 2 (Foundational)**:
- T007, T008, T009 (enums) can run in parallel
- T010, T011 (entities) can run in parallel
- T012, T013 (repositories) can run in parallel
- T015, T017, T018, T019, T020, T021 (infrastructure) can run in parallel after entities

**Within User Stories**:
- All DTO creation tasks marked [P] can run in parallel
- All template creation tasks marked [P] can run in parallel
- All service method additions marked [P] can run in parallel

**Across User Stories** (with multiple developers):
- US1 and US6 can be worked on in parallel after Phase 2
- US3 and US2 can be worked on in parallel after US1
- US4 and US5 can be worked on in parallel after their dependencies

### Critical Path

The critical path for MVP (minimum viable product) is:
1. Phase 1: Setup (6 tasks)
2. Phase 2: Foundational (15 tasks) - **BLOCKING**
3. Phase 3: US6 Authentication (8 tasks) - **BLOCKING**
4. Phase 4: US1 Create Issue (5 tasks)

**Total MVP tasks**: 34 tasks

---

## Implementation Strategy

### MVP First (Recommended)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: US6 Authentication
4. Complete Phase 4: US1 Create Issue
5. **STOP and VALIDATE**: Test user registration, login, and issue creation
6. Deploy and demo the MVP
7. Continue with remaining stories based on feedback

### Sequential Delivery (Single Developer)

Follow the phases in order:
1. Setup → Foundational → US6 → US1 → US3 → US2 → US4 → US5 → Polish
2. Each phase delivers incremental value
3. Can deploy after each phase

### Parallel Team Strategy (Multiple Developers)

With 3 developers:
- **Developer A**: Phase 1 & 2 (infrastructure)
- **Developer B**: Phase 3 (US6 Authentication) after Phase 2
- **Developer C**: Phase 4 (US1 Create Issue) after Phase 3

With 4+ developers:
- **Team 1**: Phase 1 & 2
- **Team 2**: US6 + US1 (after Phase 2)
- **Team 3**: US3 + US2 (after US1)
- **Team 4**: US4 + US5 + Polish (after their dependencies)

---

## Task Statistics

| Phase | Tasks | Parallel Tasks | Story |
|-------|-------|----------------|-------|
| Phase 1: Setup | 6 | 3 | - |
| Phase 2: Foundational | 15 | 8 | - |
| Phase 3: US6 Authentication | 8 | 3 | US6 (P1) |
| Phase 4: US1 Create Issue | 5 | 2 | US1 (P1) |
| Phase 5: US3 Board View | 9 | 4 | US3 (P2) |
| Phase 6: US2 Send Email | 10 | 4 | US2 (P1) |
| Phase 7: US4 Update Status | 8 | 2 | US4 (P2) |
| Phase 8: US5 Search/Filter | 8 | 3 | US5 (P3) |
| Phase 9: Polish | 20 | 10 | - |
| **TOTAL** | **89** | **39** | - |

---

## Notes

- All file paths use forward slashes (/) for cross-platform compatibility
- Tasks marked [P] have no dependencies on other incomplete tasks in the same phase
- Each user story is independently testable once complete
- Stop at any checkpoint to validate and deploy incrementally
- Tests are included in Phase 9 but can be written earlier if following TDD
- All code should follow AGENTS.md style guidelines (Lombok, AssertJ, no wildcards)
