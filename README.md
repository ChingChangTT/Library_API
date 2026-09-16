# Library and Skincare API

A Spring Boot 4 REST API demonstrating a production-style controller/service/repository
architecture, request validation, pagination, database migrations, consistent errors,
and integration testing.

## Run locally

Requirements: Java 21. The default `dev` profile uses an in-memory H2 database and
loads sample records.

```powershell
.\mvnw.cmd spring-boot:run
```

Open Swagger UI at <http://localhost:8082/docs>. The raw OpenAPI document is at
<http://localhost:8082/api-docs>.

## Example requests

```http
GET /api/books?page=0&size=20&sort=title,asc
GET /api/books?keyword=spring
GET /api/skincare-products?keyword=serum&page=0&size=10
```

```http
POST /api/books
Content-Type: application/json

{
  "title": "Domain-Driven Design",
  "author": "Eric Evans",
  "isbn": "9780321125217",
  "publishedYear": 2003
}
```

```http
POST /api/books/1/borrow
Content-Type: application/json

{ "borrowerName": "Ada Lovelace" }
```

Validation failures have one predictable shape:

```json
{
  "timestamp": "2026-09-06T08:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/books",
  "fieldErrors": {
    "isbn": "must contain 10 or 13 digits"
  }
}
```

## Production profile

The `prod` profile uses PostgreSQL. Supply the connection through environment
variables instead of committing credentials:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://localhost:5432/demo"
$env:DB_USERNAME="demo"
$env:DB_PASSWORD="change-me"
.\mvnw.cmd spring-boot:run
```

Flyway applies schema migrations from `src/main/resources/db/migration`.

## Test

```powershell
.\mvnw.cmd test
```

The integration suite covers pagination, searching, creation, borrowing, validation,
and deletion.

## Persistence

Books use Spring JDBC (`JdbcTemplate`); skincare products use Spring Data JPA.
Flyway creates both tables and loads sample data in the `dev` profile.
Spring SQL script initialization is disabled so `schema.sql` does not duplicate
the Flyway schema. The development H2 database is in memory, so its data is
reset when the application stops.
