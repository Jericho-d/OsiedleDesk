# API Contracts

**Feature**: Issue Tracker for Property Management  
**Date**: 2026-02-07  
**Base URL**: `/api`  
**Content-Type**: `application/json` for REST endpoints, `text/html` for HTMX endpoints

---

## Authentication

### Login

```
POST /login
Content-Type: application/x-www-form-urlencoded

Body:
username={username}&password={password}

Response:
- 302 Redirect to /board (success)
- 302 Redirect to /login?error (failure)
```

### Logout

```
POST /logout

Response:
- 302 Redirect to /login
```

---

## Issues API

### List All Issues

```
GET /api/issues

Headers:
Accept: application/json

Response 200 OK:
[
  {
    "id": 1,
    "title": "Broken street light",
    "description": "The light on Main Street...",
    "status": "PREPARED",
    "priority": "HIGH",
    "assignee": null,
    "isSent": false,
    "creatorId": 2,
    "createdAt": "2026-02-07T10:00:00",
    "updatedAt": "2026-02-07T10:00:00"
  }
]
```

### Get Single Issue

```
GET /api/issues/{id}

Response 200 OK:
{
  "id": 1,
  "title": "Broken street light",
  "description": "The light on Main Street is not working for 3 days",
  "status": "PREPARED",
  "priority": "HIGH",
  "assignee": null,
  "isSent": false,
  "sentAt": null,
  "creatorId": 2,
  "createdAt": "2026-02-07T10:00:00",
  "updatedAt": "2026-02-07T10:00:00"
}

Response 404 Not Found:
{
  "error": "Issue not found",
  "id": 999
}
```

### Create Issue

```
POST /api/issues
Content-Type: application/json

Request Body:
{
  "title": "Broken street light",
  "description": "The light on Main Street is not working",
  "priority": "HIGH"
}

Validation Rules:
- title: required, 1-200 chars
- description: required, 1-2000 chars
- priority: optional, default MEDIUM (LOW, MEDIUM, HIGH, CRITICAL)

Response 201 Created:
{
  "id": 1,
  "title": "Broken street light",
  "description": "The light on Main Street is not working",
  "status": "PREPARED",
  "priority": "HIGH",
  "assignee": null,
  "isSent": false,
  "creatorId": 2,
  "createdAt": "2026-02-07T10:00:00",
  "updatedAt": "2026-02-07T10:00:00"
}

Response 400 Bad Request:
{
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}

Response 401 Unauthorized:
{
  "error": "Authentication required"
}
```

### Update Issue

```
PUT /api/issues/{id}
Content-Type: application/json

Request Body (partial update allowed):
{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "CRITICAL"
}

Response 200 OK:
{
  "id": 1,
  "title": "Updated title",
  "description": "Updated description",
  "status": "PREPARED",
  "priority": "CRITICAL",
  "assignee": null,
  "isSent": false,
  "creatorId": 2,
  "createdAt": "2026-02-07T10:00:00",
  "updatedAt": "2026-02-07T11:00:00"
}

Response 404 Not Found:
{
  "error": "Issue not found",
  "id": 999
}

Response 403 Forbidden:
{
  "error": "Only administrators can update issues they did not create"
}
```

### Delete Issue

```
DELETE /api/issues/{id}

Response 204 No Content

Response 404 Not Found:
{
  "error": "Issue not found",
  "id": 999
}

Response 403 Forbidden:
{
  "error": "Only administrators can delete issues"
}
```

### Send Issue to Administrative Company

```
POST /api/issues/{id}/send

Authorization: ADMIN role required

Response 200 OK:
{
  "issueId": 1,
  "status": "QUEUED",
  "message": "Email queued for delivery"
}

Response 409 Conflict (Already sent):
{
  "issueId": 1,
  "status": "ALREADY_SENT",
  "message": "Email was already sent",
  "sentAt": "2026-02-07T10:30:00"
}

Response 403 Forbidden:
{
  "error": "Admin access required"
}

Response 404 Not Found:
{
  "error": "Issue not found",
  "id": 999
}

Response 500 Internal Server Error:
{
  "issueId": 1,
  "status": "FAILED",
  "message": "Email service unavailable"
}
```

