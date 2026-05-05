# Jobs API

Spring Boot REST API for managing jobs, temps, authentication, authorisation, availability checks, and job assignment workflows.

## Tech stack

- Java 21
- Spring Boot 3.5.10
- Spring Web
- Spring Security
- Spring Data JPA
- MySQL
- H2 for tests
- JWT via `jjwt`
- Bean validation
- SpringDoc OpenAPI
- Rest Assured E2E tests
- Maven

## Main responsibilities

- Authenticate temps through `/auth/login`.
- Store JWT authentication in an HTTP-only cookie.
- Provide CSRF tokens through `/csrf/csrf-token`.
- Protect all business endpoints.
- Return only jobs and temps visible to the logged-in temp.
- Prevent temps from being assigned to overlapping jobs.
- Support job creation, updates, assignment, unassignment, pagination, sorting, and filtering.
- Support temp creation, profile updates, temp updates, availability listing, pagination, and sorting.
- Return consistent JSON error responses.

## Prerequisites

- Java 21
- MySQL running locally
- A database user with permission to create/use the configured database

## Environment variables

Create a `.env` file in the API project root:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=jobs_db
DB_USER=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
JWT_COOKIE_NAME=jwt
JWT_TOKEN_EXPIRY=86400000
AUTH_USERNAME=admin
AUTH_PASSWORD=admin12345
CORS_ALLOWED_ORIGINS=http://localhost:5173
SERVER_PORT=8080
```

`JWT_SECRET` must be at least 256 bits for HS256 signing. A short value can stop the application from starting.

## Install and run

From the API project folder:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```bash
./mvnw.cmd spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

## Development data

With the `dev` profile active, the API seeds:

- One admin user: `admin@example.com` / `admin12345`
- Managers reporting to admin
- Employees reporting to managers
- Jobs with future date ranges
- Some assigned and unassigned jobs

Generated temps use the default password:

```text
password12345
```

## Scripts and commands

```bash
./mvnw clean test -Dspring.profiles.active=test
./mvnw spring-boot:run
./mvnw clean package
```

## Authentication and CSRF

Login returns `204 No Content` and sets the JWT cookie.

```http
POST /auth/login
```

Request body:

```json
{
  "username": "admin@example.com",
  "password": "admin12345"
}
```

Unsafe requests require a CSRF token. Fetch one first:

```http
GET /csrf/csrf-token
```

The response contains the token and header name. Send that token in the returned header name for `POST`, `PATCH`, `PUT`, and `DELETE` requests.

## API endpoints

### Auth

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/login` | Logs in and sets the JWT cookie |
| `POST` | `/auth/logout` | Clears the JWT cookie |
| `GET` | `/csrf/csrf-token` | Returns a CSRF token |
| `GET` | `/health` | Health check |

### Jobs

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/jobs` | Creates a job |
| `PATCH` | `/jobs/{id}` | Updates a job, assigns a temp, or unassigns a temp |
| `GET` | `/jobs` | Lists visible jobs with pagination and sorting |
| `GET` | `/jobs?assigned=true` | Lists assigned visible jobs |
| `GET` | `/jobs?assigned=false` | Lists unassigned visible jobs |
| `GET` | `/jobs/{id}` | Returns a visible job by ID |

Supported list parameters:

| Parameter | Values | Default |
|---|---|---|
| `assigned` | `true`, `false` | none |
| `sortBy` | `date`, `name` | `date` |
| `sortDir` | `asc`, `desc` | `asc` |
| `page` | number | `0` |
| `size` | number | `10` |

Example job update body:

```json
{
  "tempId": 5
}
```

To unassign a job, send `tempId` as `null` if supported by the current service implementation:

```json
{
  "tempId": null
}
```

### Temps

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/temps` | Creates a temp |
| `GET` | `/temps/me` | Returns the current logged-in temp |
| `PATCH` | `/temps/me` | Updates the current logged-in temp |
| `GET` | `/temps` | Lists visible temps |
| `GET` | `/temps?jobId={jobId}` | Lists temps available for a job |
| `GET` | `/temps/{id}` | Returns a visible temp and their assigned jobs |
| `PATCH` | `/temps/{id}` | Updates a visible temp |

Supported list parameters:

| Parameter | Values | Default |
|---|---|---|
| `jobId` | number | none |
| `sortBy` | `id`, `name`, `jobCount` | `name` |
| `sortDir` | `asc`, `desc` | `asc` |
| `page` | number | `0` |
| `size` | number | `10` |

## Response examples

Job response:

```json
{
  "id": 1,
  "name": "Site Supervisor",
  "startDate": "2026-04-20",
  "endDate": "2026-04-22",
  "temp": {
    "id": 4,
    "firstName": "Alex",
    "lastName": "Taylor"
  }
}
```

Paged response:

```json
{
  "items": [],
  "page": 0,
  "size": 10,
  "totalItems": 0,
  "totalPages": 0,
  "hasNext": false,
  "hasPrevious": false
}
```

Error response:

```json
{
  "timestamp": "2026-05-05T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/jobs",
  "validationErrors": []
}
```

## Swagger/OpenAPI

When the API is running, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## CI

The GitHub Actions workflow runs:

```bash
mvn clean test -Dspring.profiles.active=test
```

on pull requests and pushes to `main` or `master`.

## Troubleshooting

### API fails on JWT startup

Use a long enough `JWT_SECRET`. For HS256, use at least 32 bytes, for example a 64-character hex string.

### UI cannot call API

Check:

- API is running on `8080`.
- `CORS_ALLOWED_ORIGINS` includes `http://localhost:5173`.
- Browser requests use credentials.
- CSRF token has been fetched before unsafe requests.

### MCP gets 401 or 403 from API

Check:

- User is logged in through the UI.
- UI forwards cookies to MCP.
- MCP forwards the JWT cookie and `X-XSRF-TOKEN` to the API.
