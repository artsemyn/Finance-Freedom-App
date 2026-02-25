# AGENTS.md

## 1. Project Overview

This repository contains the Android application for the Personal Finance Management App.

Purpose of the app:
- Track income and expenses
- Monitor savings goals
- Manage reminders (bills, debts, installments)
- Evaluate monthly financial performance

The Android app communicates with a separate REST API backend (Node.js + Express + Prisma + Neon PostgreSQL).

Important:
- Android must never connect directly to PostgreSQL.

Architecture flow:
- Android App -> REST API -> PostgreSQL (Neon) -> JWT Authentication

## 2. Technology Stack (Android)

- Kotlin
- MVVM Architecture
- Repository Pattern
- Retrofit (API communication)
- Kotlin Coroutines + Flow / StateFlow
- Material 3 (Material You)
- EncryptedSharedPreferences (JWT storage)
- WorkManager (reminder notifications)

## 3. Project Structure

Recommended module/package structure:

```text
com.example.financefreedom
|
|-- data/
|   |-- remote/            // Retrofit services, DTOs
|   |-- local/             // Preferences, local cache (if needed)
|   `-- repository/        // Repository implementations
|
|-- domain/
|   |-- model/             // Domain models
|   `-- usecase/           // Optional use cases
|
|-- ui/
|   |-- home/
|   |-- report/
|   |-- add/
|   |-- history/
|   `-- profile/
|
|-- utils/
|
`-- MainActivity.kt
```

Bottom Navigation Pages:
- Home
- Report
- Add
- History
- Profile

## 4. Architecture Rules (Mandatory)

All agents modifying this repository must follow:
- Use MVVM strictly.
- UI -> ViewModel -> Repository -> Remote Data Source

Do not place business logic inside:
- Activities
- Fragments
- Composables

ViewModel must:
- Expose StateFlow or LiveData
- Handle coroutine scope properly
- Not hold Android Context unless necessary

Repository must:
- Be the single source of data for ViewModel
- Handle API error mapping
- Return Result wrapper (Success / Error)

Networking:
- Use Retrofit
- Attach JWT via OkHttp Interceptor
- Handle 401 (unauthorized) globally

## 5. API Integration Rules

Backend reference source:
- `README (Backend).md` (Finance Backend specification in this repository)

Backend stack:
- Node.js + Express 5
- Prisma ORM + PostgreSQL (Neon)
- JWT Authentication
- bcrypt
- node-cron

Base behavior:
- Backend runs on `process.env.PORT` with fallback `3000` (local default: `http://localhost:3000`)
- Current deployed backend base URL: `https://finance-backend-gold.vercel.app/` (must remain configurable via BuildConfig)
- Android must consume backend via REST API only
- Android must never connect directly to PostgreSQL

Primary endpoint groups to align with:
- Auth: `/auth/register`, `/auth/login`, `/auth/me`
- Transactions: `/transactions`, `/transactions/:id`, `/transactions/summary?month=YYYY-MM`
- Savings Goals: `/savings`, `/savings/:id/progress`, `/savings/:id`
- Reminders: `/reminders`, `/reminders/upcoming`, `/reminders/:id/paid`, `/reminders/:id`

Backend feature note:
- Backend includes monthly auto-savings via cron job (`src/jobs/autoSavings.job.js`)

Authentication:
- Store JWT securely using EncryptedSharedPreferences
- Add header: `Authorization: Bearer <token>`

Never:
- Hardcode base URL in multiple places
- Store token in plain SharedPreferences
- Log JWT in console

## 6. UI and Design System

Material 3 guidelines.

Color palette:
- Primary: `#70AD77`
- Accent: `#0F5257`
- Light Background: `#DFDFDF`
- Dark Background: `#1C1C1C`

Design characteristics:
- Rounded corners (20dp-28dp)
- Elevated cards
- Spacious layout
- Bento-style dashboard (Home)
- Clear distinction between income and expense colors
- Dark mode must be supported

## 7. Business Logic Rules

Balance:
- `Total Income - Total Expense`

Savings progress:
- `(currentAmount / targetAmount) * 100%`

Reminder notification:
- Trigger 1 day before due date
- Use WorkManager
- Avoid duplicate scheduling