### Update Issue Status

```
POST /api/issues/{id}/status
Content-Type: application/json
Authorization: ADMIN role required

Request Body:
{
  "status": "IN_PROGRESS"
}

Valid status values:
- PREPARED
- IN_PROGRESS
- ACKNOWLEDGED
- RESOLVED
- WON'T_DO

Response 200 OK:
{
  "id": 1,
  "title": "Broken street light",
  "status": "IN_PROGRESS",
  "updatedAt": "2026-02-07T11:00:00"
}

Response 400 Bad Request:
{
  "error": "Invalid status value",
  "validValues": ["PREPARED", "IN_PROGRESS", "ACKNOWLEDGED", "RESOLVED", "WON'T_DO"]
}

Response 403 Forbidden:
{
  "error": "Admin access required"
}
```

### Search Issues

```
GET /api/issues/search?keyword={keyword}&status={status}

Query Parameters:
- keyword: optional, searches in title and description (case-insensitive)
- status: optional, filter by status

Response 200 OK:
[
  {
    "id": 1,
    "title": "Broken street light",
    "description": "...",
    "status": "PREPARED",
    "priority": "HIGH",
    "creatorId": 2,
    "createdAt": "2026-02-07T10:00:00"
  }
]
```

---

## HTMX Endpoints (Server-Side Rendering)

### Get Board View

```
GET /board

Headers:
Accept: text/html

Response 200 OK:
Returns complete Thymeleaf-rendered HTML page with kanban board
```

### Get Issue Card Fragment (HTMX)

```
GET /fragments/issues/{id}/card

Headers:
HX-Request: true

Response 200 OK:
Returns HTML fragment for issue card (for HTMX swapping)

<div class="issue-card" id="issue-1">
  <h3>Broken street light</h3>
  <span class="badge priority-high">HIGH</span>
  <button hx-post="/api/issues/1/send" hx-target="#issue-1">Send</button>
</div>
```

### Get Column Fragment (HTMX)

```
GET /fragments/board/column?status={status}

Response 200 OK:
Returns HTML fragment for status column with all issues

<div class="column" id="column-prepared">
  <h2>PREPARED</h2>
  <div class="issue-list">
    <!-- Issue cards -->
  </div>
</div>
```

---

## Users API

### Register User

```
POST /api/users/register
Content-Type: application/json

Request Body:
{
  "username": "john_doe",
  "password": "securePassword123"
}

Validation Rules:
- username: 3-50 chars, alphanumeric + underscore, unique
- password: min 8 chars

Response 201 Created:
{
  "id": 3,
  "username": "john_doe",
  "role": "USER",
  "createdAt": "2026-02-07T10:00:00"
}

Response 400 Bad Request:
{
  "errors": [
    {
      "field": "username",
      "message": "Username already exists"
    }
  ]
}
```

### Get Current User

```
GET /api/users/me

Response 200 OK:
{
  "id": 2,
  "username": "john_doe",
  "role": "USER",
  "createdAt": "2026-02-07T09:00:00"
}

Response 401 Unauthorized:
{
  "error": "Not authenticated"
}
```

### List All Users (Admin Only)

```
GET /api/users

Authorization: ADMIN role required

Response 200 OK:
[
  {
    "id": 1,
    "username": "admin",
    "role": "ADMINISTRATOR",
    "createdAt": "2026-02-01T00:00:00"
  },
  {
    "id": 2,
    "username": "john_doe",
    "role": "USER",
    "createdAt": "2026-02-07T09:00:00"
  }
]
```

---

## Error Response Format

All error responses follow this structure:

```json
{
  "timestamp": "2026-02-07T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/issues",
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

Standard HTTP Status Codes:

- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success, no body
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists or state conflict
- `500 Internal Server Error` - Server error

---

## Rate Limits

None implemented in initial version. Consider adding:

- 100 requests per minute per user
- 5 login attempts per 15 minutes (account lockout already in spec)

---

## CORS Configuration

Not required - single-domain application with server-side rendering.

---

## Security Headers

Spring Security provides by default:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security` (when HTTPS enabled)
- `Content-Security-Policy` (configurable)
