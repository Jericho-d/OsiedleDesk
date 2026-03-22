---
name: code-review
description: Mandatory post-implementation code review. Apply after every agent completes work to verify quality, correctness, patterns, and safety. Covers static analysis, AGENTS.md compliance, security, testing, and architectural coherence.
---

# code-review

Structured code review skill for validating agent output before delivery. Every agent that produces code changes MUST
run this review before reporting completion.

## When to Apply

- After ANY code implementation task completes
- After refactoring, bug fixes, or feature additions
- Before creating a commit or PR
- When delegating work — the orchestrator runs this on the agent's output

## Review Process

### Step 0: Gather Scope

Identify all files changed in the current work session. Use `git diff` (staged + unstaged) or the known list of modified
files from the task.

```bash
git diff --name-only HEAD
git diff --name-only --cached
```

If no git changes exist (new session or untracked files), use the file list from the task context.

**Output**: A definitive list of files to review. If zero files changed — skip review, report "No changes to review."

---

### Step 1: AGENTS.md Compliance (Project Conventions)

Read `AGENTS.md` at the project root. This is the source of truth for project-specific rules. Verify every changed file
against it.

**Check each applicable rule:**

#### 1.1 — Import Order & Style

- [ ] Imports ordered: Java stdlib → third-party (Spring, Lombok) → project-local (`com.administrativetool`)
- [ ] Groups separated by blank lines
- [ ] No wildcard imports in production code (allowed in tests)

#### 1.2 — Naming Conventions

- [ ] Classes: `CamelCase`
- [ ] Methods/fields: `camelCase`
- [ ] Constants: `UPPER_SNAKE_CASE`
- [ ] Enums: values in `UPPER_SNAKE_CASE`
- [ ] Table mapping: `@Table("plural_snake_case")` with `@Column("snake_case_name")`

#### 1.3 — Type Usage

