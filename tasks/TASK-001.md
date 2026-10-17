# TASK-001: Vehicle Dashboard UI

## Goal

Replace the default greeting with a vehicle dashboard in Jetpack Compose. Use static sample data to focus on UI structure and state presentation.

## Scope

- Show a vehicle name/status and sample metrics: speed, battery or fuel level, and temperature.
- Display values, units, and statuses consistently and legibly on a phone screen.
- Separate sample data from composables just enough to replace the data source in TASK-002.
- Provide a Compose Preview for the dashboard and check the UI on a device/emulator if available.

## Not Included

- No simulation engine, Android Service, Bound Service, or AIDL.
- No dependency added solely to build the dashboard.

## Completion Criteria

- Opening the app shows the sample dashboard instead of `Hello Android!`.
- The UI does not depend on a Service or run a timed simulation.
- The build succeeds; report verification results and changed files.
