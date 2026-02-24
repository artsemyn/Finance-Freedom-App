
# ANDROID PROJECT ROADMAP

Personal Finance Management App (Finance Freedom)

---

# PHASE 0 — Foundation & Architecture Setup (Week 0)

## 1. Project Initialization

* Create new Android project (Empty Activity)
* Enable:

    * Kotlin
    * Material 3
    * ViewBinding or Compose (if chosen)
* Setup Gradle dependencies:

    * Retrofit
    * OkHttp
    * Coroutines
    * Lifecycle ViewModel
    * Navigation Component
    * WorkManager
    * EncryptedSharedPreferences

## 2. Base Architecture Setup (Mandatory MVVM)

Create base package structure:

```
data/
  remote/
  repository/
  local/

domain/
  model/

ui/
  home/
  report/
  add/
  history/
  profile/

utils/
```

Follow rule from AGENTS.md :
UI → ViewModel → Repository → Remote

## 3. Networking Layer Setup

* Create `ApiService.kt`
* Setup Retrofit Builder
* Add OkHttp Interceptor for JWT
* Create Base URL in `BuildConfig`
* Add global 401 handler

---

# PHASE 1 — Authentication System (Week 1)

## Goal:

App can register, login, and persist session.

## 1. Backend Integration

Use endpoints from backend spec :

* POST /auth/register
* POST /auth/login
* GET /auth/me

## 2. Implementation Steps

* Create:

    * AuthApi
    * AuthRepository
    * LoginViewModel
    * RegisterViewModel

* Store JWT securely using:

    * EncryptedSharedPreferences

* Implement:

    * Auto login if token exists
    * Logout (clear token)

## Deliverable

User can:

* Register
* Login
* Stay logged in
* Logout safely

---

# PHASE 2 — Transactions Core (Week 2)

This is the heart of the app.

## 1. Add Transaction Feature

Endpoint:

* POST /transactions

Fields:

* title
* amount
* type (income / expense)
* category
* date
* note

Create:

* AddTransactionViewModel
* TransactionRepository
* Transaction DTO mapping

---

## 2. History Page

Endpoints:

* GET /transactions
* PUT /transactions/:id
* DELETE /transactions/:id

Features:

* RecyclerView list
* Swipe left → delete
* Swipe right → edit
* Filter (All / Income / Expense)
* Search

---

## 3. Balance Calculation

Formula from plan :
Balance = Total Income − Total Expense

Implement:

* Use BigDecimal
* Compute inside ViewModel
* Expose as StateFlow

---

## Deliverable

* Full CRUD transactions
* Real-time balance update
* History filtering

---

# PHASE 3 — Savings Goals (Week 3)

## Backend Endpoints

* POST /savings
* GET /savings
* PUT /savings/:id/progress
* DELETE /savings/:id

## Implementation

Create:

* SavingsRepository
* SavingsViewModel
* SavingsUiState

Progress formula :
(currentAmount / targetAmount) × 100%

UI:

* Progress bar
* Deadline
* Add contribution

## Deliverable

* Create savings goal
* Track progress
* Manual update
* Auto-savings handled by backend cron

---

# PHASE 4 — Reminder System + Notification (Week 3)

## Backend Endpoints

* POST /reminders
* GET /reminders
* GET /reminders/upcoming
* PUT /reminders/:id/paid
* DELETE /reminders/:id

## Android Responsibilities

From plan :

* Notify 1 day before due date

Use:

* WorkManager
* UniqueWork
* Avoid duplicate scheduling

Flow:

1. Fetch reminders
2. Schedule notification for each
3. Cancel if marked paid

## Deliverable

* Reminder CRUD
* Notification 1 day before
* Paid status toggle

---

# PHASE 5 — Home Dashboard (Week 4)

Bento-style layout from project scope

Home displays:

* Total balance
* Current month income
* Current month expense
* Savings progress
* Top 3 upcoming reminders

Implementation:

* Combine multiple repositories
* Use Mediator state
* Handle loading state correctly

---

# PHASE 6 — Report & Analytics (Week 4)

Endpoint:

* GET /transactions/summary?month=YYYY-MM

Features:

* Income vs Expense comparison
* Pie chart by category
* Weekly bar chart
* Month-to-month comparison

Use:

* MPAndroidChart (or Compose chart lib)

Handle:

* Empty state
* Loading state
* Error state

---

# PHASE 7 — UI & Design Polish (Week 4–5)

Follow design system  :

* Rounded corners 20–28dp
* Elevated cards
* Material 3
* Dark mode support
* Clear income (green) vs expense (red) distinction

Refactor:

* Extract reusable components
* Improve spacing
* Add animations

---

# PHASE 8 — Security Hardening

* Ensure JWT encrypted
* Clear token on logout
* No base URL hardcoded
* No logging sensitive data
* Handle 401 globally

---

# PHASE 9 — Testing & Stabilization

Manual testing checklist:

Authentication:

* Invalid login
* Expired token
* Logout behavior

Transactions:

* Edge amount values
* Large numbers
* Date filters

Savings:

* Over target
* Zero progress

Reminders:

* Duplicate scheduling
* Paid toggle cancels notification

---

# PHASE 10 — Production Readiness

* Add loading shimmer
* Improve empty state UI
* Add basic crash logging
* Prepare release build
* Use proper versioning

---

# Final Recommended Timeline

Week 1
Authentication + Architecture

Week 2
Transactions + History + Balance

Week 3
Savings + Reminders + Notifications

Week 4
Dashboard + Reports + Polish

Week 5
Security + Testing + Release Prep

---
