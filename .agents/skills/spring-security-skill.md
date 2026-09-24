---
name: spring-security-skill
description: A skill that teaches Spring Security from first principles, focusing on session-based authentication and authorization with Redis and PostgreSQL.
license: MIT
compatibility: Requires java 25 or later, spring boot 4.1.1 or later and spring security 7.1.0 or later.
metadata:
  author: Kavinda Rathnayake
  version: "1.0.0"
---

# Spring Security Skill

## Purpose

This document defines the architecture, goals, constraints, learning strategy, and implementation roadmap for the Spring
Security learning project.

The project exists to teach **Spring Security from first principles** by building a practical session-based
authentication and authorization system.

The target architecture intentionally resembles a traditional Express.js server-side authentication model:

- email/password authentication;
- Redis-backed server sessions;
- PostgreSQL-backed users, roles, and permissions;
- database-backed RBAC;
- role and permission changes reflected in active sessions;
- browser cookie support;
- optional native/mobile session-token support later.

The objective is not merely to make login work. The objective is to understand how Spring Security works internally and
how its parts cooperate.

---

# 1. Project Goals

By the end of the project, the developer should understand:

1. How an HTTP request enters a Spring Boot servlet application.
2. Where the Spring Security filter chain runs.
3. How Spring Security authenticates credentials.
4. What `Authentication` represents.
5. What the `SecurityContext` and `SecurityContextHolder` do.
6. How `UserDetailsService` participates in username/password authentication.
7. How `AuthenticationManager` and `AuthenticationProvider` work.
8. How password verification is handled safely.
9. How Spring Security represents roles and permissions using `GrantedAuthority`.
10. How request-level and method-level authorization work.
11. How Spring Security persists authentication in an HTTP session.
12. How Spring Session replaces local container session storage.
13. How Redis stores active Spring sessions.
14. How to find active sessions belonging to a particular user.
15. How to revoke sessions.
16. How to update or invalidate active session authorization state when roles or permissions change.
17. How browser cookie sessions differ from header-based session transport.
18. How to secure a real session-based application against common threats.

---

# 2. Non-Goals

The initial project is **not** intended to teach:

- JWT authentication;
- OAuth2 login;
- OpenID Connect;
- authorization servers;
- microservice token propagation;
- WebFlux security;
- distributed JWT revocation;
- Flyway or Liquibase database migrations.

These may be explored in separate projects later.

---

# 3. Technical Stack

## Application

- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Data Redis
- Spring Session Data Redis
- Bean Validation

## Infrastructure

- PostgreSQL
- Redis
- Docker
- Docker Compose

## Persistence Rule

Use **Spring Data JPA** and Hibernate.

Do not introduce Flyway.

PostgreSQL stores permanent application data.

Redis stores temporary active session state.

---

# 4. High-Level Architecture

```text
                  ┌──────────────────────────┐
                  │         Client           │
                  │                          │
                  │ Browser / Mobile Client  │
                  └────────────┬─────────────┘
                               │
                               │ session identifier
                               ▼
┌─────────────────────────────────────────────────────────┐
│                   Spring Boot API                       │
│                                                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │           Spring Security Filter Chain            │  │
│  │                                                   │  │
│  │ Authentication → SecurityContext → Authorization │  │
│  └───────────────────────┬───────────────────────────┘  │
│                          │                              │
│              ┌───────────┴───────────┐                  │
│              │                       │                  │
│              ▼                       ▼                  │
│       PostgreSQL                Spring Session          │
│                                                         │
│  users                        Redis                     │
│  roles                 ┌────────────────────────────┐   │
│  permissions           │ session id                 │   │
│  user_roles            │ timestamps                 │   │
│  role_permissions      │ SecurityContext            │   │
│                        │ Authentication              │   │
│                        │ authorities                 │   │
│                        └────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

The key architectural rule is:

```text
PostgreSQL = source of truth
Redis      = active authentication/session state
```

---

# 5. Authentication Flow

The desired login flow is:

```text
POST /api/auth/login
        │
        ▼
email + password
        │
        ▼
AuthenticationManager
        │
        ▼
