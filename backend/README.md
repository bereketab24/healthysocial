# HealthySocial Backend 🚀

The backend for HealthySocial is a robust Spring Boot 3 REST API designed for high performance, security, and scalability. It handles habit tracking, goal management, social interactions, and community challenges.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.3.5
- **Language**: Java 21 (Temurin)
- **Security**: Spring Security + OAuth2 Resource Server (Keycloak JWT)
- **Database**: PostgreSQL
- **Persistence**: Spring Data JPA / Hibernate
- **Migrations**: Liquibase (YAML format)
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Mapping**: MapStruct
- **Build Tool**: Gradle (Kotlin DSL)

## 📋 Core Modules

- **User Management**: Profile handling and integration with Keycloak IDs.
- **Habit Tracker**: Logic for creating habits, logging daily completion, and calculating streaks.
- **Goals**: Tracking long-term objectives with progress percentages.
- **Social**: Feed system with posts, comments, likes, and user follow relationships.
- **Challenges**: Community-driven challenges with participant tracking and leaderboards.

## 🔐 Security Architecture

The backend operates as a **Stateless Resource Server**.

1. **Authentication**: The client sends a Bearer JWT in the `Authorization` header.
2. **Validation**: The `SecurityConfig` verifies the signature using Keycloak's public keys (JWKS).
3. **Role Mapping**: `KeycloakJwtAuthConverter` extracts roles from the JWT (`realm_access` and `resource_access`) and maps them to Spring Authorities (e.g., `ROLE_USER`, `ROLE_ADMIN`).
4. **Current User Resolution**: The `UserService` resolves the authenticated user by looking up the `sub` (Subject) claim from the JWT in the local PostgreSQL database. If the user doesn't exist locally yet, they are automatically provisioned.

## 🗄️ Database Design

We use a normalized relational schema managed by **Liquibase**. 

### Key Design Patterns:
- **UUIDs for IDs**: All primary keys are UUIDs to prevent ID enumeration and facilitate easier distribution in the future.
- **Audit Fields**: Every table includes `created_at` and `updated_at` for tracking.
- **Unique Constraints**: Used strategically on `likes` (preventing double likes), `follows` (preventing double follows), and `habit_logs` (preventing multiple logs for the same habit on the same day).

## 🚀 Setup and Execution

### Running Locally
1. Ensure you have a PostgreSQL instance and Keycloak running (use root `docker compose up -d postgres keycloak`).
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```

### Running Tests
```bash
./gradlew test
```
*Note: Tests use an in-memory H2 database with PostgreSQL compatibility mode.*

## 📖 API Documentation

Once the server is running, you can explore the API interactively:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

The API is fully documented using Swagger annotations, providing clear descriptions of request/response bodies and status codes.

## 📂 Internal Structure

```text
src/main/java/com/university/healthysocial/
├── config/         # Spring Configuration (Security, OpenAPI, etc.)
├── controller/     # REST Controllers
├── domain/         # JPA Entities and Enums
├── dto/            # Data Transfer Objects (Requests/Responses)
├── exception/      # Global Exception Handling
├── mapper/         # MapStruct interfaces
├── repository/     # JPA Repositories
├── security/       # Custom security utilities
└── service/        # Business Logic
```

## 📐 Design Decisions: Habit Streaks
The habit streak logic is calculated dynamically in `HabitService` based on daily logs. This ensures that the streak is always accurate relative to the current time, rather than storing a stale value in the database.
