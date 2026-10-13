# Architecture

## Current State

The project has one Android module, `:app`. `MainActivity` calls `setContent` to render the default Compose screen; `ui/theme` contains the default theme. There is no data layer, simulation engine, Android Service, or IPC yet.

```text
MainActivity → Compose UI (default greeting)
```

## Planned Evolution

```text
TASK-001  Compose Dashboard ← static sample data
TASK-002  Compose Dashboard ← Simulation Engine state
TASK-003  Simulation Engine ← lifecycle managed by Android Service
TASK-004  Compose Dashboard ↔ Bound Service ↔ Simulation Engine
TASK-005  Compose Dashboard ↔ AIDL ↔ Service ↔ Simulation Engine
```

- **UI:** displays state and sends user actions. TASK-001 uses sample data and needs no Service.
- **Simulation Engine:** Kotlin logic that simulates vehicle state, separate from the UI so it can be tested.
- **Service:** manages the simulation according to the Android lifecycle. Choose background behavior when that task defines the need.
- **Bound Service:** provides an API for the UI to connect, disconnect, and receive state.
- **AIDL:** defines an IPC contract when the project crosses a process boundary; add it after the basic Bound Service flow works.

Keep the structure simple and in one module until a concrete requirement calls for more. See [`PROJECT.md`](PROJECT.md) for scope and task order.