All calculations must be done safely:
- Avoid floating precision issues
- Prefer BigDecimal for money if needed

## 8. Error Handling Standard

UI must handle:
- Loading state
- Empty state
- Error state

Never:
- Crash on null response
- Show raw backend error stack

Error format expected from backend:

```json
{
  "error": "message"
}
```

## 9. Code Style and Conventions

- Follow Kotlin official style guide

Use meaningful naming, for example:
- `TransactionRepository`
- `AddTransactionViewModel`
- `ReportUiState`

- Avoid massive classes
- Separate UI state from domain model

Use:
- Sealed classes for UI state
- Data classes for models
- Extension functions for formatting

## 10. Security Rules

- Never commit API keys
- Never commit real base URLs for production
- Keep base URL configurable (BuildConfig)
- JWT must be encrypted
- Logout must clear token

## 11. Development Workflow

When implementing new features:
1. Create API interface (if needed)
2. Create DTO
3. Map DTO -> Domain Model
4. Add Repository function
5. Add ViewModel logic
6. Connect to UI
7. Handle loading/error states

## 12. Roadmap Context (Roadmap.md)

Roadmap source:
- `Roadmap.md` at repository root is the implementation timeline reference.

Phase order to follow:
1. Phase 0: Foundation & Architecture Setup
2. Phase 1: Authentication
3. Phase 2: Transactions Core
4. Phase 3: Savings Goals
5. Phase 4: Reminder + Notification
6. Phase 5: Home Dashboard
7. Phase 6: Report & Analytics
8. Phase 7: UI & Design Polish
9. Phase 8: Security Hardening
10. Phase 9: Testing & Stabilization
11. Phase 10: Production Readiness

Phase 0 mandatory baseline (must be completed before Phase 1):
- Add required app dependencies (Retrofit, OkHttp, Coroutines, ViewModel, Navigation, WorkManager, EncryptedSharedPreferences)
- Keep MVVM chain strict: UI -> ViewModel -> Repository -> Remote
- Implement networking foundation (`ApiService`, Retrofit builder, JWT interceptor, BuildConfig base URL)
- Implement global 401 handling flow and session/token clearing path
- Keep token encrypted and never log JWT

## 13. Project Progress Checklist

Use this checklist to track roadmap completion status.  
Status legend:
- `[x]` Completed
- `[ ]` Not Started / In Progress

- [x] Phase 0: Foundation & Architecture Setup
- [x] Phase 1: Authentication
- [ ] Phase 2: Transactions Core
- [ ] Phase 3: Savings Goals
- [ ] Phase 4: Reminder + Notification
- [ ] Phase 5: Home Dashboard
- [ ] Phase 6: Report & Analytics
- [ ] Phase 7: UI & Design Polish
- [ ] Phase 8: Security Hardening
- [ ] Phase 9: Testing & Stabilization
- [ ] Phase 10: Production Readiness

## 14. Agent Change Logging (Mandatory)

Any agent modifying files in this repository must:
- Update this `AGENTS.md`
- Add entry under Change Log
- Keep newest entries at the top

Format:
- `YYYY-MM-DD | Agent Name | Files Changed | Short Description`

If no files were modified, no entry required.

## 15. Change Log

2026-02-25 | Codex | AGENTS.md, app/src/main/java/com/example/financefreedom/ui/auth/LoginScreen.kt | Updated login UX so credential-related errors clear email/password inputs instead of leaving stale values.

2026-02-25 | Codex | AGENTS.md, app/src/main/java/com/example/financefreedom/data/repository/AuthRepositoryImpl.kt, app/src/main/java/com/example/financefreedom/ui/navigation/AppNavGraph.kt | Fixed register success handling for backends that do not return token on register; route to login after register unless token is present.

