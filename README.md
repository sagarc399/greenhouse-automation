# 🌱 Greenhouse Management System

A full-stack automated greenhouse management platform built with **Spring Boot 3** (backend) and **Vue 3** (frontend).

---

## Tech Stack

| Layer       | Technology                                              |
|-------------|--------------------------------------------------------|
| Backend     | Spring Boot 3, Spring Security, Spring Data JPA        |
| Database    | PostgreSQL 16                                          |
| Auth        | OAuth2 Google login                                     |
| API Docs    | Springdoc OpenAPI 2 (Swagger UI)                       |
| i18n        | Spring MessageSource (EN/ES)                           |
| Frontend    | Vue 3, Vite, Pinia, Vue Router, Axios, vue-i18n        |
| Styling     | Tailwind CSS                                           |
| Testing     | JUnit 5, Mockito, Selenium 4                           |
| CI/CD       | GitHub Actions                                         |

---

## Project Structure

```
greenhouse-app/
├── backend/          # Spring Boot project
│   └── src/
│       ├── main/java/com/greenhouse/app/
│       │   ├── entity/       # JPA entities
│       │   ├── repository/   # Spring Data repositories
│       │   ├── service/      # Business logic
│       │   ├── controller/   # REST controllers
│       │   ├── dto/          # Request/response DTOs
│       │   ├── security/     # OAuth2 & Spring Security
│       │   ├── config/       # OpenAPI, Security config
│       │   └── exception/    # Global exception handler
│       └── resources/
│           ├── application.properties
│           └── i18n/         # messages.properties (EN/ES)
├── frontend/         # Vue 3 project
│   └── src/
│       ├── views/            # Page components
│       ├── components/       # Shared UI components
│       ├── stores/           # Pinia stores
│       ├── services/         # Axios API clients
│       ├── router/           # Vue Router
│       └── i18n/             # Translations (EN/ES)
├── .github/workflows/ci.yml  # GitHub Actions CI
└── README.md
```

---

## Setup & Run

### Prerequisites

- Java 17+
- Node.js 20+
- PostgreSQL 16 running locally
- Google OAuth2 credentials (Client ID + Secret)

### 1. Database

```sql
CREATE DATABASE greenhouse_db;
```

### 2. Backend

```bash
cd backend

# Set your Google OAuth2 credentials
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret

mvn spring-boot:run
```

Backend starts at **http://localhost:8080**

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at **http://localhost:5173**

---

## API Endpoints Summary

| Method   | Endpoint                        | Description                         |
|----------|---------------------------------|-------------------------------------|
| GET      | `/api/dashboard`                | System summary metrics              |
| GET/POST | `/api/greenhouses`              | List / create greenhouses           |
| GET/PUT/DELETE | `/api/greenhouses/{id}`  | Read / update / delete greenhouse   |
| GET/POST | `/api/zones`                    | List / create zones                 |
| GET/PUT/DELETE | `/api/zones/{id}`        | Read / update / delete zone         |
| GET/POST | `/api/sensors`                  | List / create sensors               |
| GET/PUT/DELETE | `/api/sensors/{id}`      | Read / update / delete sensor       |
| GET/POST | `/api/sensor-readings`          | List / record sensor readings       |
| GET/POST | `/api/actuators`                | List / create actuators             |
| PATCH    | `/api/actuators/{id}/state`     | Toggle actuator ON/OFF              |
| GET/POST | `/api/automation-rules`         | List / create automation rules      |
| GET/POST | `/api/alerts`                   | List / create alerts                |
| PUT      | `/api/alerts/{id}/status`       | Update alert status                 |

Full interactive documentation: **http://localhost:8080/swagger-ui.html**

---

## Role Permissions

| Endpoint category          | ADMIN | OPERATOR |
|----------------------------|:-----:|:--------:|
| GET any resource            | ✅    | ✅       |
| POST greenhouses/zones/sensors/actuators/rules | ✅ | ❌ |
| PUT/DELETE most resources  | ✅    | ❌       |
| POST sensor-readings       | ✅    | ✅       |
| PUT alert status           | ✅    | ✅       |

---

## Running Tests

```bash
# Unit tests (JUnit 5 + Mockito)
cd backend
mvn test

# E2E Selenium tests (requires running frontend)
mvn test -P e2e
```

---

## Generating Javadoc

```bash
cd backend
mvn javadoc:javadoc
# Output: target/site/apidocs/index.html
```