AuthenticationProvider
        │
        ├── UserDetailsService
        │       │
        │       ▼
        │   PostgreSQL
        │
        └── PasswordEncoder
                │
                ▼
         verify password
                │
                ▼
       authenticated Authentication
                │
                ▼
          SecurityContext
                │
                ▼
             HttpSession
                │
                ▼
           Spring Session
                │
                ▼
              Redis
                │
                ▼
        session identifier
```

For browsers, the session identifier should be returned in an HTTP-only cookie.

For native/mobile applications, header-based session-ID transport may be implemented later.

---

# 6. Important Spring Security Concepts

## 6.1 SecurityFilterChain

The servlet request passes through filters before reaching controllers.

Spring Security inserts a filter chain that performs tasks including:

- loading existing authentication;
- processing login/authentication mechanisms;
- handling security exceptions;
- enforcing authorization;
- persisting the security context.

Mental comparison:

```text
Express middleware chain
        ≈
Spring Security filter chain
```

The comparison is useful conceptually, although the implementations differ.

---

## 6.2 Authentication

`Authentication` represents the current authentication state.

Conceptually:

```text
Authentication
├── principal
├── credentials
├── authenticated
└── authorities
```

After successful authentication it may represent:

```text
principal:
  user id
  email
  account information

authorities:
  ROLE_ADMIN
  user:read
  user:update
  user:delete
```

---

## 6.3 SecurityContext

The `SecurityContext` contains the current `Authentication`.

```text
SecurityContext
└── Authentication
```

Spring Security exposes it through `SecurityContextHolder`.

A rough Express mental model is:

```text
req.user
```

but Spring Security's context is more structured and integrated with its filters and authorization system.

---

## 6.4 UserDetails

`UserDetails` adapts an application user into the format Spring Security expects for username/password authentication.

The custom implementation may expose:

```text
CustomUserDetails
├── id
├── email
├── password hash
├── enabled/account flags
└── authorities
```

Do not expose the password hash through controllers or API DTOs.

---

## 6.5 UserDetailsService

`UserDetailsService` loads a user for authentication.

Typical operation:

```text
loadUserByUsername(email)
```

Project behavior:

```text
Spring Security
      │
      ▼
Custom UserDetailsService
      │
      ▼
UserRepository
      │
      ▼
PostgreSQL
      │
      ├── User
      ├── Roles
      └── Permissions
```

---

## 6.6 AuthenticationManager

`AuthenticationManager` is the main abstraction used to request authentication.

Conceptually:

```text
authenticate(credentials)
```

For email/password authentication, the request eventually reaches an appropriate `AuthenticationProvider`.

---

## 6.7 AuthenticationProvider

An `AuthenticationProvider` performs authentication for a particular kind of credential.

For database-backed username/password login, Spring Security commonly uses `DaoAuthenticationProvider`.

It cooperates with:

- `UserDetailsService`
- `PasswordEncoder`

---

## 6.8 PasswordEncoder

Passwords must never be stored in plaintext.

The project must use Spring Security's password encoding abstractions.

The database stores only password hashes.

Registration:

```text
raw password
    │
    ▼
PasswordEncoder
    │
    ▼
password hash
    │
    ▼
PostgreSQL
```

Login:

```text
submitted password
       +
stored password hash
       │
       ▼
PasswordEncoder.matches(...)
```

---

## 6.9 GrantedAuthority

Spring Security represents authorization privileges with `GrantedAuthority`.

This project will use both roles and permissions as authorities.

Roles:

```text
ROLE_USER
ROLE_ADMIN
ROLE_MODERATOR
```

Permissions:

```text
profile:read
profile:update

user:read
user:create
user:update
user:delete

role:read
role:create
role:assign

permission:read
```

---

# 7. Database Model

## User

```text
users
────────────────────────────
id              UUID PK
email           VARCHAR UNIQUE
password_hash   VARCHAR
name            VARCHAR
enabled         BOOLEAN
created_at
updated_at
```

Possible future account state fields:

```text
account_locked
credentials_expired
account_expired
```

These should only be added when the corresponding Spring Security concept is being studied.

---

## Role

```text
roles
────────────────────────────
id              UUID PK
name            VARCHAR UNIQUE
created_at
updated_at
```

Examples:

```text
USER
ADMIN
MODERATOR
```

---

## Permission

```text
permissions
────────────────────────────
id              UUID PK
name            VARCHAR UNIQUE
created_at
updated_at
```

Examples:

```text
user:read
user:create
user:update
user:delete
role:read
role:assign
```

---

## UserRole

Many-to-many relationship:

```text
user_roles
────────────────────────────
user_id
role_id
```

---

## RolePermission

Many-to-many relationship:

```text
role_permissions
────────────────────────────
role_id
permission_id
```

---

# 8. RBAC Model

Example:

```text
User: alice@example.com
│
├── ROLE_USER
│   ├── profile:read
│   └── profile:update
│
└── ROLE_ADMIN
    ├── user:read
    ├── user:create
    ├── user:update
    └── user:delete