2026-02-25 | Codex | AGENTS.md, gradle/libs.versions.toml, app/build.gradle.kts, app/src/main/java/com/example/financefreedom/MainActivity.kt, app/src/main/java/com/example/financefreedom/data/remote/ApiService.kt, app/src/main/java/com/example/financefreedom/data/remote/dto/AuthDto.kt, app/src/main/java/com/example/financefreedom/data/remote/mapper/AuthMapper.kt, app/src/main/java/com/example/financefreedom/domain/model/User.kt, app/src/main/java/com/example/financefreedom/data/repository/AuthRepository.kt, app/src/main/java/com/example/financefreedom/data/repository/AuthRepositoryImpl.kt, app/src/main/java/com/example/financefreedom/ui/auth/AuthUiState.kt, app/src/main/java/com/example/financefreedom/ui/auth/AuthViewModelFactory.kt, app/src/main/java/com/example/financefreedom/ui/auth/LoginViewModel.kt, app/src/main/java/com/example/financefreedom/ui/auth/RegisterViewModel.kt, app/src/main/java/com/example/financefreedom/ui/auth/StartupViewModel.kt, app/src/main/java/com/example/financefreedom/ui/auth/LoginScreen.kt, app/src/main/java/com/example/financefreedom/ui/auth/RegisterScreen.kt, app/src/main/java/com/example/financefreedom/ui/navigation/Routes.kt, app/src/main/java/com/example/financefreedom/ui/navigation/AppNavGraph.kt, app/src/main/java/com/example/financefreedom/ui/home/HomeScreen.kt, app/src/main/java/com/example/financefreedom/ui/profile/ProfileScreen.kt | Implemented Phase 1 authentication flow end-to-end: register/login/me integration, encrypted token session bootstrap, logout, 401-driven session handling, and Compose auth navigation with placeholder home/profile screens.

2026-02-25 | Codex | AGENTS.md | Added a roadmap-based project progress checklist section and initialized current phase statuses.

2026-02-24 | Codex | AGENTS.md, gradle/libs.versions.toml, app/build.gradle.kts, app/src/main/AndroidManifest.xml, app/src/main/java/com/example/financefreedom/data/local/TokenManager.kt, app/src/main/java/com/example/financefreedom/data/local/SessionManager.kt, app/src/main/java/com/example/financefreedom/data/remote/ApiService.kt, app/src/main/java/com/example/financefreedom/data/remote/ApiClient.kt, app/src/main/java/com/example/financefreedom/data/remote/AuthInterceptor.kt, app/src/main/java/com/example/financefreedom/data/remote/UnauthorizedHandler.kt, app/src/main/java/com/example/financefreedom/utils/Result.kt, app/src/main/java/com/example/financefreedom/utils/ApiErrorParser.kt | Implemented Phase 0 foundation: core dependencies, networking baseline with JWT + global 401 handling, encrypted token storage, and roadmap alignment section.
2026-02-23 | Codex | app/build.gradle.kts, AGENTS.md | Wired deployed backend URL into Android BuildConfig as BASE_URL.
2026-02-23 | Codex | AGENTS.md, README.md, design-language.md | Set deployed backend address to https://finance-backend-gold.vercel.app/ in project documentation and API integration guidance.
2026-02-23 | Codex | AGENTS.md, design-language.md, README.md | Rewrote design-language to Android/backend-aligned spec, added Android project README, and fixed backend fallback port to 3000 for doc consistency.
2026-02-23 | Codex | AGENTS.md, app/src/main/java/com/example/financefreedom/data/remote/.gitkeep, app/src/main/java/com/example/financefreedom/data/local/.gitkeep, app/src/main/java/com/example/financefreedom/data/repository/.gitkeep, app/src/main/java/com/example/financefreedom/domain/model/.gitkeep, app/src/main/java/com/example/financefreedom/domain/usecase/.gitkeep, app/src/main/java/com/example/financefreedom/ui/home/.gitkeep, app/src/main/java/com/example/financefreedom/ui/report/.gitkeep, app/src/main/java/com/example/financefreedom/ui/add/.gitkeep, app/src/main/java/com/example/financefreedom/ui/history/.gitkeep, app/src/main/java/com/example/financefreedom/ui/profile/.gitkeep, app/src/main/java/com/example/financefreedom/utils/.gitkeep | Created MVVM package skeleton in source tree and aligned documented package path.
2026-02-23 | Codex | AGENTS.md | Matched backend integration details with README (Backend).md and corrected backend reference filename.
2026-02-23 | Codex | AGENTS.md | Updated backend integration section to match README.md backend stack, base behavior, and endpoint groups.
2026-02-23 | Codex | AGENTS.md | Rewrote and reformatted file with clean Markdown structure, fixed encoding artifacts, and preserved all repository rules.
