# Research: Issue Tracker Implementation

**Date**: 2026-02-07  
**Feature**: Issue Tracker for Property Management  
**Research Scope**: Technology stack selection for Java 25 + Spring Boot + Lightweight Frontend

---

## 1. Backend Framework: Spring Boot 4.0.2

### Decision
**Spring Boot 4.0.2** with **Java 25**

### Rationale
- Spring Boot 4.0.2 officially supports Java 17 through Java 25 (confirmed in Spring documentation)
- Provides auto-configuration for all required components (security, mail, data access)
- Mature ecosystem with excellent documentation
- Built-in embedded Tomcat for easy deployment on resource-constrained machines

### Alternatives Considered
- **Quarkus**: Native compilation support but steeper learning curve, overkill for this use case
- **Micronaut**: Good alternative but Spring Boot has larger community and better documentation
- **Plain Java + Jetty**: Too low-level, would require significant boilerplate

---

## 2. Build Tool: Gradle 9.x

### Decision
**Gradle 9.x** with Kotlin DSL

### Rationale
- Project already uses Gradle (from AGENTS.md)
- Spring Boot 4.0.2 requires Gradle 8.14+ or 9.x
- Better incremental build performance than Maven
- Kotlin DSL provides type-safe build scripts

### Alternatives Considered
- **Maven 3.6.3+**: Widely used but less flexible, slower builds

---

## 3. Frontend Technology: HTMX + Thymeleaf

### Decision
**HTMX 2.0.x** + **Thymeleaf 3.2.x** with **htmx-spring-boot** library

### Rationale
- **Ultra-lightweight**: HTMX is ~14KB minified, perfect for 4GB RAM constraint
- **No build step required**: Direct CDN or webjar inclusion, no npm/node needed
- **Server-side rendering**: HTML generated on server, minimal client-side JS
- **Progressive enhancement**: Works without JavaScript, enhanced with HTMX
- **Spring Boot integration**: `htmx-spring-boot` library (v5.0.0) provides Thymeleaf helpers and annotation support
- **Perfect fit**: Enables SPA-like interactivity (inline editing, dynamic updates) without heavy JS frameworks

### Key Libraries
```
org.springframework.boot:spring-boot-starter-thymeleaf
io.github.wimdeblauwe:htmx-spring-boot:5.0.0
org.webjars.npm:htmx.org:2.0.8
```

### Alternatives Considered

| Option | Pros | Cons | Verdict |
|--------|------|------|---------|
| **React/Vue/Angular** | Rich ecosystem, component-based | Heavy (100KB+), requires Node.js/npm build, overkill for simple CRUD | ❌ Rejected - violates 4GB RAM constraint |
| **Alpine.js** (~15KB) | Lightweight, reactive | Less mature Spring Boot integration | ❌ Rejected - HTMX has better Spring support |
| **Thymeleaf only** | No additional JS | Full page reloads for updates | ❌ Rejected - poor UX for board updates |
| **JTE (Java Template Engine)** | Very fast, type-safe | Newer, smaller community | ❌ Rejected - Thymeleaf is standard |

### Research Sources
- "Modern frontends with htmx" book by Wim Deblauwe (Spring I/O 2020 talk)
- Spring.io blog: "Hypermedia and Browser Enhancement" (March 2024)
- GitHub: wimdeblauwe/htmx-spring-boot library with 600+ stars

---

## 4. Database: PostgreSQL

### Decision
**PostgreSQL 16+** with **Spring Data JDBC** (not JPA/Hibernate)

### Rationale
- PostgreSQL mentioned in project AGENTS.md
- Spring Data JDBC is lighter than JPA/Hibernate (no session cache, simpler)
- Good fit for simple CRUD operations with explicit SQL control
- Suitable for up to 1000 issues as per requirements

### Alternatives Considered
- **H2**: Good for development, but PostgreSQL preferred for production
- **MySQL**: PostgreSQL has better JSON support if needed later

---

## 5. Email Service: JavaMailSender

### Decision
**JavaMailSender** with **Spring Boot Mail Starter**

### Rationale
- Standard Spring Boot approach for email
- Simple configuration via application.properties
- Supports both SimpleMailMessage (text) and MimeMessage (HTML)
- Async sending support recommended for non-blocking user experience

### Configuration Pattern
```properties
spring.mail.host=${SPRING_MAIL_HOST}
spring.mail.port=${SPRING_MAIL_PORT:587}
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Best Practices Identified
1. **Async sending**: Use `@Async` to prevent blocking HTTP requests
2. **Retry logic**: Implement for transient failures
3. **Timeout configuration**: Set connection/write timeouts (5s/3s recommended)
4. **Environment variables**: Never hardcode credentials

---

## 6. Security: Spring Security

### Decision
**Spring Security 6.x** with **BCrypt** password encoding

### Rationale
- Industry standard for Spring Boot applications
- Form-based authentication (as per spec requirements)
- Role-based access control (USER vs ADMINISTRATOR)
- Session management with timeout support
- Protection against CSRF, session fixation, etc.

### Key Components
- `SecurityFilterChain` (WebSecurityConfigurerAdapter deprecated in Spring Security 6)
- `BCryptPasswordEncoder` (strength 10-12)
- `UserDetailsService` for custom user loading

---

## 7. Additional Libraries

### Lombok
**Decision**: Use Lombok for boilerplate reduction  
**Rationale**: Reduces POJO boilerplate (getters, setters, constructors), mentioned in AGENTS.md

### Validation
**Decision**: Jakarta Bean Validation (spring-boot-starter-validation)  
**Rationale**: Declarative validation annotations (@NotNull, @Size, etc.)

### WebJars
**Decision**: Use WebJars for client-side libraries (HTMX)  
**Rationale**: Version management via Gradle, no CDN dependency

---

## Summary: Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Language | Java | 25 | Backend language |
| Framework | Spring Boot | 4.0.2 | Application framework |
| Build Tool | Gradle | 9.x | Build automation |
| Template Engine | Thymeleaf | 3.2.x | Server-side HTML rendering |
| Frontend Enhancement | HTMX | 2.0.x | Progressive AJAX enhancement |
| HTMX Integration | htmx-spring-boot | 5.0.0 | Spring Boot helpers |
| Database | PostgreSQL | 16+ | Data persistence |
| Data Access | Spring Data JDBC | 4.0.x | Database access |
| Email | JavaMailSender | N/A | Email sending |
| Security | Spring Security | 6.x | Authentication/authorization |
| Password Hashing | BCrypt | N/A | Password encryption |
| Validation | Jakarta Validation | 3.x | Input validation |
| Boilerplate | Lombok | N/A | Code generation |

---

## Resource Estimates

### Memory Footprint
- **JVM Heap**: 512MB-1GB (suitable for 4GB RAM machine)
- **PostgreSQL**: 256MB shared memory
- **Frontend**: Negligible (server-side rendered)
- **Total**: ~1.5GB at runtime, well within 4GB constraint

### Disk Space
- **Application JAR**: ~50-80MB (with dependencies)
- **PostgreSQL Data**: ~100MB (for 1000 issues)
- **Logs**: ~50MB
- **Total**: Well within 8GB constraint

---

## Research Conclusion

All technical decisions support the success criteria:
- ✅ **<3s load time**: HTMX + Thymeleaf SSR is extremely fast
- ✅ **<500KB frontend**: HTMX is ~14KB, no JS framework overhead
- ✅ **Java 25 support**: Spring Boot 4.0.2 officially supports Java 25
- ✅ **4GB RAM compatible**: Minimal memory footprint design
- ✅ **Email reliability**: JavaMailSender with async sending and retry logic
