# Number Guessing Game API

A simple REST API for a number guessing game built with Spring Boot.

## Technologies Used

- Java 17
- Spring Boot 3.5.4
- Spring Security with JWT Authentication
- PostgreSQL
- Docker

## Prerequisites

- JDK 17+
- Docker and Docker Compose

## Setup and Run

1. Clone the repository
2. Start the PostgreSQL database:
   ```
   docker-compose up -d
   ```
3. Run the Spring Boot application:
   ```
   ./gradlew bootRun
   ```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
  - Request Body: `{ "username": "user123", "password": "password123" }`
  - Response: `{ "token": "jwt-token", "username": "user123" }`

- `POST /api/auth/login` - Login an existing user
  - Request Body: `{ "username": "user123", "password": "password123" }`
  - Response: `{ "token": "jwt-token", "username": "user123" }`

### Testing the API

Use the following curl commands to test the API:

**Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

**Accessing protected endpoints:**
```bash
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```