```

Spring Security authorities:

```text
ROLE_USER
ROLE_ADMIN
profile:read
profile:update
user:read
user:create
user:update
user:delete
```

The application should eventually demonstrate:

```java
@PreAuthorize("hasRole('ADMIN')")
```

and:

```java
@PreAuthorize("hasAuthority('user:delete')")
```

---

# 9. Session Architecture

Authentication is session-based.

Do not use JWT as the default authentication strategy.

After successful authentication:

```text
Authentication
     │
     ▼
SecurityContext
     │
     ▼
HttpSession
     │
     ▼
Spring Session
     │
     ▼
Redis
```

Spring Session should manage the session representation instead of building a custom Redis object such as:

```json
{
  "roles": [],
  "permissions": []
}
```

Conceptually, the Redis-backed session contains:

```text
Session
├── session id
├── creation time
├── last accessed time
├── expiration data
└── attributes
    └── Spring Security context
        └── Authentication
            ├── principal
            └── authorities
```

---

# 10. Session Identifier

A generated opaque session ID is sent to clients.

Conceptually:

```text
SESSION=<opaque-generated-session-id>
```

For browsers:

```text
Set-Cookie: SESSION=...
```

Expected production cookie properties include:

```text
HttpOnly
Secure
appropriate SameSite policy
```

Exact settings depend on deployment topology and frontend/backend origin strategy.

---

# 11. Browser and Mobile Clients

## Browser

Use cookie-based session transport.

Typical request:

```text
Cookie: SESSION=<session-id>
```

The browser automatically sends the cookie according to cookie policy.

---

## Mobile / Native

A later milestone may implement header-based session identification.

Example concept:

```text
X-Auth-Token: <session-id>
```

Do not enable multiple session-ID transport mechanisms casually.

Security consequences must be understood before implementation.

---

# 12. Role and Permission Synchronization

This is a core project requirement.

Suppose an authenticated user's active Redis session currently contains:

```text
ROLE_ADMIN
user:read
user:update
```

An administrator grants:

```text
user:delete
```

PostgreSQL then contains the new state, but the active session may still contain the old `Authentication`.

The system must handle this intentionally.

---

## 12.1 Update Rules

When roles or permissions change:

```text
Update PostgreSQL
       │
       ▼
Find active sessions for the affected user
       │
       ├── no sessions
       │      └── done
       │
       └── active sessions
              │
              ▼
       refresh or invalidate
```

PostgreSQL must be updated first.

If the user is logged out, only PostgreSQL needs changing.

On the next login, the current roles and permissions are loaded from PostgreSQL and stored in the new authenticated
session.

---

## 12.2 Strategy A: Session Invalidation

Simplest approach:

```text
authorization changed
       │
       ▼
update database
       │
       ▼
delete user's active sessions
       │
       ▼
user authenticates again
```

This is especially appropriate for security-sensitive changes:

- account disabled;
- password changed;
- privileged role removed;
- administrator access revoked;
- suspicious account activity.

---

## 12.3 Strategy B: Active Session Refresh

This more closely matches the intended learning goal:

```text
authorization changed
       │
       ▼
update PostgreSQL
       │
       ▼
load current roles + permissions
       │
       ▼
find active sessions
       │
       ▼
replace/update authenticated authorities
       │
       ▼
persist session state back to Redis
```

The project should eventually implement and study this strategy.

The implementation must preserve consistency and should not manually modify random Redis keys behind Spring Session's
back.

---

# 13. Session Indexing

To synchronize or revoke sessions for a specific user, the application needs to locate that user's active sessions.

Use Spring Session's indexed Redis session support.

Conceptually:

```text
principal = alice@example.com

