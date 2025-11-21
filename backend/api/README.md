# Backend API

This lightweight Ktor server exposes a single endpoint to bulk-import activities into a local SQLite database.

## Prerequisites

- JDK 17 or newer
- No external database setup is required; the server creates an `activities.db` SQLite file in the working directory on first run.

## Running the server

From the repository root:

```
./gradlew :backend:api:run
```

The server listens on `http://localhost:8080`.

You can verify the server is running by visiting `http://localhost:8080/` or issuing:

```
curl http://localhost:8080/
```

The response should be:

```
working
```

> **Tip:** The Android app module automatically starts the backend in the background during `preBuild` (via `startBackendForLocalDev`).
> Ensure `bash` and `pgrep` are available on your machine so the task can spawn the server script without manual steps.

If you prefer to build an executable JAR, you can run:

```
./gradlew :backend:api:shadowJar
java -jar backend/api/build/libs/api-all.jar
```

## Bulk insert endpoint

**Request**

- **Method:** `POST`
- **Path:** `/activities/bulk`
- **Body:** JSON array of activities

Each activity supports:

- `title` (String)
- `dayOfWeek` (String, e.g., `"MONDAY"` or `"Monday"`)
- `startTime` (String, `HH:mm`)
- `endTime` (String, `HH:mm`)
- `location` (String, optional)
- `travelBufferMinutes` (Int, optional)

### Example request

Use `curl` to send a bulk payload:

```
curl -X POST http://localhost:8080/activities/bulk \
  -H "Content-Type: application/json" \
  -d '[
    {
      "title": "Morning Run",
      "dayOfWeek": "Monday",
      "startTime": "06:30",
      "endTime": "07:15",
      "location": "Park",
      "travelBufferMinutes": 10
    },
    {
      "title": "Study",
      "dayOfWeek": "MONDAY",
      "startTime": "09:00",
      "endTime": "10:30"
    }
  ]'
```

### Responses

- `201 Created` with `{ "inserted": <count> }` when rows are added.
- `400 Bad Request` with `{ "message": "..." }` for invalid JSON, empty payloads, or malformed day-of-week strings.

### Verifying data in SQLite

After inserts, you can inspect the database with the SQLite CLI:

```
sqlite3 activities.db 'SELECT id, title, dayOfWeek, startTime, endTime FROM activities;'
```

Ensure you run the command from the same directory where the server was started (where `activities.db` was created).
