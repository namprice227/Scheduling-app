# Backend API

This lightweight Ktor server exposes endpoints to bulk-import activities into a local SQLite database and to
convert free-form activity text into normalized JSON the mobile client can consume.

## Prerequisites

- JDK 17 or newer
- No external database setup is required; the server creates an `activities.db` SQLite file in the working directory on first run.

## Running the server

From the repository root:

```
./gradlew :backend:api:run
```
```
./gradlew :backend:api:shadowJar
java -jar backend/api/build/libs/api-all.jar
```
The server listens on `http://localhost:8080` and creates `activities.db` in the working directory.

## Natural-language parsing endpoint

**Request**

- **Method:** `POST`
- **Path:** `/activities/parse`
- **Body:** JSON object with
  - `lines`: array of free-form activity descriptions (one per activity)
  - `persist` (optional): boolean. When `true` (default), parsed items are inserted into SQLite after parsing.

Each description can include a day of week, one or two times, an optional travel buffer, and a short title. The
parser mirrors the Android `NaturalLanguagePlanner`, so inputs like `"Monday gym 7-8pm travel 15m"` become
structured activities with start/end times and travel buffers.

### Example request

```
curl -X POST http://localhost:8080/activities/parse \
  -H "Content-Type: application/json" \
  -d '{
    "lines": [
      "Monday gym 7-8pm travel 15m",
      "Tuesday study 9am-11am"
    ],
    "persist": true
  }'
```

### Responses

- `200 OK` with a JSON body containing normalized activities, any per-line parsing errors, and the count inserted
  into SQLite.
- `400 Bad Request` with `{ "message": "..." }` for invalid JSON or empty inputs.

**Example response**

```
{
  "activities": [
    {
      "title": "Gym",
      "dayOfWeek": "MONDAY",
      "startTime": "19:00",
      "endTime": "20:00",
      "location": null,
      "travelBufferMinutes": 15
    },
    {
      "title": "Study",
      "dayOfWeek": "TUESDAY",
      "startTime": "09:00",
      "endTime": "11:00",
      "location": null,
      "travelBufferMinutes": 0
    }
  ],
  "errors": [],
  "inserted": 2
}
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
**Example payload**

```
[
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
]
```

**Response**

- `201 Created` with `{ "inserted": <count> }` when rows are added.
- `400 Bad Request` with `{ "message": "..." }` for invalid JSON, empty payloads, or malformed day-of-week strings.
