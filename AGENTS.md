# AGENTS.md

This guide is for agentic coding assistants working in this repository.

## Project Overview
Spring Boot 4.0.2 application for administrative/issue tracking with Java 25, Gradle 9.3.1, PostgreSQL, and Lombok.

### Project Goal
Build a Jira-like issue tracking system for property management with the following features:
- Board view with issue columns: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO
- Each issue can be sent via email to the Administrative Company (Zarządca osiedla) with a single button
- Automatic status progression after sending email
- Secure email configuration stored via environment variables
- Role-based authentication with BCrypt password encoding
- All code and documentation in English

## Build/Test Commands
- **Build**: `./gradlew build`
- **Run**: `./gradlew bootRun`
- **Clean**: `./gradlew clean`
- **Test**: `./gradlew test` (run all tests)
- **Single test**: `./gradlew test --tests ClassName.methodName`
- **Test class**: `./gradlew test --tests ClassName`
- **Check**: `./gradlew check` (runs tests + verification tasks)
- **BootJar**: `./gradlew bootJar` (builds executable JAR)

## Code Style Guidelines

### Imports
- Order: Java stdlib, third-party (org.springframework, lombok), project-local (com.administrativetool)
- No wildcard imports except in test files
- Separate groups with blank line

### Formatting & Structure
- Use Lombok annotations (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) on model classes
- Package annotation first, then imports (separated by blank lines between groups)
- No trailing whitespace
- Use standard Java naming conventions: `CamelCase` for classes, `camelCase` for methods/fields, `UPPER_SNAKE_CASE` for constants

### Types & Naming
- Entity models in `com.administrativetool.model` package
- Use `Long` for entity IDs with `@Id` annotation
- Enums for fixed value sets (Status, Priority) - values in `UPPER_SNAKE_CASE`
- Repository interfaces extend `CrudRepository<Entity, Long>`
- Service classes use constructor injection with `@Autowired` on constructor
- Controllers use `@RestController` + `@RequestMapping("/api/resource")`
- Table mapping: `@Table("plural_table_name")` with explicit `@Column("snake_case_name")` when needed

### Error Handling
- Controllers return `ResponseEntity` for 404/not found scenarios: `ResponseEntity.notFound().build()` or `ResponseEntity.noContent().build()`
- Check for null before operations: `if (existing == null) { return ResponseEntity.notFound().build(); }`
- Repository: use `.orElse(null)` for optional results (template pattern)
- No checked exceptions in service layer

### Testing
- Use JUnit 5 (JUnit Platform) via `spring-boot-starter-test` - ONLY JUnit 5 should be used with AssertJ
- Use AssertJ for assertions - prefer AssertJ-specific methods (e.g., `.usingRecursiveComparison()`) over multiple separate `assertThat()` calls
- Test directory: `src/test/java/com/administrativetool/`
- Match source package structure under test
- Spring Boot test: `@SpringBootTest` for integration tests
- Controller tests: `@WebMvcTest` for web layer only
- No access modifiers (public/private) in test methods or test fields
- Use `var` instead of specific types for local variables in tests (e.g., `var issue = ...` NOT `Issue issue = ...`)

