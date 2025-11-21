# Backend API

This lightweight Ktor server exposes a single endpoint to bulk-import activities into a local SQLite database.

## Running the server

```
./gradlew :backend:api:run
```

The server listens on `http://localhost:8080` and creates `activities.db` in the working directory.

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
