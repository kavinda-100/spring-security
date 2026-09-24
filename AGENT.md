# AGENT.md

## Project Overview

This project is a **Spring Boot + Spring Security learning project** focused on building a production-style, session-based authentication and authorization system.

The main goal is to learn Spring Security by implementing an authentication architecture similar to a traditional server-side Express.js application:

- Users authenticate with **email and password**.
- Authentication sessions are stored in **Redis**.
- User roles and permissions are stored in **PostgreSQL**.
- **Spring Data JPA** is used for persistence and schema/entity management.
- Redis and PostgreSQL run in **Docker containers**.
- Spring Security handles authentication and authorization.
- Spring Session is used to persist authenticated sessions in Redis.
- Active user sessions should reflect role and permission changes when appropriate.

This is a learning project. Code should favor clarity, correctness, and explicit explanations over clever abstractions.

---

## Core Technical Decisions

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Data Redis
- Spring Session Data Redis
- PostgreSQL
- Redis
- Docker / Docker Compose

### Persistence

Use **Spring Data JPA**.

Do **not** introduce Flyway or Liquibase unless explicitly requested later.

For development, database schema management may use Hibernate/JPA configuration appropriate for the learning stage.

PostgreSQL is the source of truth for users, roles, and permissions.

### Authentication

Authentication is **session-based**, not JWT-based.

The intended flow is:

1. User submits email and password.
2. Spring Security authenticates the credentials.
3. User roles and permissions are loaded from PostgreSQL.
4. Spring Security creates the authenticated `Authentication`.
5. The `SecurityContext` is stored in the HTTP session.
6. Spring Session persists that session in Redis.
7. The generated session ID is returned to the client.

For browser clients, use an HTTP-only session cookie.

For native/mobile clients, header-based session transport may be added in a later milestone.

### Authorization

Use database-backed RBAC.

Expected model:

- User
- Role
- Permission
- User ↔ Role
- Role ↔ Permission

Roles should become Spring Security authorities such as:

```text
ROLE_USER
ROLE_ADMIN
```

Permissions should become authorities such as:

```text
user:read
user:create
user:update
user:delete
role:assign
```

Authorization should eventually demonstrate both:

```java
hasRole("ADMIN")
```

and:

```java
hasAuthority("user:delete")
```

including method security via `@PreAuthorize`.

---

## Redis Session Rules

Redis stores **active authentication/session state**.

PostgreSQL remains the source of truth.

Use Spring Session rather than creating a custom Redis session implementation unless the learning milestone explicitly requires studying the lower-level behavior.

The intended mental model is:

```text
PostgreSQL = persistent identity + authorization source of truth
Redis      = active session/authentication state
```

When user roles or permissions change:

1. Update PostgreSQL first.
2. Check whether the user currently has active Redis-backed sessions.
3. If no active session exists, no Redis update is required.
4. If active sessions exist, synchronize or invalidate them according to the current milestone's chosen strategy.
5. The next login must always load the latest roles and permissions from PostgreSQL.

Security-critical changes such as account disabling, password changes, or removing privileged access may invalidate active sessions rather than updating them in place.

---

## Agent Guidelines

### Learning-first implementation

Do not skip directly to large amounts of code.

When introducing a Spring Security concept:

1. Explain what problem it solves.
2. Explain where it sits in the request/authentication flow.
3. Relate it to familiar Express.js concepts when useful.
4. Show a small example.
5. Only then integrate it into the project.

Avoid treating Spring Security as magic configuration.

Important concepts should be explained explicitly, especially:

- `SecurityFilterChain`
- Servlet filters
- `SecurityContext`
- `SecurityContextHolder`
- `Authentication`
- `AuthenticationManager`
- `AuthenticationProvider`
- `DaoAuthenticationProvider`
- `UserDetails`
- `UserDetailsService`
- `GrantedAuthority`
- `PasswordEncoder`
- `HttpSession`
- Spring Session
- `RedisIndexedSessionRepository`
- `@PreAuthorize`

### Do not use JWT by default

This project is intentionally focused on server-side sessions.

Do not replace session authentication with JWT access/refresh tokens unless explicitly requested.

### Do not use Flyway

Do not add:

- Flyway
- Liquibase

unless explicitly requested later.

Use Spring Data JPA/Hibernate for the current learning project.

### Prefer framework-native solutions

Prefer learning and using Spring-native components before writing custom replacements.

Examples:

- Spring Security for authentication/authorization
- Spring Session for Redis-backed sessions
- Spring Data JPA for persistence
- `PasswordEncoder` for password hashing

Custom infrastructure should only be introduced when it teaches an important concept or solves a project requirement that Spring does not already address cleanly.

### Security requirements

Never:

- store plaintext passwords;
- log passwords;
- expose password hashes through API responses;
- trust client-supplied roles or permissions;
- treat Redis as the permanent authorization source of truth.

Use an appropriate Spring Security `PasswordEncoder`.

### Code style

Prefer:

- clear package boundaries;
- constructor injection;
- DTOs for API request/response payloads;
- service-layer business logic;
- repositories only for persistence operations;
- small, focused classes;
- descriptive names;
- explicit transactions where consistency matters.

Avoid overengineering while the project is still teaching fundamentals.

---

## Suggested Package Structure

The package structure can evolve, but a reasonable target is:

```text
src/main/java/.../
├── config/
│   └── security/
├── auth/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   └── security/
├── user/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
├── role/
│   ├── entity/
│   ├── repository/
│   └── service/
├── permission/
│   ├── entity/
│   ├── repository/
│   └── service/
└── session/
    └── service/
```

This is guidance, not a rigid requirement.

---

## Milestone Rule

Implement the project incrementally.

Do not jump ahead and wire Redis, RBAC, method authorization, session indexing, and cache synchronization all at once.

Each milestone should leave the project runnable and should teach one clear Spring Security concept before adding the next layer.

See `.agents/skills/spring-security-skill.md` for the detailed architecture, goals, and milestone plan.
