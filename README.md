# Ticketing App

Spring Boot + MongoDB ticketing system with CRUD APIs for internal support workflows. It focuses on managing ticket lifecycles, user access, and SLA policy enforcement in a secure, role-aware backend.

## Project Scope

This project provides a backend API for managing users, tickets, SLA policies, and complaint categories with JWT-based security. It covers ticket creation, assignment, status transitions, internal comments, and SLA timing, while keeping authorization rules centralized for admins and non-admin users.

## Technologies

- Java 17
- Spring Boot
- Spring Security (JWT)
- Spring Data MongoDB
- Gradle
- Swagger / OpenAPI

## Features

- Authentication (register, login, refresh)
- User management CRUD
- Ticket management CRUD with comments, assignments, and status changes
- SLA policy management CRUD
- Complaint category management CRUD
- Role-aware access controls (ADMIN vs non-admin)

## Run

```bash
./gradlew bootRun
```

## Test

```bash
./gradlew test
```

## Security Notes

- Use `Authorization: Bearer <accessToken>` for secured endpoints.
- ADMIN can access all tickets; non-admin users only access their own tickets.
- Only ADMIN can create complaint categories.

## API Docs

- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Notes

- User passwords are hashed with BCrypt before storage.
- JWT settings live under `security.jwt` in `src/main/resources/application.yaml`.
- Ticket creation calculates an SLA deadline using the configured SLA policy, with a sensible fallback if no policy exists yet.