- [ ] `var` used instead of explicit types for local variables (per AGENTS.md: "do not use specific types in java files,
  use only `final var`")
- [ ] `Long` for entity IDs with `@Id`
- [ ] Lombok annotations (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) on model classes — no boilerplate
  getters/setters

#### 1.4 — Architectural Patterns

- [ ] Entities in `com.administrativetool.model` (or `domain/model`)
- [ ] Repositories extend `CrudRepository<Entity, Long>`
- [ ] Services use constructor injection with `@Autowired`
- [ ] Controllers use `@RestController` + `@RequestMapping("/api/resource")`
- [ ] Controllers return `ResponseEntity` for error scenarios
- [ ] Repository uses `.orElse(null)` for optional results
- [ ] No checked exceptions in service layer

#### 1.5 — Test Conventions (if tests changed)

- [ ] JUnit 5 only (no JUnit 4 imports)
- [ ] AssertJ for assertions — prefer `.usingRecursiveComparison()` over multiple `assertThat()`
- [ ] No access modifiers (`public`/`private`) on test methods or fields
- [ ] `var` for local variables in tests
- [ ] `@SpringBootTest` for integration, `@WebMvcTest` for controller tests
- [ ] Test package mirrors source package structure

**Severity**: AGENTS.md violations are **CRITICAL** — these are explicit project rules.

---

### Step 2: Static Analysis (Language Server)

Run `lsp_diagnostics` on every changed file.

```
For each changed file:
  → lsp_diagnostics(filePath, severity="error")
  → lsp_diagnostics(filePath, severity="warning")
```

**Evaluate results:**

- **Errors**: CRITICAL — must be fixed before delivery
- **Warnings**: WARNING — should be fixed, note pre-existing ones separately
- Distinguish between pre-existing issues and newly introduced ones (compare with `git stash` state if needed)

**Absolute prohibitions (auto-fail):**

- `@SuppressWarnings` added to hide new warnings
- `@ts-ignore`, `@ts-expect-error`, `as any` (for any TS/JS in the project)
- Empty catch blocks: `catch(e) {}`
- Commented-out code blocks (dead code)

---

### Step 3: Security Review

Scan changed files for security issues relevant to this Spring Boot application.

#### 3.1 — Secrets & Configuration

- [ ] No hardcoded passwords, API keys, tokens, or connection strings
- [ ] Sensitive values use environment variables (e.g., `${SPRING_MAIL_PASSWORD}`)
- [ ] No secrets in committed files (`.env`, `credentials.json`, etc.)
- [ ] Default values in `application.properties` are safe for local dev only

#### 3.2 — Input Validation

- [ ] User inputs validated (`@Valid`, `@NotNull`, `@Size`, etc.)
- [ ] No SQL injection vectors (parameterized queries, Spring Data methods)
- [ ] No path traversal possibilities in file operations
- [ ] Request DTOs used (not raw entity binding from HTTP)

#### 3.3 — Authentication & Authorization

- [ ] Endpoints have proper security annotations or SecurityFilterChain rules
- [ ] Role checks match AGENTS.md (ADMIN vs USER permissions)
- [ ] No sensitive data exposed in API responses (passwords, internal IDs, stack traces)

#### 3.4 — Email Security (project-specific)

- [ ] Email credentials from environment variables only
- [ ] No email content injection (user input sanitized before email body)
- [ ] `is_send` flag checked for idempotency before sending

**Severity**: Security issues are **CRITICAL**.

---

### Step 4: Logic & Correctness

Review the actual implementation logic of changed code.

#### 4.1 — Business Logic

- [ ] Status transitions follow the valid workflow (PREPARED → IN_PROGRESS → ACKNOWLEDGED → RESOLVED, any → WON'T_DO)
- [ ] Edge cases handled: null checks, empty collections, boundary values
- [ ] No silent failures — errors are logged or propagated
- [ ] Idempotent operations where expected (email send, status change)

#### 4.2 — Error Handling

- [ ] Errors return appropriate HTTP status codes (400, 401, 403, 404, 500)
- [ ] `ResponseEntity.notFound().build()` for missing resources
- [ ] No swallowed exceptions (empty catch blocks)
- [ ] Consistent error response format (GlobalExceptionHandler pattern)

#### 4.3 — Data Integrity

- [ ] Database operations use transactions where needed
- [ ] No N+1 query patterns introduced
- [ ] Entity state changes are persisted (`.save()` called)
- [ ] Null checks before repository operations

**Severity**: Logic issues are **CRITICAL** if they cause data loss/corruption or incorrect behavior; **WARNING**
otherwise.

---

### Step 5: Code Quality & Maintainability

#### 5.1 — Complexity

- [ ] No methods exceeding ~30 lines (suggest extraction)
- [ ] No deeply nested conditionals (>3 levels)
- [ ] Single Responsibility — each method/class has one clear purpose
- [ ] No code duplication across changed files

#### 5.2 — Readability

- [ ] Variable and method names are descriptive and self-documenting
- [ ] No magic numbers or strings — use constants or enums
- [ ] Complex logic has explanatory comments (but not obvious code)
- [ ] Consistent formatting with surrounding code

#### 5.3 — Dead Code

- [ ] No commented-out code blocks
- [ ] No unused imports
- [ ] No unreachable code paths
- [ ] No unused variables or parameters

**Severity**: Quality issues are **WARNING** or **SUGGESTION**.

---

### Step 6: Frontend Review (if HTML/CSS/JS changed)

Applies when Thymeleaf templates, CSS, or JavaScript files are modified.

#### 6.1 — HTML/Thymeleaf

- [ ] Proper `th:` attribute usage (no raw dynamic values)
- [ ] Accessibility: `role`, `aria-label`, `alt` attributes present
- [ ] Semantic HTML elements (`<main>`, `<nav>`, `<section>`)
- [ ] No inline styles (use CSS classes)

#### 6.2 — CSS

- [ ] Uses design tokens (CSS variables from `:root`) — not hardcoded values
- [ ] Works with dark mode (`[data-theme="dark"]`) if applicable
- [ ] Responsive: tested at 480px, 768px, 1024px breakpoints
- [ ] No `!important` unless overriding third-party styles
- [ ] Animation performance: uses `transform`/`opacity` over layout properties

#### 6.3 — JavaScript

- [ ] No global variable leaks (use IIFE or `let`/`const`)
- [ ] Event listeners cleaned up (no memory leaks)
- [ ] HTMX integration follows existing patterns (`hx-get`, `hx-trigger`, `hx-swap`)
- [ ] SortableJS callbacks handle edge cases (null checks on `evt.to`, `evt.from`)
- [ ] No `console.log` left in production code

#### 6.4 — Cross-cutting

- [ ] CSRF tokens included in POST forms
- [ ] No XSS vectors (user input escaped in templates)
- [ ] Loading states shown during async operations (HTMX indicators)

**Severity**: Accessibility and XSS issues are **CRITICAL**; visual/UX issues are **WARNING**.

---

### Step 7: Test Coverage Assessment

#### 7.1 — New Code Coverage

- [ ] New public methods have corresponding tests
- [ ] New endpoints have controller tests (`@WebMvcTest`)
- [ ] New service logic has unit tests
- [ ] Edge cases from Step 4 have test coverage

#### 7.2 — Existing Tests

- [ ] Existing tests still pass (run `./gradlew test` if feasible)
- [ ] No tests deleted or weakened to make them pass
- [ ] Modified behavior reflected in updated test assertions

#### 7.3 — Test Quality

- [ ] Tests verify behavior, not implementation details
- [ ] Descriptive test method names (describe what is being tested)
- [ ] No test interdependencies (each test is isolated)
- [ ] AssertJ used properly (not `assertEquals` from JUnit)

**Severity**: Missing tests for critical logic are **WARNING**; test quality issues are **SUGGESTION**.

---

## Report Format

Generate a structured report after completing all steps:

```
## Code Review Report

### Summary
| Category             | Status | Issues |
|----------------------|--------|--------|
| AGENTS.md Compliance | PASS/FAIL | N     |
| Static Analysis      | PASS/FAIL | N     |
| Security             | PASS/FAIL | N     |
| Logic & Correctness  | PASS/FAIL | N     |
| Code Quality         | PASS/FAIL | N     |
| Frontend             | PASS/FAIL/N/A | N  |
| Test Coverage        | PASS/FAIL | N     |

### Issues Found

#### CRITICAL (must fix)
1. **[Category]** Description — `file:line` — Fix: specific action

#### WARNING (should fix)
1. **[Category]** Description — `file:line` — Suggestion: specific action

#### SUGGESTION (consider)
1. **[Category]** Description — `file:line` — Idea: specific action

### Verdict
- **APPROVED**: Zero CRITICAL, acceptable WARNINGs
- **CHANGES REQUESTED**: CRITICAL issues exist, list required fixes
- **NEEDS DISCUSSION**: Architectural concerns requiring human decision
```

---

## Severity Classification

| Level          | Definition                                                                           | Action                          |
|----------------|--------------------------------------------------------------------------------------|---------------------------------|
| **CRITICAL**   | Breaks functionality, violates project rules, security vulnerability, data loss risk | Must fix before delivery        |
| **WARNING**    | Suboptimal but functional, missing tests, minor inconsistency                        | Should fix, document if skipped |
| **SUGGESTION** | Improvement opportunity, style preference, future tech debt                          | Note for consideration          |

---

## Review Principles

1. **Be specific** — Always include file path, line number, and a concrete fix. Never say "consider improving."
2. **Respect project conventions** — AGENTS.md rules are non-negotiable, not suggestions.
3. **Distinguish new vs pre-existing** — Only flag issues introduced by the current changes. Note pre-existing problems
   separately.
4. **Minimize false positives** — When uncertain, prefer SUGGESTION over WARNING.
5. **Review intent, not just syntax** — Does the code actually solve the problem it was meant to solve?
6. **Don't block on style** — If it matches the project's existing patterns, it's fine even if you'd do it differently.
7. **Check the diff, not just the file** — Focus review effort on what actually changed.
