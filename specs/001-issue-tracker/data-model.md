# Data Model

**Feature**: Issue Tracker for Property Management  
**Date**: 2026-02-07  
**Database**: PostgreSQL 16+ with Spring Data JDBC

---

## Entity Relationship Diagram

```
┌─────────────────┐       ┌─────────────────┐
│     users       │       │     issues      │
├─────────────────┤       ├─────────────────┤
│ PK id           │──┐    │ PK id           │
│    username     │  │    │ FK creator_id   │─┘
│    password     │  │    │    title        │
│    role         │  │    │    description  │
│    created_at   │  │    │    status       │
│    updated_at   │  │    │    priority     │
└─────────────────┘  │    │    assignee     │
                     │    │    is_sent      │
                     │    │    sent_at      │
                     │    │    created_at   │
                     │    │    updated_at   │
                     │    └─────────────────┘
                     │
                     └────FK references users.id (creator)
```

---

## Table: users

Stores authenticated users with role-based access control.

### Fields

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique user identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login username (3-50 chars) |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | USER or ADMINISTRATOR |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Account creation time |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update time |

### Indexes

- `PRIMARY KEY (id)`
- `UNIQUE INDEX idx_username (username)`

### Constraints

- Username: 3-50 characters, alphanumeric + underscore
- Password: Stored as BCrypt hash (60-72 chars)
- Role: CHECK constraint - must be 'USER' or 'ADMINISTRATOR'

### Sample Data

```sql
INSERT INTO users (username, password, role, created_at) VALUES
('admin', '$2a$12$...hash...', 'ADMINISTRATOR', NOW()),
('john_doe', '$2a$12$...hash...', 'USER', NOW());
```

---

## Table: issues

Stores issue reports submitted by residents.

### Fields

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique issue identifier |
| creator_id | BIGINT | FK → users.id, NOT NULL | User who created the issue |
| title | VARCHAR(200) | NOT NULL | Issue title/summary |
| description | TEXT | NOT NULL | Detailed description |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PREPARED' | Current status |
| priority | VARCHAR(20) | NOT NULL, DEFAULT 'MEDIUM' | Priority level |
| assignee | VARCHAR(50) | NULL | Assigned user (optional) |
| is_sent | BOOLEAN | NOT NULL, DEFAULT FALSE | Email sent flag |
| sent_at | TIMESTAMP | NULL | When email was sent |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update time |

### Indexes

- `PRIMARY KEY (id)`
- `INDEX idx_status (status)` - For board view queries
- `INDEX idx_creator (creator_id)` - For user's issues
- `INDEX idx_created_at (created_at DESC)` - For sorting

### Constraints

- Status: CHECK constraint - must be one of PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO
- Priority: CHECK constraint - must be one of LOW, MEDIUM, HIGH, CRITICAL
- Title: Max 200 characters
- Description: Max 2000 characters

### Enums

```java
public enum Status {
    PREPARED,      // Initial state
    IN_PROGRESS,   // Email sent to admin company
    ACKNOWLEDGED,  // Admin company acknowledged
    RESOLVED,      // Issue completed
    WON'T_DO       // Issue rejected
}

public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
```

### State Transition Rules

Valid transitions (enforced at application level):

```
PREPARED → IN_PROGRESS (via Send button)
IN_PROGRESS → ACKNOWLEDGED (manual admin action)
ACKNOWLEDGED → RESOLVED (manual admin action)
[Any] → WON'T_DO (rejection from any state)
RESOLVED → PREPARED (reopening)
```

### Sample Data

```sql
INSERT INTO issues (creator_id, title, description, status, priority, created_at) VALUES
(2, 'Broken street light', 'The light on Main Street is not working for 3 days', 'PREPARED', 'HIGH', NOW()),
(2, 'Garbage not collected', 'Trash has not been picked up for a week', 'IN_PROGRESS', 'MEDIUM', NOW());
```

---

## Repository Interfaces

### UserRepository

```java
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    
    @Query("SELECT * FROM users WHERE role = :role")
    List<User> findByRole(@Param("role") String role);
}
```

### IssueRepository

```java
@Repository
public interface IssueRepository extends CrudRepository<Issue, Long> {
    
    // Board view: find all issues grouped by status
    List<Issue> findAllByOrderByCreatedAtDesc();
    
    // Find by status for column rendering
    List<Issue> findByStatusOrderByCreatedAtDesc(Status status);
    
    // Find user's issues
    @Query("SELECT * FROM issues WHERE creator_id = :creatorId ORDER BY created_at DESC")
    List<Issue> findByCreatorId(@Param("creatorId") Long creatorId);
    
    // Search by keyword (title or description)
    @Query("SELECT * FROM issues WHERE title ILIKE '%' || :keyword || '%' OR description ILIKE '%' || :keyword || '%'")
    List<Issue> searchByKeyword(@Param("keyword") String keyword);
    
    // Count by status for metrics
    @Query("SELECT status, COUNT(*) FROM issues GROUP BY status")
    Map<String, Long> countByStatus();
}
```

---

## DDL (PostgreSQL)

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMINISTRATOR')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_username ON users(username);

-- Issues table
CREATE TABLE issues (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PREPARED' 
        CHECK (status IN ('PREPARED', 'IN_PROGRESS', 'ACKNOWLEDGED', 'RESOLVED', 'WON\'T_DO')),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' 
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    assignee VARCHAR(50),
    is_sent BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_creator ON issues(creator_id);
CREATE INDEX idx_issues_created_at ON issues(created_at DESC);
```

---

## Validation Rules

### User Validation

| Field | Rule | Error Message |
|-------|------|---------------|
| username | Required, 3-50 chars, alphanumeric + underscore | "Username must be 3-50 characters" |
| username | Unique | "Username already exists" |
| password | Required, min 8 chars | "Password must be at least 8 characters" |
| role | Must be USER or ADMINISTRATOR | "Invalid role" |

### Issue Validation

| Field | Rule | Error Message |
|-------|------|---------------|
| title | Required, max 200 chars | "Title is required (max 200 characters)" |
| description | Required, max 2000 chars | "Description is required (max 2000 characters)" |
| status | Must be valid enum value | "Invalid status" |
| priority | Must be valid enum value | "Invalid priority" |

---

## Data Integrity Notes

1. **Soft Deletes**: Not implemented - issues are kept for history
2. **Audit Trail**: No separate audit table - rely on created_at/updated_at
3. **Archiving**: Not in scope - assume <1000 active issues
4. **Cascading**: User deletion cascades to their issues (ON DELETE CASCADE)
5. **Immutability**: Issue creator cannot be changed after creation
