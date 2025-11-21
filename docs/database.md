# Database and Room integration

This app now persists schedules with a local **Room (SQLite) database** instead of the earlier in-memory repository. The flow stays aligned with the Clean Architecture boundaries: the database is hidden behind the repository so the presentation layer only deals with domain models.

## Data structures stored on disk
| Table | Columns (type) | Purpose |
| --- | --- | --- |
| `schedule_entries` | `id` (PK, autoincrement), `title` (TEXT), `day_of_week` (INTEGER), `start_time` (TEXT), `end_time` (TEXT), `location` (TEXT, nullable), `travel_buffer_minutes` (INTEGER, default 0) | Stores each user-supplied schedule block with optional location and travel buffer. | 

**Type converters** in `Converters` map `DayOfWeek` ⟷ `Int` and `LocalTime` ⟷ `String` so the DAO can work with strongly typed Kotlin APIs while SQLite stores simple primitives.

A **unique index** across `(day_of_week, start_time, title)` prevents duplicate inserts of the same block, matching the previous in-memory deduping behavior.

## How Room is wired into the app
1. `ScheduleEntryEntity` defines the schema for `schedule_entries` with the fields listed above.
2. `ScheduleDao` exposes two operations:
   - `entries()` returns a `Flow<List<ScheduleEntryEntity>>` ordered by day/time for reactive UI updates.
   - `insertAll()` writes a batch of entities using `OnConflictStrategy.REPLACE` to honor the unique index.
3. `ScheduleDatabase` creates a singleton Room database (`schedule.db`) and surfaces the DAO. It registers the converters for `DayOfWeek` and `LocalTime`.
4. `ScheduleRepository` transforms between `ScheduleEntryEntity` and the domain `ScheduleEntry` model so callers never touch Room types. It streams DAO updates to the UI and batches writes from the ViewModel.
5. `SchedulerViewModel` now injects the Room-backed repository. On init it collects `repository.entries()` and merges the results into `UiState`, driving gym recommendations and the schedule list. On submit it parses user text, then calls `repository.addAll(parsed)` to persist.

## Migration guidance
- This is **schema version 1**, so no migration is required. Existing users from the in-memory build had no on-disk data, so there is nothing to migrate.
- Future schema changes must add a version bump and a `Migration` object or provide a manual data transform path to avoid dropping user data.

## Relationship to current features
- **Schedule storage:** The weekly schedule that powers gym recommendations and movement nudges now survives app restarts and device reboots.
- **Gym recommendations:** `SchedulerViewModel` feeds the persisted entries into `GymRecommendationUseCase`, so suggested slots remain consistent across sessions.
- **Movement nudges & focus timer:** These features read from the same `UiState.scheduleEntries`, which is hydrated from the database on launch.

With this wiring, the UI remains unchanged while persistence is handled transparently by Room behind the repository boundary.
