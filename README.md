# Personal Finance Tracker

A full-stack personal finance tracker built with **React.js**, **Spring Boot**, **MySQL**, and **JWT authentication**. Demonstrates modern Full Stack Java development with CRUD operations, RESTful APIs, database integration, and secure per-user data isolation.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React.js, React Router, Recharts, Axios |
| Backend | Spring Boot 3, Spring Security, Spring Data JPA |
| Database | MySQL |
| Auth | JWT (JSON Web Tokens) |
| Build/VCS | Maven, Git |

## Features

- **Authentication** — signup, login, JWT token management; users only see their own data.
- **Dashboard** — income/expense summary, pie chart by category, budget vs. spent bar chart.
- **Transaction Management** — add, edit, delete income/expense entries with category, date, amount.
- **Budget Analytics** — set monthly category limits, get visual alerts when over budget.
- **Data Export** — download monthly expense reports as CSV or PDF.

## Project Structure

```
personal-finance-tracker/
├── backend/                    # Spring Boot API
│   ├── pom.xml
│   └── src/main/java/com/financetracker/
│       ├── config/             # Security & CORS config
│       ├── controller/         # REST controllers
│       ├── dto/                # Request/response DTOs
│       ├── entity/             # JPA entities
│       ├── exception/          # Global exception handling
│       ├── repository/         # Spring Data repositories
│       ├── security/           # JWT filter, service, user details
│       └── service/            # Business logic
├── frontend/                   # React SPA
│   └── src/
│       ├── api/                # Axios API clients
│       ├── components/         # Navbar, PrivateRoute
│       ├── context/            # AuthContext
│       └── pages/              # Login, Register, Dashboard, Transactions, Budgets
└── README.md
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- MySQL 8+

### 1. Database Setup

```sql
CREATE DATABASE finance_tracker;
```

The schema is auto-created by Hibernate (`spring.jpa.hibernate.ddl-auto=update`) on first run.

### 2. Backend Setup

```bash
cd backend
# Edit src/main/resources/application.properties with your MySQL credentials
mvn clean install
mvn spring-boot:run
```
The API runs on `http://localhost:8080`.

**Important (production):** override `jwt.secret`, `spring.datasource.username/password` via environment variables instead of committing them:

```bash
export SPRING_DATASOURCE_USERNAME=your_user
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your_base64_256bit_secret
```

### 3. Frontend Setup

```bash
cd frontend
cp .env.example .env   # adjust REACT_APP_API_URL if needed
npm install
npm start
```
The app runs on `http://localhost:3000`.

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Login, returns JWT |
| GET/POST | `/api/transactions` | List / create transactions |
| PUT/DELETE | `/api/transactions/{id}` | Update / delete a transaction |
| GET/POST | `/api/budgets` | List / create budgets |
| GET | `/api/budgets/status?month=&year=` | Budget vs. spent status |
| GET | `/api/dashboard/summary?month=&year=` | Aggregated dashboard data |
| GET | `/api/export/csv?month=&year=` | Download CSV report |
| GET | `/api/export/pdf?month=&year=` | Download PDF report |

All endpoints except `/api/auth/**` and `/api/health` require a `Authorization: Bearer <token>` header.

## Git Workflow

```bash
git init
git add .
git commit -m "Initial commit: Personal Finance Tracker full stack app"
git branch -M main
git remote add origin <your-repo-url>
git push -u origin main
```

Suggested branching model: `main` (stable) → `develop` → feature branches (`feature/budget-alerts`, `feature/csv-export`, etc.), merged via pull requests.

## License

MIT — free to use for learning and portfolio purposes.
