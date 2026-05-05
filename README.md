# HealthySocial 🌿

HealthySocial is a modern wellness platform designed to help users build better habits, achieve long-term goals, and connect with a health-conscious community. It combines personal productivity tools with social features to create a holistic environment for lifestyle improvement.

## 🏗️ Architecture Overview

The project is built using a modern decoupled architecture:

- **Frontend**: React 19 SPA with TypeScript, Vite, and Tailwind CSS.
- **Backend**: Spring Boot 3 REST API (Java 21) with Spring Data JPA.
- **Identity Provider**: Keycloak (OIDC) for secure authentication and user management.
- **Database**: PostgreSQL for persistent storage.
- **Migrations**: Liquibase for version-controlled database schema.
- **Containerization**: Docker Compose for orchestrating all services.

## 🚀 Getting Started

### Prerequisites

- [Docker](https://www.docker.com/products/docker-desktop/) and Docker Compose.
- [Node.js](https://nodejs.org/) (v20+ recommended) for local frontend development.
- [Java 21](https://adoptium.net/temurin/releases/?version=21) and Gradle for local backend development.

### Running the Entire Stack (Docker)

1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd healthysocial
   ```

2. **Build the backend**:
   ```bash
   cd backend
   ./gradlew build
   cd ..
   ```

3. **Start all services**:
   ```bash
   docker compose up -d
   ```
   This will start:
   - **PostgreSQL** on port `5432`
   - **Keycloak** on port `8180` (with pre-configured realm)
   - **Spring Boot API** on port `8080`

4. **Access the Application**:
   - The backend API will be available at `http://localhost:8080`.
   - Swagger UI: `http://localhost:8080/swagger-ui.html`.
   - Keycloak Admin: `http://localhost:8180` (Credentials: `admin`/`admin`).

### Local Development

For the best development experience, it is recommended to run services locally:

1. **Start Infrastructure**: Run only Postgres and Keycloak via Docker.
   ```bash
   docker compose up -d postgres keycloak
   ```
2. **Backend**: Run the Spring Boot application from your IDE or via terminal:
   ```bash
   cd backend
   ./gradlew bootRun
   ```
3. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Open `http://localhost:5173` in your browser.

## 📐 Design Decisions

### 1. Security First with Keycloak
Instead of building a custom auth system, we utilize **Keycloak**. This provides:
- **OIDC/OAuth2** compliance out of the box.
- Secure password storage and session management.
- Scalability for future features like Social Login (Google, Apple) or MFA.
- **Stateless Backend**: The Spring Boot API verifies JWTs locally, ensuring high performance and scalability.

### 2. Versioned Database Schema
We use **Liquibase** to manage the database schema. Every change is a "changeset" in a YAML migration file. This ensures:
- Consistency across development environments.
- Easy rollbacks and automated migrations during deployment.
- **Reproducible Seed Data**: Demo users, habits, and posts are automatically loaded on first run.

### 3. Modern Java stack
- **Java 21**: Leveraging modern language features like Records for DTOs and Pattern Matching.
- **Spring Boot 3.3**: The latest stable framework for production-ready services.
- **MapStruct**: Type-safe mapping between Entities and DTOs, reducing boilerplate and potential errors.

### 4. Responsive & Performant Frontend
- **Vite**: Used over CRA for significantly faster build and reload times.
- **Tailwind CSS**: Utility-first styling for rapid UI development and a consistent design system.
- **Axios Interceptors**: Automatically attaches the Keycloak token to every request and handles token refreshing seamlessly.

## 📂 Project Structure

```text
healthysocial/
├── backend/            # Spring Boot Application
│   ├── src/            # Java source and resources
│   ├── keycloak/       # Realm configuration for import
│   └── build.gradle.kts
├── frontend/           # React Application
│   ├── src/            # React components, pages, hooks
│   └── package.json
└── compose.yaml        # Docker orchestration
```

---
Happy Coding! 🏃‍♂️🥗🧘‍♀️
