# MyTodoWebApp

A Spring Boot todo application with a same-origin HTML/CSS/JavaScript frontend, a MySQL database, and session-based authentication.

## Requirements

- Java 17
- MySQL

## Local setup

1. Create a MySQL database and a dedicated application account with access only to that database.
2. Copy `.env.example` to `.env` and fill in your local values. Do not commit `.env`.
3. Export those values in your shell before starting the app. Spring Boot reads environment variables directly; it does not automatically load `.env`.
4. Run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`) and open `http://localhost:8080`.

Required variables are `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Optional TLS can be enabled by setting `TLS_ENABLED=true` with `TLS_KEYSTORE`, `TLS_KEYSTORE_PASSWORD`, `TLS_KEYSTORE_TYPE`, and `TLS_KEY_ALIAS` pointing at externally managed key material. The application no longer includes a keystore. For production, terminate TLS at a trusted proxy or provide a managed keystore and enable TLS.

The browser UI and API are served from the same origin. Session cookies and CSRF protection are enabled; the frontend obtains and sends the CSRF token automatically. No cross-origin API access is configured.

## API

Base path: `/api`

- `POST /register` — register; returns a safe user DTO (201, or 409 for a duplicate account)
- `POST /login` — authenticate and establish a server-side session (401 for invalid credentials)
- `POST /logout` — invalidate the session
- `GET /me` — return the signed-in user's safe profile
- `GET /user/{id}` — return the current user's profile only
- `GET /tasks` — list the signed-in user's tasks
- `POST /task` — create a task owned by the signed-in user
- `PUT /task/{id}` — update one of the signed-in user's tasks
- `DELETE /task/{id}` — delete one of the signed-in user's tasks

Mutating requests require the `X-XSRF-TOKEN` header matching the `XSRF-TOKEN` cookie. The frontend handles this automatically.

## Configuration

| Variable | Required | Description |
| --- | --- | --- |
| `DB_URL` | Yes | JDBC URL, for example `jdbc:mysql://localhost:3306/todos` |
| `DB_USERNAME` | Yes | Database account name |
| `DB_PASSWORD` | Yes | Database account password |
| `SERVER_PORT` | No | HTTP or HTTPS listener port (default `8080`) |
| `TLS_ENABLED` | No | Set `true` to enable embedded TLS |
| `TLS_KEYSTORE` | For embedded TLS | Filesystem path to externally managed PKCS#12 keystore |
| `TLS_KEYSTORE_PASSWORD` | For embedded TLS | Keystore password |
| `TLS_KEYSTORE_TYPE` | No | Keystore type (default `PKCS12`) |
| `TLS_KEY_ALIAS` | For embedded TLS | Certificate alias |

## Build and tests

Run `./mvnw verify` (Windows: `mvnw.cmd verify`). Tests use an in-memory H2 database and do not need MySQL credentials.

