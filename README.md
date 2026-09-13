# AI Finance & Expense Management System

A full-stack personal finance management application with AI-powered expense categorization and a conversational finance assistant.

![Dashboard](image.png)

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA |
| Database | H2 (in-memory) |
| AI | Google Gemini 2.5 Flash |
| Frontend | React 18, Vite, Tailwind CSS, Recharts |
| Testing | JUnit 5, Mockito 5 |

---

## Features

- **Expense Management** — CRUD with category and date filtering, pagination
- **Income Management** — CRUD with source categorization
- **Budget Management** — Monthly budgets with real-time spending tracking and exceeded warnings
- **Dashboard** — Balance overview, monthly spending chart, category breakdown pie chart, budget utilization, recent transactions
- **AI Expense Categorization** — One-click category suggestion from expense description using Gemini
- **AI Finance Chat** — Ask questions about your real transactions; answers are grounded in your actual data

---

## Prerequisites

Make sure you have the following installed:

- **Java 21+** — [Download](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi) or install via Homebrew: `brew install maven`
- **Node.js 20+** and **npm 10+** — [Download](https://nodejs.org/)
- **Google Gemini API key** — Get one free at [Google AI Studio](https://aistudio.google.com/app/apikey)

---

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/prar1hana/AI-Finance-Expense-Management-System.git
cd AI-Finance-Expense-Management-System
```

### 2. Configure the Gemini API key

Open `backend/src/main/resources/application-dev.properties` and replace `YOUR_GEMINI_API_KEY_HERE` with your actual key:

```properties
gemini.api.key=YOUR_GEMINI_API_KEY_HERE
```

Get a free key at [Google AI Studio](https://aistudio.google.com/app/apikey) → click **Get API key**.

**Gemini model in use:** `gemini-2.5-flash`

This is configured in `backend/src/main/resources/application.properties`:
```properties
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
```

**To switch to a different model** (e.g. `gemini-2.5-pro`, `gemini-flash-latest`), change only the model name in that URL. To find models available for your API key, call:
```bash
curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_KEY" | grep '"name"'
```

### 3. Start the backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev "-Dspring-boot.run.jvmArguments=-XX:+EnableDynamicAgentLoading"
```

Wait for: `Started FinanceAiApplication` in the output.

Backend runs at **http://localhost:8080**

### 4. Start the frontend

Open a new terminal tab:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at **http://localhost:5173**

---

## Demo Account

A demo account with sample data is pre-loaded on every backend start:

| Field | Value |
|---|---|
| Email | demo@example.com |
| Password | password123 |

Sample data includes: 10 expenses, 2 income records, 5 budgets for the current month.

You can also register a new account at http://localhost:5173/register

---

## Running Tests

```bash
cd backend
mvn test
```

30 tests across service, controller, and repository layers.

---

## Project Structure

```
├── backend/                          # Spring Boot application
│   └── src/main/java/com/financeai/
│       ├── config/                   # Security, CORS, DataLoader
│       ├── controller/               # 7 REST controllers
│       ├── dto/                      # Request/Response DTOs
│       ├── entity/                   # JPA entities
│       ├── exception/                # Global exception handling
│       ├── repository/               # Spring Data JPA repositories
│       ├── security/                 # JWT provider and filter
│       └── service/                  # Business logic
│
└── frontend/                         # React + Vite application
    └── src/
        ├── api/                      # Axios API modules
        ├── components/               # Charts, forms, layout, common UI
        ├── context/                  # Auth context
        ├── pages/                    # Dashboard, Expenses, Income, Budgets, AI Chat
        └── utils/                    # Formatters and constants
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register new user |
| POST | `/api/auth/login` | No | Login, receive JWT |
| GET | `/api/expenses` | Yes | List expenses (paginated, filterable) |
| POST | `/api/expenses` | Yes | Create expense |
| PUT | `/api/expenses/{id}` | Yes | Update expense |
| DELETE | `/api/expenses/{id}` | Yes | Delete expense |
| GET | `/api/incomes` | Yes | List income records |
| POST | `/api/incomes` | Yes | Create income |
| GET | `/api/budgets` | Yes | List budgets with spent amounts |
| POST | `/api/budgets` | Yes | Create budget |
| GET | `/api/dashboard/summary` | Yes | Balance, income, expense totals |
| GET | `/api/dashboard/monthly-spending` | Yes | Month-by-month chart data |
| GET | `/api/dashboard/recent-transactions` | Yes | Recent transactions |
| POST | `/api/ai/categorize` | Yes | Suggest category from description |
| POST | `/api/ai/chat` | Yes | Finance assistant chat |

All authenticated endpoints require: `Authorization: Bearer <token>`

---

## H2 Database Console (dev only)

While the backend is running: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`
- Password: *(leave empty)*

---

## Notes

- Data is **in-memory only** — resets on every backend restart (by design for dev/demo)
- The Gemini API key is **never committed** — `application-dev.properties` is gitignored
- This is a **simulated** finance app — no real payments or bank connections
