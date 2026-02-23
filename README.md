# Finance Freedom Android

Android app for the Personal Finance Management project.

## Overview

Finance Freedom helps users:
- Track income and expenses
- Monitor savings goals
- Manage reminders (bills, debts, installments)
- Review monthly financial performance

Backend architecture:
- Android App -> REST API -> PostgreSQL (Neon)
- Android must never connect directly to PostgreSQL

Backend reference:
- `README (Backend).md`

Current deployed backend base URL:
- `https://finance-backend-gold.vercel.app/`
- Keep this configurable via BuildConfig (do not hardcode in multiple places)

## Current Status

This repository currently contains:
- Android project setup with Jetpack Compose + Material 3
- Base package namespace: `com.example.financefreedom`
- Initial MVVM folder skeleton:
  - `data/remote`
  - `data/local`
  - `data/repository`
  - `domain/model`
  - `domain/usecase`
  - `ui/home`, `ui/report`, `ui/add`, `ui/history`, `ui/profile`
  - `utils`

## Tech Stack (Android)

- Kotlin
- MVVM Architecture
- Repository Pattern
- Retrofit (planned for API integration)
- Kotlin Coroutines + Flow / StateFlow
- Material 3 (Material You)
- EncryptedSharedPreferences (planned for JWT storage)
- WorkManager (planned for reminder notifications)

## Requirements

- Android Studio (latest stable recommended)
- JDK 11
- Android SDK (minSdk 30, targetSdk 36, compileSdk 36)

## Run the App

1. Open project in Android Studio.
2. Sync Gradle.
3. Select an emulator/device.
4. Run `app` configuration.

CLI option:

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Project Structure

```text
finance-android/
|-- app/
|   |-- src/
|   |   |-- main/
|   |   |   |-- java/com/example/financefreedom/
|   |   |   |   |-- data/
|   |   |   |   |-- domain/
|   |   |   |   |-- ui/
|   |   |   |   `-- utils/
|   |   |   `-- res/
|   |   |-- test/
|   |   `-- androidTest/
|   `-- build.gradle.kts
|-- AGENTS.md
|-- design-language.md
|-- README (Backend).md
`-- README.md
```

## API Integration Contract

Source of truth:
- `README (Backend).md`

Current base URL (deployed):
- `https://finance-backend-gold.vercel.app/`

Local backend fallback:
- `http://localhost:3000`

Protected endpoints use:
- `Authorization: Bearer <token>`

Primary endpoint groups:
- Auth: `/auth/register`, `/auth/login`, `/auth/me`
- Transactions: `/transactions`, `/transactions/:id`, `/transactions/summary?month=YYYY-MM`
- Savings Goals: `/savings`, `/savings/:id/progress`, `/savings/:id`
- Reminders: `/reminders`, `/reminders/upcoming`, `/reminders/:id/paid`, `/reminders/:id`

Error format from backend:

```json
{ "error": "message" }
```

## Architecture Rules (Mandatory)

- Keep strict flow: `UI -> ViewModel -> Repository -> Remote Data Source`
- Do not place business logic in Activities, Fragments, or Composables
- ViewModel exposes UI state via StateFlow/LiveData
- Repository is single source of data and maps API errors
- Handle `401 Unauthorized` globally

See `AGENTS.md` for full rules and workflow requirements.

## Design System

Source:
- `design-language.md`

Core palette:
- Primary: `#70AD77`
- Accent: `#0F5257`
- Light background: `#DFDFDF`
- Dark background: `#1C1C1C`

## Security Rules

- Never commit API keys
- Never log JWT
- Use EncryptedSharedPreferences for token storage
- Keep base URL configurable (BuildConfig)
- Logout must clear token/session data

## Roadmap (Recommended Next Implementation Order)

1. Add networking layer (Retrofit + OkHttp + auth interceptor)
2. Implement auth flow (`register`, `login`, `me`)
3. Implement transactions list/create
4. Implement monthly report summary
5. Implement savings goals and progress updates
6. Implement reminders + WorkManager scheduling

## Documentation

- Android agent rules: `AGENTS.md`
- Backend API reference: `README (Backend).md`
- UI/UX and design standards: `design-language.md`