### Dependencies & Libraries
- Spring Boot starters: web, data-jdbc, validation, security, mail
- Database: PostgreSQL (configure in application.properties)
- Lombok: for getters/setters/constructors (use annotations, don't write boilerplate)
- Spring Data R2DBC: `org.springframework.data.relational.core.mapping.*` for entity mapping
- Security: Form-based login with BCrypt password encoding and role-based access control
- Email: Spring Boot Mail (JavaMail) for sending issues to Administrative Company

### Database Configuration
- Connection URL in `application.properties`: `jdbc:postgresql://postgres:5432/admtool`
- Init mode: `spring.sql.init.mode=always`
- Docker compose available for Postgres container

## Domain Model Conventions

### Status Enum Values
Status follows a defined workflow with these values (UPPER_SNAKE_CASE):
- **PREPARED** - Initial status for newly created issues
- **IN_PROGRESS** - Issue has been sent to Administrative Company and is being processed
- **ACKNOWLEDGED** - Administrative Company has acknowledged the issue
- **RESOLVED** - Issue has been resolved/completed
- **WON'T_DO** - Issue was rejected or will not be addressed

### Priority Enum Values
Priority levels (UPPER_SNAKE_CASE):
- **LOW** - Minor issues, can be addressed later
- **MEDIUM** - Standard priority issues
- **HIGH** - Urgent issues requiring attention
- **CRITICAL** - Emergency issues needing immediate action

### Issue Entity
Required fields:
- `id` (Long, @Id) - Primary key
- `title` (String) - Issue title
- `description` (String) - Detailed issue description
- `status` (Status) - Current status in workflow
- `priority` (Priority) - Priority level
- `assignee` (String) - User assigned to the issue
- `is_send` (boolean) - Flag indicating if email has been sent to Administrative Company

### Status Transition Rules
Valid status transitions:
1. PREPARED → IN_PROGRESS (when email is sent to Administrative Company)
2. IN_PROGRESS → ACKNOWLEDGED (when Administrative Company acknowledges)
3. ACKNOWLEDGED → RESOLVED (when issue is completed)
4. Any status → WON'T_DO (when issue is rejected)
5. RESOLVED → PREPARED (for reopening resolved issues, optional)

## Email Sending Requirements

### Email Service Configuration
- Use Spring Boot Mail (JavaMail) with SMTP configuration
- Email configuration via environment variables for security:
  - `SPRING_MAIL_HOST` - SMTP server host
  - `SPRING_MAIL_PORT` - SMTP server port (default: 587)
  - `SPRING_MAIL_USERNAME` - SMTP authentication username
  - `SPRING_MAIL_PASSWORD` - SMTP authentication password
  - `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` - Enable SMTP auth (true)
  - `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` - Enable STARTTLS (true)
  - `ADMIN_COMPANY_EMAIL` - Target email for Zarządca osiedla (Administrative Company)

### Email Content Template
Email sent to Administrative Company should include:
- Issue title
- Issue description
- Priority level
- Current status
- Assignee
- Link to issue (if applicable)

### Send Endpoint
- Endpoint: `POST /api/issues/{id}/send`
- Behavior:
  1. Retrieves issue by ID
  2. Sends email to ADMIN_COMPANY_EMAIL
  3. Updates issue status from PREPARED to IN_PROGRESS
  4. Sets `is_send = true` on issue
  5. Returns 200 OK on success, 404 if issue not found
- Idempotent: Check `is_send` flag before sending to prevent duplicate emails

### Email Service Class
- Create `com.administrativetool.service.EmailService`
- Use `JavaMailSender` from Spring Boot Mail
- Method: `sendIssueEmail(Issue issue)` returns void
- Throw appropriate exceptions for email sending failures

## Security Requirements

### Authentication
- Form-based login (HTTP Basic is deprecated for this application)
- BCrypt password encoding (minimum 10 rounds, preferably 12)
- No plaintext passwords stored anywhere

### Authorization - Role-Based Access Control
Roles:
- **ADMIN** - Full access to all features
  - Create, read, update, delete any issue
  - Send issues to Administrative Company
  - Manage users (create, update roles, change passwords)
  - Access all configuration
- **USER** - Standard access
  - View all issues
  - Create new issues
  - Update issues they created or are assigned to
  - Cannot send emails to Administrative Company
  - Cannot manage other users

### Security Configuration
- Extend `WebSecurityConfigurerAdapter` (or SecurityFilterChain for Spring Security 6+)
- Configure form login with custom login page
- Configure logout with CSRF protection
- In-memory user database for initial setup (upgrade to database-backed users in production)
- Default admin user: username=admin, password=admin (BCrypt encoded)

### Password Requirements
- Minimum 8 characters
- Recommended: mix of uppercase, lowercase, numbers, and special characters

## API Design Patterns

### RESTful Conventions
- Use `@RestController` with `@RequestMapping("/api/resource")`
- CRUD operations:
  - `GET /api/issues` - List all issues
  - `GET /api/issues/{id}` - Get single issue
  - `POST /api/issues` - Create new issue (returns 201/202)
  - `PUT /api/issues/{id}` - Update issue (returns 202)
  - `DELETE /api/issues/{id}` - Delete issue (returns 202/204)
- Custom actions:
  - `POST /api/issues/{id}/send` - Send issue to Administrative Company
  - `POST /api/issues/{id}/status` - Change issue status

### Response Codes
- **200 OK** - Successful GET or action (send, status change)
- **201 Created** - Successful resource creation
- **202 Accepted** - Successful update/delete (async processing indication)
- **204 No Content** - Successful deletion
- **400 Bad Request** - Invalid input data
- **401 Unauthorized** - Authentication required or failed
- **403 Forbidden** - User lacks required permissions
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error (should be minimized)

### Error Handling
- Use `GlobalExceptionHandler` for consistent error responses
- Return `ResponseEntity` for 404/not found scenarios
- Check for null before operations: `if (existing == null) { return ResponseEntity.notFound().build(); }`
- Repository: use `.orElse(null)` for optional results (template pattern)
- No checked exceptions in service layer

## Business Rules & Workflow

### Issue Lifecycle
1. **Creation**: User creates issue with PREPARED status
2. **Send to Admin**: Admin clicks "Send" button → Email sent → Status changes to IN_PROGRESS
3. **Processing**: Administrative Company works on issue
4. **Acknowledgment**: Admin changes status to ACKNOWLEDGED when company responds
5. **Resolution**: Admin changes status to RESOLVED when issue is completed
6. **Rejection**: Admin can change to WON'T_DO if issue is rejected

### Email Sending Logic
```java
// Pseudocode for send endpoint
POST /api/issues/{id}/send
- Find issue by ID
- If issue == null, return 404
- If issue.isSend() == true, return 200 (already sent)
- Send email to ADMIN_COMPANY_EMAIL with issue details
- Update issue.status = IN_PROGRESS
- Update issue.isSend = true
- Save issue
- Return 200 OK
```

### Status Validation
- Prevent illegal status transitions
- Validate that issue can only be sent once (is_send flag)
- Allow WON'T_DO from any status (rejection override)
- Allow reopening RESOLVED to PREPARED (optional feature)

## Configuration Management

### Environment Variables
All sensitive configuration must use environment variables:
- Database credentials: `DB_USERNAME`, `DB_PASSWORD`
- Email credentials: `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `ADMIN_COMPANY_EMAIL`
- SMTP settings: `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`

### application.properties Structure
```
# Database configuration
spring.datasource.url=${DB_URL:jdbc:postgresql://postgres:5432/admtool}
spring.datasource.username=${DB_USERNAME:admtool}
spring.datasource.password=${DB_PASSWORD:admtool}

# Email configuration
spring.mail.host=${SPRING_MAIL_HOST:smtp.example.com}
spring.mail.port=${SPRING_MAIL_PORT:587}
spring.mail.username=${SPRING_MAIL_USERNAME:}
spring.mail.password=${SPRING_MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=${SMTP_AUTH:true}
spring.mail.properties.mail.smtp.starttls.enable=${SMTP_STARTTLS:true}
admin.company.email=${ADMIN_COMPANY_EMAIL:admin@example.com}

# Security configuration (for development only - use env vars in production)
app.security.admin.username=${ADMIN_USERNAME:admin}
app.security.admin.password=${ADMIN_PASSWORD:admin}
```

### Security Best Practices
- Never commit secrets to repository
- Use default values only for local development
- Production must override with environment variables
- Rotate credentials periodically
- Use strong passwords (minimum 12 characters with mixed types)

## Package Structure
```
com.administrativetool/
├── AdministrativeToolApplication.java (main class)
├── config/
│   ├── SecurityConfig.java          (Security configuration)
│   └── EmailConfig.java             (Email configuration)
├── controller/
│   └── IssueController.java         (REST endpoints)
├── domain/
│   ├── model/
│   │   ├── Issue.java               (Issue entity)
│   │   ├── User.java                (User entity)
│   │   ├── Status.java              (Status enum)
│   │   └── Priority.java            (Priority enum)
│   └── event/
│       ├── IssueCreatedEvent.java
│       ├── IssueUpdatedEvent.java
│       └── IssueDeletedEvent.java
├── repository/
│   ├── IssueRepository.java         (Issue data access)
│   └── UserRepository.java          (User data access)
├── application/
│   ├── query/
│   │   └── IssueQueryService.java   (Read operations)
│   ├── command/
│   │   └── IssueCommandService.java (Write operations)
│   └── handler/
│       └── IssueCommandHandler.java
├── service/
│   └── EmailService.java            (Email sending logic)
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

### Package Structure
```
com.administrativetool/
├── AdministrativeToolApplication.java (main class)
├── config/
│   ├── SecurityConfig.java          (Security configuration)
│   └── EmailConfig.java             (Email configuration)
├── controller/
│   └── IssueController.java         (REST endpoints)
├── domain/
│   ├── model/
│   │   ├── Issue.java               (Issue entity)
│   │   ├── User.java                (User entity)
│   │   ├── Status.java              (Status enum)
│   │   └── Priority.java            (Priority enum)
│   └── event/
│       ├── IssueCreatedEvent.java
│       ├── IssueUpdatedEvent.java
│       └── IssueDeletedEvent.java
├── repository/
│   ├── IssueRepository.java         (Issue data access)
│   └── UserRepository.java          (User data access)
├── application/
│   ├── query/
│   │   └── IssueQueryService.java   (Read operations)
│   ├── command/
│   │   └── IssueCommandService.java (Write operations)
│   └── handler/
│       └── IssueCommandHandler.java
├── service/
│   └── EmailService.java            (Email sending logic)
├── dto/
│   └── EmailRequest.java            (Email DTO)
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

## Notes
- This is a template/starting point - consider upgrading security and database config for production
- Java 25 via Gradle toolchains - no local JDK 25 required
- IDE: IntelliJ IDEA (.idea/ configs present) but standard Gradle build works anywhere
- IMPORTANT: do not use specific types in java files (e.g., Strin) use only `final var`
- Use `var` instead of specific types for local variables in tests (e.g., var issue)

## Active Technologies
- Java 25 (LTS) (001-issue-tracker)
- PostgreSQL 16+ with Spring Data JDBC (001-issue-tracker)
- Java 25 (via Gradle toolchain) + Spring Boot 4.0.2, Spring Mail (JavaMailSender), Spring Security 6, Thymeleaf 3, HTMX 2.0.8, Lombok (004-issue-email)
- PostgreSQL 16+ via Spring Data JDBC (no schema changes needed) (004-issue-email)

## Recent Changes
- 001-issue-tracker: Added Java 25 (LTS)
