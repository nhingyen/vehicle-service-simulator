# Architecture

## Current State

The project has one Android module, `:app`. `VehicleState` is a plain Kotlin model, `SampleVehicleData` provides one static value, and `MainActivity` passes it to `VehicleDashboard`. The dashboard uses the existing Compose theme and displays the state. Start/Stop controls are disabled. There is no simulation engine, Android Service, or IPC yet.

The app uses a dark Material 3 palette by default. Dynamic system colors are disabled by default so the dashboard keeps its defined colors.

```text
VehicleState → SampleVehicleData → MainActivity → VehicleDashboard
```

## Task Evolution

```text
TASK-001  Compose Dashboard ← static sample data (complete)
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
