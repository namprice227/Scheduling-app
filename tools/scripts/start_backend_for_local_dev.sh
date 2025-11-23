#!/usr/bin/env bash
set -euo pipefail

BACKEND_BIN="C:/Users/nampr/Desktop/Scheduling-app/backend/api/build/install/api/bin/api"

if pgrep -f "com.example.scheduler.backend.ServerKt" > /dev/null; then
  echo "Backend API already running on port 8080"
else
  nohup "$BACKEND_BIN" >/tmp/backend-api.log 2>&1 &
  echo "Started backend API (logs: /tmp/backend-api.log)"
fi
