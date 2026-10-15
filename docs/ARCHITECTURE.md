# Architecture

## Current State

The project has one Android module, `:app`. `VehicleState` is a plain Kotlin model and `SampleVehicleData` supplies the initial value. `VehicleSimulationEngine` generates bounded values without Android or Compose dependencies. `VehicleViewModel` owns the current `StateFlow`, running status, and one coroutine Job. `MainActivity` collects both flows and passes values and Start/Stop actions to the existing dashboard. Stop cancels the Job and keeps the latest state. There is no Android Service or IPC yet.

The app uses a dark Material 3 palette by default. Dynamic system colors are disabled by default so the dashboard keeps its defined colors.

```text
SampleVehicleData → VehicleViewModel → MainActivity → VehicleDashboard
                           ↑
                 VehicleSimulationEngine
```

## Task Evolution

```text
TASK-001  Compose Dashboard ← static sample data (complete)
TASK-002  Compose Dashboard ← ViewModel StateFlow ← Simulation Engine (complete)
TASK-003  Simulation Engine ← lifecycle managed by Android Service
TASK-004  Compose Dashboard ↔ Bound Service ↔ Simulation Engine
TASK-005  Compose Dashboard ↔ AIDL ↔ Service ↔ Simulation Engine
```

- **UI:** displays state and sends Start/Stop actions to the ViewModel.
- **Simulation Engine:** Kotlin logic that simulates vehicle state, separate from the UI so it can be tested.
- **Service:** manages the simulation according to the Android lifecycle. Choose background behavior when that task defines the need.
- **Bound Service:** provides an API for the UI to connect, disconnect, and receive state.
- **AIDL:** defines an IPC contract when the project crosses a process boundary; add it after the basic Bound Service flow works.

Keep the structure simple and in one module until a concrete requirement calls for more. See [`PROJECT.md`](PROJECT.md) for scope and task order.
