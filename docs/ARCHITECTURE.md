# Architecture

## Current State

The project has one Android module, `:app`. `VehicleState` is a plain Kotlin model and `SampleVehicleData` supplies the initial value. `VehicleSimulationEngine` generates bounded values without Android or Compose dependencies. `VehicleDataService` is a Started Service that owns the coroutine Job and scope. `MainActivity` sends Start/Stop Intent actions and collects in-process `StateFlow` values through `VehicleViewModel`. Repeated Start does not launch another loop; Stop cancels the Job, retains the latest state, and stops the Service. Service destruction cancels its scope. The Service uses `START_NOT_STICKY`; it is not a Foreground Service, so Android may stop it in the background. State resets if the process dies. Bound Service and IPC are planned for later tasks.

The app uses a dark Material 3 palette by default. Dynamic system colors are disabled by default so the dashboard keeps its defined colors.

```text
VehicleSimulationEngine → VehicleDataService → VehicleSimulationState
                                 ↑                     ↓
                      Start/Stop Intent           VehicleViewModel
                                 ↑                     ↓
                            MainActivity → VehicleDashboard
```

## Task Evolution

```text
TASK-001  Compose Dashboard ← static sample data (complete)
TASK-002  Compose Dashboard ← ViewModel StateFlow ← Simulation Engine (complete)
TASK-003  Simulation Engine ← lifecycle managed by Started Service (complete)
TASK-004  Compose Dashboard ↔ Bound Service ↔ Simulation Engine
TASK-005  Compose Dashboard ↔ AIDL ↔ Service ↔ Simulation Engine
```

- **UI:** displays shared state through the ViewModel and sends Start/Stop Intent actions to the Service.
- **Simulation Engine:** Kotlin logic that simulates vehicle state, separate from the UI so it can be tested.
- **Service:** manages the simulation Job and coroutine scope according to the Android lifecycle.
- **Bound Service:** provides an API for the UI to connect, disconnect, and receive state.
- **AIDL:** defines an IPC contract when the project crosses a process boundary; add it after the basic Bound Service flow works.

Keep the structure simple and in one module until a concrete requirement calls for more. See [`PROJECT.md`](PROJECT.md) for scope and task order.
