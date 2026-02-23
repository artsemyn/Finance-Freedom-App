# Finance Freedom Design Language (Android)

## 1. Purpose

This document defines the design language for the Finance Freedom Android app and aligns with:
- `AGENTS.md` (architecture, security, workflow, UI rules)
- `README (Backend).md` (backend stack and API contract)

Scope:
- Android app UI/UX behavior (Material 3)
- Financial semantics and presentation rules
- API-aware UX states based on backend behavior

Non-scope:
- Web/React/Tailwind guidance
- Direct database integration from Android

## 2. Product and Architecture Alignment

Finance Freedom app goals:
- Track income and expense transactions
- Monitor savings goals and progress
- Manage reminders and upcoming due items
- Evaluate monthly financial performance

System flow (mandatory):
- Android App -> REST API -> PostgreSQL (Neon)

Architecture in app (mandatory):
- UI -> ViewModel -> Repository -> Remote Data Source
- MVVM + Repository Pattern
- Retrofit + OkHttp Interceptor (JWT)
- Coroutines + Flow/StateFlow

## 3. Platform and Technical Context

Android stack assumptions:
- Kotlin
- Jetpack Compose + Material 3 (Material You)
- WorkManager for reminder notifications
- EncryptedSharedPreferences for JWT

Backend contract source:
- `README (Backend).md`

Backend summary:
- Node.js + Express 5
- Prisma + PostgreSQL (Neon)
- JWT Authentication + bcrypt
- node-cron auto-savings job

Server behavior:
- Uses `process.env.PORT` with fallback `3000`
- Local default URL typically `http://localhost:3000` (for emulator, configure host mapping as needed)

## 4. Information Architecture

Bottom navigation pages:
- Home
- Report
- Add
- History
- Profile

Feature to API mapping:
- Auth: `POST /auth/register`, `POST /auth/login`, `GET /auth/me`
- Transactions: `POST /transactions`, `GET /transactions`, `PUT /transactions/:id`, `DELETE /transactions/:id`, `GET /transactions/summary?month=YYYY-MM`
- Savings: `POST /savings`, `GET /savings`, `PUT /savings/:id/progress`, `DELETE /savings/:id`
- Reminders: `POST /reminders`, `GET /reminders`, `GET /reminders/upcoming`, `PUT /reminders/:id/paid`, `DELETE /reminders/:id`

## 5. Visual Identity

### 5.1 Core Colors (from AGENTS.md)
- Primary: `#70AD77`
- Accent: `#0F5257`
- Light Background: `#DFDFDF`
- Dark Background: `#1C1C1C`

### 5.2 Semantic Financial Colors
- Income: green (positive)
- Expense: red (negative)
- Savings/Goals: accent teal
- Reminder/Warning: orange

Rules:
- Income and expense colors must always be visually distinct.
- Never swap income/expense semantics across screens.
- In dark mode, preserve semantic meaning first, then tune brightness for contrast.

### 5.3 Elevation and Shape
- Rounded corners: 20dp-28dp
- Prefer elevated cards over flat blocks for major data groups
- Home should follow bento-style grouping with clear hierarchy

## 6. Typography and Density

Typography guidance:
- Use Material 3 type scale tokens
- Keep hierarchy obvious: screen title > section title > value > label
- Financial amount text should be at least one weight heavier than adjacent labels

Readability:
- Avoid cramped layouts; prioritize scanning
- Use consistent spacing rhythm across cards, lists, and forms

## 7. Screen Patterns

### 7.1 Home
- Bento-style dashboard
- At minimum includes:
  - Balance snapshot
  - Income vs expense summary
  - Savings progress
  - Upcoming reminders

### 7.2 Report
- Monthly summary from `/transactions/summary?month=YYYY-MM`
- Must let user understand net result quickly:
  - Total income
  - Total expense
  - Balance (`income - expense`)

### 7.3 Add
- Fast form for adding transactions (and optionally reminders/goals based on flow)
- Strong type cues for income vs expense
- Inline validation before submission

### 7.4 History
- Transaction list with clear type/category/date/amount
- Filter and sort affordances should remain simple and readable

### 7.5 Profile
- Account details from `/auth/me`
- Theme mode controls
- Logout action must clear encrypted token

## 8. Financial Logic Presentation Rules

Balance formula:
- `Total Income - Total Expense`

Savings progress formula:
- `(currentAmount / targetAmount) * 100%`

Precision:
- Avoid floating precision issues for money
- Prefer `BigDecimal` in domain/business calculations when needed

Display rules:
- Always format currency consistently
- Never truncate in ways that change perceived value

## 9. API-Aware UX States

Every major screen must define:
- Loading state
- Empty state
- Error state
- Content state

Backend error contract:
```json
{ "error": "message" }
```

UI error rules:
- Show user-friendly messages, never raw stack traces
- Handle null/missing fields safely (no crashes)
- For `401 Unauthorized`, trigger global auth handling (clear session/redirect flow)

## 10. Authentication and Security UX

Mandatory security behavior:
- Store JWT only in EncryptedSharedPreferences
- Send `Authorization: Bearer <token>` on protected calls
- Never log JWT
- Never hardcode production base URL in multiple places
- Keep base URL configurable via BuildConfig

Logout behavior:
- Clear token and user session artifacts
- Reset navigation state to authenticated entry point

## 11. Reminders and Notifications

Reminder UX must align to business rules:
- Trigger reminder notification 1 day before due date
- Implement with WorkManager
- Prevent duplicate scheduling for same reminder/time

Backend alignment:
- Upcoming reminders source: `GET /reminders/upcoming`
- Paid toggle source: `PUT /reminders/:id/paid`

## 12. Accessibility and Dark Mode

Accessibility:
- Preserve clear contrast in light and dark modes
- Keep touch targets and spacing comfortable
- Avoid color-only communication; pair with labels/icons where possible

Dark mode:
- Must be fully supported
- Keep semantic colors meaningful and readable on `#1C1C1C` background

## 13. Implementation Checklist

When adding a new feature:
1. Confirm endpoint and payload with `README (Backend).md`
2. Create Retrofit API interface
3. Create DTO and map DTO -> domain model
4. Add repository function with success/error mapping
5. Add ViewModel state + events/effects as needed
6. Wire UI and handle loading/empty/error/content states
7. Validate token handling and unauthorized flow
8. Verify light/dark mode and financial color semantics

## 14. Version

Current version: `v2.0.0`

Summary of v2.0.0:
- Replaced web-oriented design language with Android Material 3 guidance
- Synced backend contract references to `README (Backend).md`
- Synced architecture and security rules with `AGENTS.md`
