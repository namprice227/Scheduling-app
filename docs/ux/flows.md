# Core flows

1. **Onboarding interview**
   1. Welcome card introduces AI helper.
   2. Chat bubble asks for Monday routine; text field accepts free-form input.
   3. Parser validates response, stores structured entry, and prompts for next day until Sunday is covered.
2. **Gym reminder**
   1. Repository tracks open windows; UI surfaces top suggestion.
   2. User confirms or requests alternate slot (future iteration).
3. **Movement nudges**
   1. Stand-up evaluator checks idle duration every 15 minutes via WorkManager.
   2. Notification + in-app card remind the user to move.
4. **Focus mode**
   1. User selects study duration (default 25 min) and taps Start.
   2. Timer locks orientation, counts down, and rewards streaks with celebratory copy.
