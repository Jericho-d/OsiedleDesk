# Quickstart Guide

**Feature**: Issue Tracker for Property Management  
**Date**: 2026-02-07  
**Estimated Setup Time**: 30 minutes

---

## Prerequisites

- Java 25 JDK (or use Gradle toolchain - no local install required)
- Docker and Docker Compose (for PostgreSQL)
- Git

---

## 1. Clone and Setup

```bash
# Clone the repository
git clone <repository-url>
cd administrativetool

# Switch to feature branch
git checkout 001-issue-tracker

# Start PostgreSQL
./gradlew composeUp
# OR manually:
docker-compose up -d postgres
```

---

## 2. Configure Environment

Create `src/main/resources/application-local.properties`:

```properties
# Database (for local development)
spring.datasource.url=jdbc:postgresql://localhost:5432/admtool
spring.datasource.username=admtool
spring.datasource.password=admtool

# Email (use your SMTP provider)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Admin company email (where issues are sent)
admin.company.email=admin@yourcompany.com

# Logging
logging.level.com.administrativetool=DEBUG
```

**Never commit this file!** It's already in `.gitignore`.

---

## 3. Run the Application

```bash
# Build and run
./gradlew bootRun

# Application starts on:
# - http://localhost:8080 (main application)
# - http://localhost:8080/h2-console (if using H2 for dev)
```

---

## 4. Initial Setup

### Create Admin User

The application seeds an admin user on first run:

- **Username**: `admin`
- **Password**: `admin`

**Change this immediately after first login!**

### Register Regular Users

1. Navigate to http://localhost:8080/register
2. Create user accounts
3. Login and start creating issues

---

## 5. Verify Installation

### Test Email Configuration

```bash
# Send test email (admin only)
curl -X POST http://localhost:8080/api/test/email \
  -u admin:admin
```

### Check Application Health

```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
{"status":"UP"}
```

---

## 6. Development Workflow

### Run Tests

```bash
# All tests
./gradlew test

# Single test class
./gradlew test --tests IssueControllerTest

# Single test method
./gradlew test --tests IssueControllerTest.shouldSendIssueEmail

# Continuous testing
./gradlew test --continuous
```

### Build for Production

```bash
# Create executable JAR
./gradlew bootJar

# JAR location
ls build/libs/*.jar

# Run JAR
java -jar build/libs/administrativetool-0.0.1-SNAPSHOT.jar
```

### Code Formatting

```bash
# Check formatting
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply
```

---

## 7. Common Tasks

### Reset Database

```bash
# Stop containers
docker-compose down -v

# Start fresh
docker-compose up -d postgres

# Run application (tables auto-created)
./gradlew bootRun
```

### View Logs

```bash
# Application logs
./gradlew bootRun | tee app.log

# PostgreSQL logs
docker-compose logs -f postgres
```

### Debug Mode

```bash
# Run with debug port 5005
./gradlew bootRun --debug-jvm

# Then attach debugger in IntelliJ:
# Run → Attach to Process → localhost:5005
```

---

## 8. Production Deployment

### Environment Variables

Set these in production (never in code):

```bash
# Required
export SPRING_MAIL_HOST=smtp.your-provider.com
export SPRING_MAIL_PORT=587
export SPRING_MAIL_USERNAME=your-email@domain.com
export SPRING_MAIL_PASSWORD=your-secure-password
export ADMIN_COMPANY_EMAIL=admin@company.com
export DB_USERNAME=dbuser
export DB_PASSWORD=dbpass
export DB_URL=jdbc:postgresql://prod-db:5432/admtool

# Optional (with defaults)
export SERVER_PORT=8080
export JAVA_OPTS="-Xms512m -Xmx1g"
```

### Docker Production Build

```bash
# Build Docker image
./gradlew bootBuildImage

# Run with docker-compose
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### System Requirements

Minimum for production:

- **RAM**: 2GB (4GB recommended)
- **CPU**: 1 core
- **Disk**: 8GB
- **OS**: Linux (Ubuntu 22.04 LTS recommended)

---

## 9. Troubleshooting

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or run on different port
./gradlew bootRun --args='--server.port=8081'
```

### Database Connection Failed

```bash
# Check if PostgreSQL is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Verify connection
psql -h localhost -U admtool -d admtool
```

### Email Not Sending

1. Check SMTP configuration in `application-local.properties`
2. Verify credentials (use app password for Gmail)
3. Check application logs for errors
4. Test SMTP connectivity:
   ```bash
   telnet smtp.gmail.com 587
   ```

### Out of Memory

```bash
# Increase JVM heap
export JAVA_OPTS="-Xms1g -Xmx2g"
./gradlew bootRun
```

---

## 10. Next Steps

1. ✅ Application running locally
2. ✅ Admin user created
3. ✅ Email configured
4. ➡️ Create first issue
5. ➡️ Test "Send" functionality
6. ➡️ Invite residents to register

### Helpful URLs

- Application: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html (if enabled)
- H2 Console: http://localhost:8080/h2-console (dev only)

---

## Support

For issues or questions:

1. Check the logs: `build/logs/spring.log`
2. Review the specification: `specs/001-issue-tracker/spec.md`
3. Check implementation plan: `specs/001-issue-tracker/plan.md`
