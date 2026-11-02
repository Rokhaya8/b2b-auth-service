# B2B Auth Service

Authentication and account management module of a B2B e-commerce platform, built with
**Spring Boot**, **Spring Security** and **JWT**. Companies can register, log in, retrieve
their profile, change their password, and reset a forgotten password through a
time-limited token.

> **Team project.** This module comes from a B2B platform built by a team of about ten
> students (ENSA Marrakech, 2025): products, cart, orders, payment, delivery, reviews and
> statistics were developed by other team members. **Authentication and account
> management was my part.** This repository extracts it as a
> [standalone service](#standalone-version) that runs on its own.

## Features

| Endpoint | Access | Description |
|----------|--------|-------------|
| `POST /api/auth/register` | Public | Register a company (unique email and ICE, password hashed with BCrypt) |
| `POST /api/auth/login` | Public | Log in as a company or an administrator, returns a JWT |
| `POST /api/auth/forgot-password` | Public | Generate a reset token valid for 15 minutes and send it by email |
| `POST /api/auth/reset-password` | Public | Set a new password with the reset token (single use) |
| `GET /api/me` | Authenticated | Profile of the logged-in user |
| `PUT /api/account/password` | Authenticated | Change password (current password required) |

## How It Works

```
Client ── POST /api/auth/login ──► AuthController ── checks BCrypt hash ──► returns a JWT
Client ── GET /api/me + "Authorization: Bearer <JWT>" ──► JwtAuthenticationFilter
          ── valid signature and not expired? ──► user identified ──► MeController
```

- **Passwords are never stored in clear**: they are hashed with BCrypt, a one-way
  function with a random salt.
- **JWT**: after login, the server returns a token signed with HMAC-SHA256 containing the
  email, the role (`ADMIN` or `COMPANY`) and an expiry date. The client sends it with each
  request; the server only checks the signature, so no session is stored (stateless API).
- **Password reset**: a random UUID valid for 15 minutes is stored in the database and
  emailed to the user. It is deleted after use. The response to `forgot-password` is the
  same whether the email exists or not, so the endpoint cannot be used to find out which
  emails have an account.
- **Two user types**: administrators (`AdminUser`) and client companies (`Company`).

## Project Structure

```
src/main/java/com/b2b/
├── controller/   AuthController, MeController, AccountController
├── service/      PasswordResetService, AccountService, MailService
├── model/        AdminUser, Company, PasswordResetToken
├── repository/   Spring Data JPA repositories
├── security/     JwtUtil (create / verify tokens), JwtAuthenticationFilter
├── config/       SecurityConfig, JwtConfig, DemoAdminSeeder
├── dto/ auth/    Request and response objects
└── B2bAuthServiceApplication.java
```

## How to Run

**Requirements:** Java 17 or later, and Maven (included in IntelliJ IDEA).

- **IntelliJ IDEA:** open the folder, wait for Maven to download the dependencies, then
  run `B2bAuthServiceApplication`.
- **Command line:** `mvn spring-boot:run`

The API starts on http://localhost:8081 with an in-memory **H2** database: nothing to
install, and the data is reset at each restart.

### Try it

The file [`requests.http`](requests.http) contains every request, ready to run from
IntelliJ (green arrow next to each request): register, log in, `/me` with and without a
token, change password, forgot / reset password, and admin login.

With curl:

```bash
# Register a company
curl -X POST http://localhost:8081/api/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Atlas Distribution","email":"contact@atlas.ma","password":"motdepasse123","ice":"001234567000089"}'

# Log in and copy the "token" from the response
curl -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"contact@atlas.ma","password":"motdepasse123"}'

# Call a protected route
curl http://localhost:8081/api/me -H "Authorization: Bearer <token>"
```

Without a valid token, protected routes answer **401 Unauthorized**.

### Configuration

No secret is written in the code. Everything is read from environment variables, with
default values meant **only for local development**:

| Variable | Purpose | Default |
|----------|---------|---------|
| `PORT` | HTTP port of the API | `8081` |
| `JWT_SECRET` | Key used to sign tokens (at least 32 characters) | development value |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Administrator created at startup | `admin@b2b.local` / `admin12345` |
| `MAIL_ENABLED` | Send real emails | `false`: the reset email is printed in the server console |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP settings | — |

The H2 console is available at http://localhost:8081/h2-console
(JDBC URL `jdbc:h2:mem:b2bauth`, user `sa`, no password).

## Standalone Version

This repository is a standalone version of the module: it runs on its own, with an
in-memory database and no external service. Compared with the team version, security is
enabled (protected routes, JWT checked on each request), configuration comes from
environment variables, and reset emails are printed in the console by default.

## Limitations and Next Steps

- Input validation is minimal (email format, password strength).
- No refresh token or token revocation: logging out only deletes the token on the client.
- No rate limiting on login and forgot-password.
- No automated tests yet.

## Tools

Java 17, Spring Boot, Spring Security, Spring Data JPA, JJWT, BCrypt, H2, Lombok, Maven