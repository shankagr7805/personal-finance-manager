# Personal Finance Manager

A RESTful API for tracking personal income, expenses, and savings goals — built with Spring Boot 3 and secured via session-based authentication.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security (session-based) |
| Database | H2 (in-memory) |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito |
| Build | Maven |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+

### Running Locally

```bash
git clone <your-repo-url>
cd personal-finance-manager
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:fintrackdb`).

### Running Tests

```bash
mvn test
```

For coverage report:
```bash
mvn test jacoco:report
# Open target/site/jacoco/index.html
```

---

## API Reference

All endpoints except `/api/auth/register` and `/api/auth/login` require authentication via session cookie.

### Authentication

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "Jane Doe",
  "phoneNumber": "+1234567890"
}
```

Response `201`:
```json
{ "message": "User registered successfully", "userId": 1 }
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{ "username": "user@example.com", "password": "password123" }
```

Response `200` (sets session cookie):
```json
{ "message": "Login successful" }
```

#### Logout
```
POST /api/auth/logout
```

---

### Transactions

#### Create Transaction
```
POST /api/transactions
{ "amount": 5000.00, "date": "2024-01-15", "category": "Salary", "description": "Monthly salary" }
```

#### Get Transactions (with optional filters)
```
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&categoryId=1
```

#### Update Transaction
```
PUT /api/transactions/{id}
{ "amount": 6000.00, "description": "Updated salary" }
```
> Note: `date` cannot be changed after creation.

#### Delete Transaction
```
DELETE /api/transactions/{id}
```

---

### Categories

Default categories (cannot be deleted): `Salary` (INCOME), `Food`, `Rent`, `Transportation`, `Entertainment`, `Healthcare`, `Utilities` (all EXPENSE).

#### Get All Categories
```
GET /api/categories
```

#### Create Custom Category
```
POST /api/categories
{ "name": "Freelance", "type": "INCOME" }
```

#### Delete Custom Category
```
DELETE /api/categories/{name}
```

---

### Savings Goals

Progress is automatically calculated as `Total Income − Total Expenses` since the goal's start date.

#### Create Goal
```
POST /api/goals
{ "goalName": "Emergency Fund", "targetAmount": 5000.00, "targetDate": "2026-06-01" }
```

#### Get All Goals
```
GET /api/goals
```

#### Get Goal
```
GET /api/goals/{id}
```

#### Update Goal
```
PUT /api/goals/{id}
{ "targetAmount": 6000.00, "targetDate": "2026-12-01" }
```

#### Delete Goal
```
DELETE /api/goals/{id}
```

---

### Reports

#### Monthly Report
```
GET /api/reports/monthly/{year}/{month}
```

#### Yearly Report
```
GET /api/reports/yearly/{year}
```

---

## Error Handling

| Status | Meaning |
|---|---|
| 400 | Bad request / validation error |
| 401 | Unauthenticated |
| 403 | Forbidden (accessing another user's data) |
| 404 | Resource not found |
| 409 | Conflict (duplicate category name, existing email) |

All errors return:
```json
{ "message": "Description of the error", "status": 400 }
```

---

## Design Decisions

**Session-based auth over JWT**: Simpler to implement correctly and sufficient for a web application context. The session cookie is HttpOnly for XSS protection.

**H2 in-memory DB**: Keeps setup friction to zero. Swapping in PostgreSQL just requires changing `application.properties` — the JPA queries are all portable JPQL.

**Category merging at query time**: Default categories live in the DB with `user = null`. The service fetches defaults + user's custom categories and merges them in memory — clean and avoids complex SQL joins.

**Goal progress is always live**: `currentProgress` is never stored. It's recalculated on every read from actual transaction data, so it's always accurate even after transactions are added or deleted.

**Global exception handler**: `@ControllerAdvice` in `GlobalExceptionHandler` maps domain exceptions to HTTP responses in one place, keeping controllers thin.

---

## Deploying to Render

1. Push to a public GitHub repository.
2. Create a new **Web Service** on [render.com](https://render.com).
3. Set build command: `mvn clean package -DskipTests`
4. Set start command: `java -jar target/personal-finance-manager-1.0.0.jar`
5. Set environment variable `SERVER_PORT=10000` (Render's default port).
6. Deploy and run the test script:

```bash
bash financial_manager_tests.sh https://your-app.onrender.com/api
```
