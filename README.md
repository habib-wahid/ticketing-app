# Ticketing App

Spring Boot + MongoDB ticketing system with basic CRUD APIs for:

- Users
- Tickets
- SLA policies

## Run

```bash
./gradlew bootRun
```

## Test

```bash
./gradlew test
```

## API Endpoints

### Users
- `GET /api/users`
- `GET /api/users/{userId}`
- `POST /api/users`
- `PUT /api/users/{userId}`
- `DELETE /api/users/{userId}`

### Tickets
- `GET /api/tickets`
- `GET /api/tickets/{ticketId}`
- `POST /api/tickets`
- `PUT /api/tickets/{ticketId}`
- `DELETE /api/tickets/{ticketId}`

### SLA Policies
- `GET /api/sla-policies`
- `GET /api/sla-policies/{id}`
- `POST /api/sla-policies`
- `PUT /api/sla-policies/{id}`
- `DELETE /api/sla-policies/{id}`

## API Docs

- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Notes

- User passwords are hashed with BCrypt before storage.
- Ticket creation calculates an SLA deadline using the configured SLA policy, with a sensible fallback if no policy exists yet.
