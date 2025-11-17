# AI Time-Coach (Scheduling-app)

AI-first Android companion that learns your weekly routine, auto-schedules workouts with travel buffers, keeps you moving with stand‑up nudges, and powers a distraction-free focus timer—all through natural-language input.

## 🌟 Key features
1. **Guided setup interview** – AI chats with you about a typical Monday-to-Sunday schedule. It validates each free-form entry (“Classes 8–11, commute 30m”) before storing it in the local Room database.
2. **Smart gym planner** – Suggests gym slots that fit open windows, accounts for transit time, and tracks consistency.
3. **Move reminders** – Micro-break nudges (stand, stretch, walk) tailored to sedentary streaks detected in your schedule.
4. **Focus mode** – Minimalist timer that locks the screen in study mode, logs sessions, and syncs with AI insights.
5. **Beginner-friendly text interface** – Users can describe tasks in plain language; the AI parser extracts intents, durations, and locations.

## 🏗️ Repository structure
```
Scheduling-app/
├── README.md                # Beginner-friendly overview, AI workflow, and setup steps
├── docs/
│   ├── architecture.md      # Explanation of AI pipeline, clean architecture layers, and data flows
│   ├── prompts.md           # Prompt templates + guardrails for natural-language scheduling input
│   └── ux/
│       ├── personas.md      # Target users, their routines, and AI expectations
│       └── flows.md         # Onboarding, AI-setup interview, gym nudges, stand-up reminders, focus timer
├── app/
│   ├── build.gradle.kts     # Android module configuration (Jetpack Compose, coroutines, etc.)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── com/example/scheduler/
│       │   │       ├── ai/             # LLM request models, parser/validator, travel-time estimator
│       │   │       ├── data/           # Repositories, entities, Room (future)
│       │   │       ├── domain/         # Use cases (AI setup interview, gym recommender, stand-up coach, focus mode)
│       │   │       └── presentation/   # Compose screens, navigation, ViewModels, timers
│       │   └── res/
│       │       └── values/strings.xml
│       └── test/                       # Unit tests (domain + ViewModel)
├── backend/
│   ├── api/                            # Optional cloud endpoints (sync, AI inference fallback)
│   └── infrastructure/                 # Deployment, secrets, monitoring
└── tools/
    ├── scripts/                        # Data seeding, prompt evaluation, CI helpers
    └── ci/                             # GitHub Actions/automation configs
```

## 🧰 Tech stack
| Layer | Technology choices | Purpose |
| --- | --- | --- |
| UI | **Kotlin**, **Jetpack Compose**, **Material 3**, **Navigation Compose** | Declarative UI, adaptive layouts, guided flows, focus timer screen |
| Presentation | **Android ViewModel**, **StateFlow/Coroutines** | State management, lifecycle-aware logic |
| Domain | Plain Kotlin modules with use cases & sealed models | Encapsulate AI interview logic, gym planning, movement reminders |
| Data | In-memory repository (prototype) → **Room**, **DataStore**, **WorkManager** | Offline-first storage, preference handling, background sync |
| AI & NLP | `NaturalLanguagePlanner` fallback today → **OpenAI / Azure OpenAI LLM API**, optional **Google Distance Matrix** | Parse free-form schedules, validate intent, estimate travel time |
| Testing | **JUnit4/5**, **Turbine**, **Compose UI Test**, **Detekt/ktlint** | Unit, flow, and UI tests + static analysis |
| Tooling | **Gradle (KTS)**, **GitHub Actions**, **Play Console** | Build, CI/CD, linting, release management |

## 🧩 Prototype status
- ✅ **Composable UI** for setup input, schedule list, gym suggestion card, movement reminders, and focus timer is live in `SchedulerApp`.
- ✅ **ViewModel + use cases** power deterministic AI-like behaviors (parsing, gym suggestions, stand-up nudges, timer logic).
- ⚙️ **Persistence and real AI calls** are stubbed with in-memory repositories and deterministic parsers so the flows are demoable offline.

## 🚀 Run the Android demo
1. Install Android Studio (Giraffe+) with SDK 34 and the Compose toolkit.
2. Clone this repo and open it in Android Studio.
3. Create a `local.properties` with your SDK path (e.g., `sdk.dir=/Users/me/Library/Android/sdk`).
4. Run `./gradlew :app:assembleDebug` or use *Run ▶️ app* in Android Studio to launch the Compose preview on an emulator/device.

## 🔐 Privacy & safety principles
- Transparent consent during AI onboarding; users can opt out of cloud inference.
- Encrypted local storage for personal schedules and health data.
- Feature flags to disable gym or movement nudges when not desired.

## 🧪 Testing strategy
- Unit tests for use cases (parsing schedule text, gym-slot generator, reminder cadence).
- Instrumented UI tests covering onboarding chat, task entry, and focus timer.
- Prompt regression suite to ensure AI responses remain deterministic for given inputs.

## 🗺️ Next steps
1. Swap the in-memory repository for Room entities + DAO.
2. Integrate a hosted LLM endpoint (OpenAI/Azure) behind `NaturalLanguagePlanner`.
3. Add WorkManager jobs and notifications so stand-up/gym reminders work even when the app is closed.
