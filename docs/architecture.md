# Architecture blueprint

This document tracks the current implementation state of the AI Time Coach prototype.

## Layers implemented
- **Presentation (Compose)**: `SchedulerApp` and `SchedulerViewModel` create a beginner-friendly chat-like input, show gym suggestions, and expose focus timer controls.
- **Domain use cases**: `ScheduleParser`, `GymRecommendationUseCase`, `StandUpReminderUseCase`, and `FocusTimerUseCase` handle the business rules described in the README.
- **Data**: For now an in-memory `ScheduleRepository` mimics future Room persistence but keeps the API identical so it can be swapped later.
- **AI**: `NaturalLanguagePlanner` is a deterministic parser that represents how an LLM-backed interpreter will behave. Prompts and guardrails live in `docs/prompts.md`.

## Next steps
1. Replace `ScheduleRepository` with a Room database (entities mirror `ScheduleEntry`).
2. Move natural-language understanding to a cloud function and keep deterministic parser as offline fallback.
3. Add WorkManager jobs to evaluate movement reminders and gym suggestions even when the UI is not visible.
