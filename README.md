# Administrative Tool

A Spring Boot 4.0.2 + Gradle 9.3.1 application for a Jira-like issue tracking system for property management (zarządzanie nieruchomościami).

## Features

- **Issue Tracking**: Board view with columns: PREPARED, IN_PROGRESS, ACKNOWLEDGED, RESOLVED, WON'T_DO
- **Email Integration**: Send issues to Administrative Company via email with one click
- **Role-Based Security**: ADMIN and USER roles with form-based authentication
- **Health Checks**: Actuator endpoints for monitoring
- **Database**: PostgreSQL with automatic schema initialization
- **Java 25**: Latest Java version via Gradle toolchains

## Docker-Only Development

**IMPORTANT**: All builds, tests, and runs MUST be executed inside Docker containers. No local Java/Gradle installation required.

### Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- Make (optional, for convenience commands)

### Quick Start

```bash
# Clone and navigate to the project
git clone <repository>
cd administration-tool

# Build the application (runs in Docker)
make build

# Run tests (runs in Docker)
make test

# Start development server with live reload
make dev

# Access the application
open http://localhost:8080
```

### Available Make Commands

| Command | Description |
|---------|-------------|
| `make build` | Build the application in Docker container |
| `make test` | Run all tests in Docker container |
| `make run` | Run production build in Docker |
| `make dev` | Start development environment with live reload |
| `make clean` | Stop all containers and remove volumes |
| `make rollback` | Reset database to initial state |
| `make status` | Show running container status |
| `make logs` | View container logs |
| `make shell` | Open shell in development container |

### Docker Services

The application uses the following Docker services:

- **postgres**: PostgreSQL 15 database with persistent storage
- **app-dev**: Development server with live reload and volume mounts
- **gradle-build**: One-time build container
- **gradle-test**: One-time test container
- **db-rollback**: Database reset utility

### Development Workflow

1. **Start Development Environment**:
   ```bash
   make dev
   ```
   This starts PostgreSQL and the application server with live reload.

2. **Make Code Changes**:
   Edit files locally - changes are automatically detected and the server reloads.

3. **Run Tests**:
   ```bash
   make test
   ```
   Tests run in a Docker container and the database is automatically rolled back after completion.

4. **Build for Production**:
   ```bash
   make build
   ```

5. **Clean Up**:
   ```bash
   make clean
   ```

### Database Management

The database is automatically rolled back after each:
- Test run (`make test`)
- Build completion (`make build`)
- Application shutdown (`make clean`)

To manually rollback the database:
```bash
make rollback
```

### Environment Variables

Configure via environment variables or `.env` file:

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/admtool` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `admtool` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `admtool` | Database password |
| `ADMIN_COMPANY_EMAIL` | - | Email for Administrative Company |
| `SPRING_MAIL_HOST` | - | SMTP server host |
| `SPRING_MAIL_PORT` | `587` | SMTP server port |
| `SPRING_MAIL_USERNAME` | - | SMTP username |
| `SPRING_MAIL_PASSWORD` | - | SMTP password |

### API Endpoints

| Endpoint | Method | Description | Access |
|----------|--------|-------------|--------|
| `/api/issues` | GET | List all issues | ADMIN, USER |
| `/api/issues` | POST | Create new issue | ADMIN, USER |
| `/api/issues/{id}` | GET | Get issue by ID | ADMIN, USER |
| `/api/issues/{id}` | PUT | Update issue | ADMIN, USER |
| `/api/issues/{id}` | DELETE | Delete issue | ADMIN, USER |
| `/api/issues/{id}/send` | POST | Send issue via email | ADMIN only |
| `/actuator/health` | GET | Health check | Public |
| `/login` | - | Login page | Public |

### Default Users

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin` | ADMIN |
| `user` | `user` | USER |

### Project Structure

```
├── src/main/java/com/administrativetool/
│   ├── AdministrativeToolApplication.java
│   ├── config/          # Security and other configurations
│   ├── controller/      # REST API controllers
│   ├── domain/          # Domain models and events
│   │   ├── model/       # Issue, User, Status, Priority
│   │   └── event/       # Domain events
│   ├── repository/      # Data repositories
│   ├── application/     # Application services (CQRS)
│   │   ├── query/       # Read operations
│   │   ├── command/     # Write operations
│   │   └── handler/     # Event handlers
│   ├── service/         # Business services (Email)
│   └── exception/       # Custom exceptions
├── src/main/resources/
│   ├── application.yml  # Application configuration
├── src/test/            # Test classes
├── docker-compose.yml   # Production Docker services
├── docker-compose.override.yml  # Development overrides
├── Dockerfile           # Production Dockerfile
├── Dockerfile.dev       # Development Dockerfile
├── Makefile             # Convenience commands
└── scripts/             # Utility scripts
```

### Docker Architecture

```
┌─────────────────────────────────────────┐
│           Docker Compose                │
├─────────────────────────────────────────┤
│  ┌──────────┐      ┌──────────────┐    │
│  │  gradle  │      │   app-dev    │    │
│  │ (build)  │      │ (bootRun)    │    │
│  └────┬─────┘      └──────┬───────┘    │
│       │                   │            │
│       └─────────┬─────────┘            │
│                 │                      │
│          ┌──────┴──────┐               │
│          │  postgres   │               │
│          │  (database) │               │
│          └─────────────┘               │
└─────────────────────────────────────────┘
```

### Troubleshooting

**Issue**: `make build` fails with permission errors
**Solution**: Ensure the rollback script is executable: `chmod +x scripts/db-rollback.sh`

**Issue**: Port 8080 is already in use
**Solution**: Change the port in `docker-compose.override.yml`:
```yaml
ports:
  - "8081:8080"
```

**Issue**: Database connection errors
**Solution**: Ensure PostgreSQL is healthy: `docker-compose ps` should show postgres as healthy

**Issue**: Gradle daemon errors
**Solution**: The daemon is disabled in Docker. If you see daemon-related errors, ensure `GRADLE_OPTS` includes `-Dorg.gradle.daemon=false`.

### Security Notes

- Default passwords are for development only - change in production
- Email credentials should be configured via environment variables
- CSRF is disabled for API simplicity - re-enable if needed
- Use HTTPS in production

### License

[Your License Here]

### Contributing

[Your Contributing Guidelines Here]
