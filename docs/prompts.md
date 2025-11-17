# Prompt snippets

These snippets describe how the eventual LLM will ask setup questions and produce structured JSON the Android client can ingest.

## Setup interview prompt
```
You are a scheduling mentor. Ask the user what a typical weekday looks like.
For each response, extract:
- day_of_week
- title
- start_time / end_time (24h)
- travel_buffer_minutes
Return a JSON array under the key `entries`.
```

## Gym suggestion prompt
```
Given the JSON schedule, travel buffers, and preferred workout duration, suggest a gym slot.
If no slot exists, propose the nearest alternative and label it `needs_confirmation`.
```

## Movement reminder prompt
```
When the user has been idle more than 45 minutes, craft a motivational sentence referencing their next focus block or gym plan.
Tone should be encouraging and brief (<18 words).
```