active sessions:
├── session A
├── session B
└── session C
```

This later enables functionality such as:

```text
GET /api/auth/sessions
DELETE /api/auth/sessions/{sessionId}
DELETE /api/auth/sessions
```

Possible UI use cases:

```text
Chrome on Linux      current
Firefox on Linux
Android
```

Device metadata is not required initially and may be added later.

---

# 14. API Target

## Authentication

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
```

---

## Users

```text
GET    /api/users
GET    /api/users/{id}
PATCH  /api/users/{id}
DELETE /api/users/{id}
```

---

## Roles

```text
GET    /api/roles
POST   /api/roles
PATCH  /api/roles/{id}
DELETE /api/roles/{id}
```

---

## Permissions

```text
GET  /api/permissions
POST /api/permissions
```

---

## Role Permissions

```text
PUT /api/roles/{id}/permissions
```

---

## User Roles

```text
PUT /api/users/{id}/roles
```

---

## Sessions

```text
GET    /api/auth/sessions
DELETE /api/auth/sessions/{sessionId}
```

A "revoke all other sessions" endpoint may be added later.

---

# 15. Suggested Package Architecture

A possible structure is:

```text
src/main/java/com/example/security/
│
├── config/
│   └── security/
│
├── auth/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   └── security/
│
├── user/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
│
├── role/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
│
├── permission/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
│
└── session/
    ├── service/
    └── dto/
```

The architecture may evolve as concepts are learned.

Avoid creating abstractions before the project actually needs them.

---

# 16. Infrastructure

Redis and PostgreSQL run in Docker containers.

Conceptual Compose setup:

```text
Docker Compose
│
├── PostgreSQL
│   └── persistent application data
│
└── Redis
    └── active sessions
```

During learning, running Spring Boot directly from the development environment is preferred because debugging is easier.

The Spring Boot application can be containerized later.

---

# 17. Data Ownership

## PostgreSQL owns

```text
users
password hashes
roles
permissions
user-role relationships
role-permission relationships
account status
```

## Redis owns

```text
active session state
session expiration
Spring Security context
temporary authenticated authorities
```

Never rely on Redis as the permanent authorization source of truth.

---

# 18. Security Principles

## Passwords

Never:

```text
store plaintext passwords
log passwords
return password hashes
```

Always use an appropriate Spring Security password encoder.

---

## Authorization

Never trust roles or permissions supplied by the client.

Authorization decisions must use server-controlled authenticated authorities.

---

## Session Security

The project should eventually cover:

- session fixation protection;
- CSRF;
- CORS;
- session expiration;
- logout;
- concurrent sessions;
- session revocation;
- secure cookies;
- `HttpOnly`;
- `Secure`;
- `SameSite`;
- account disabling;
- password changes;
- permission revocation.

---

# 19. Learning Method

For every new Spring Security component, use this sequence:

```text
1. What problem does it solve?
2. Where does it run?
3. What goes into it?
4. What comes out of it?
5. How does it connect to the previous component?
6. What is the closest Express.js mental model?
7. Build the smallest working example.
8. Inspect the behavior.
9. Integrate it into the project.
```

Do not present large configuration files without explaining each important part.

Whenever possible, trace one request end-to-end.

---

# 20. Milestones

## Milestone 1 — Spring Security Fundamentals

Goal:

Understand the Spring Security architecture before adding database authentication.

Study:

- servlet request lifecycle;
- filters;
- `SecurityFilterChain`;
- `SecurityContext`;
- `SecurityContextHolder`;
- `Authentication`;
- anonymous authentication;
- `AuthenticationManager`;
- `AuthenticationProvider`;
- `UserDetails`;
- `UserDetailsService`;
- `GrantedAuthority`.

Build a minimal secured application.

Do not add Redis yet.

---

## Milestone 2 — Database Authentication

Goal:

Authenticate users stored in PostgreSQL.

Implement:

- `AppUser` JPA entity;
- `UserRepository`;
- registration;
- password hashing;
- custom `UserDetails`;
- custom `UserDetailsService`;
- database-backed login;
- `/api/auth/me`.

Use Spring Data JPA.

Do not use Flyway.

---

## Milestone 3 — Roles and Permissions

Goal:

Understand authorization.

Implement:

- `Role` entity;
- `Permission` entity;
- user-role relationship;
- role-permission relationship;
- authority mapping;
- `hasRole(...)`;
- `hasAuthority(...)`;
- `@EnableMethodSecurity`;
- `@PreAuthorize`.

---

## Milestone 4 — HTTP Session Authentication

Goal:

Understand how Spring Security persists authentication between requests.

Study:

- `HttpSession`;
- session ID;
- security context persistence;
- login/logout lifecycle;
- session fixation protection.

At this stage the session may still use the servlet container's local session store.

---

## Milestone 5 — Redis-Backed Sessions

Goal:

Move active sessions into Redis.

Add:

- Spring Session Data Redis;
- Redis configuration;
- Docker Redis service;
- session inspection;
- expiration configuration.

Verify that authentication survives across requests through Redis-backed session state.

---

## Milestone 6 — Active Session Indexing

Goal:

Find active sessions belonging to a specific authenticated principal.

Implement indexed Redis session support.

Build:

```text
GET /api/auth/sessions
```

Study:

- principal indexing;
- multiple active sessions;
- revoking individual sessions.

---

## Milestone 7 — Permission Synchronization

Goal:

Keep active authenticated sessions consistent with authorization changes.

Implement:

```text
change user role/permission
        │
        ▼
update PostgreSQL
        │
        ▼
find active user sessions
        │
        ▼
refresh or invalidate session authentication
```

Study transactional consistency and failure handling.

---

## Milestone 8 — Session Management

Implement:

```text
list sessions
logout current session
revoke selected session
revoke all sessions
revoke all other sessions
```

Study concurrent session behavior.

---

## Milestone 9 — Browser and Mobile Session Transport

Browser:

```text
Cookie: SESSION=...
```

Native/mobile:

```text
header-based session id
```

Study the security tradeoffs before enabling both.

---

## Milestone 10 — Security Hardening

Study and implement:

- CSRF;
- CORS;
- cookie settings;
- HTTPS assumptions;
- session expiration;
- remember-me concepts, if useful;
- login throttling;
- account locking;
- disabling accounts;
- password changes;
- session invalidation;
- privileged access revocation;
- security-focused tests.

---

# 21. Initial Dependency Direction

The project will eventually need functionality corresponding to:

```text
Spring Web
Spring Security
Spring Data JPA
PostgreSQL Driver
Spring Data Redis
Spring Session Data Redis
Validation
```

Optional development helpers may be added when they improve learning or productivity.

Flyway must not be added.

---

# 22. Development Rules for Agents

When assisting with this project:

1. Preserve session-based authentication.
2. Do not silently switch to JWT.
3. Do not add Flyway.
4. Use Spring Data JPA.
5. Keep PostgreSQL as the authorization source of truth.
6. Use Spring Session for Redis-backed sessions.
7. Prefer explaining Spring Security's built-in mechanisms over recreating them.
8. Introduce one security concept at a time.
9. Keep code runnable at the end of each milestone.
10. Explain important framework behavior before hiding it behind abstractions.
11. Do not overengineer the first implementation.
12. When roles or permissions change, explicitly consider active-session consistency.
13. Treat permission revocation more carefully than permission addition where security impact differs.
14. Never weaken password or session security merely to simplify a demo.

---

# 23. Target Mental Model

By the end of the project, this flow should be fully understandable:

```text
HTTP Request
     │
     ▼
Servlet Filter Chain
     │
     ▼
Spring Security Filter Chain
     │
     ▼
load SecurityContext from session
     │
     ▼
Authentication
     │
     ├── Principal
     └── GrantedAuthorities
     │
     ▼
Authorization decision
     │
     ▼
Controller
```

For login:

```text
credentials
     │
     ▼
AuthenticationManager
     │
     ▼
AuthenticationProvider
     │
     ├── UserDetailsService → PostgreSQL
     └── PasswordEncoder
     │
     ▼
Authentication
     │
     ▼
SecurityContext
     │
     ▼
HttpSession
     │
     ▼
Spring Session
     │
     ▼
Redis
```

For a role or permission change:

```text
authorization update
       │
       ▼
PostgreSQL
       │
       ▼
find active Redis sessions
       │
       ├── none → finish
       │
       └── found
              │
              ▼
       refresh/invalidate
       authentication state
```

That is the architecture this learning project is designed to teach.
