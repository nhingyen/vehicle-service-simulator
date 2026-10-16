# Architecture

## Current State

The project has one Android module, `:app`. `VehicleState` is a plain Kotlin model and `SampleVehicleData` supplies the initial value. `VehicleSimulationEngine` generates bounded values without Android or Compose dependencies. `VehicleDataService` is a Started Service that owns the coroutine Job and scope. Its local Binder exposes read-only in-process `StateFlow` values from `VehicleSimulationState`. `MainActivity` binds in `onStart`, passes the Binder to `VehicleViewModel`, and unbinds in `onStop`; the ViewModel cancels its collectors when disconnected. Start/Stop still use Intent actions. Repeated Start does not launch another loop; Stop cancels the Job, retains the latest state, and stops the started Service (a bound instance remains until unbound). Service destruction cancels its scope. The Service uses `START_NOT_STICKY`; it is not a Foreground Service, so Android may stop it in the background. State resets if the process dies. IPC is planned for a later task.

The app uses a dark Material 3 palette by default. Dynamic system colors are disabled by default so the dashboard keeps its defined colors.

```text
VehicleSimulationEngine → VehicleDataService → VehicleSimulationState
MainActivity --Start/Stop Intent--> VehicleDataService
MainActivity --bind/unbind--> VehicleDataService.LocalBinder
VehicleDataService.LocalBinder --StateFlow--> VehicleViewModel → VehicleDashboard
```

## Task Evolution

```text
TASK-001  Compose Dashboard ← static sample data (complete)
TASK-002  Compose Dashboard ← ViewModel StateFlow ← Simulation Engine (complete)
TASK-003  Simulation Engine ← lifecycle managed by Started Service (complete)
TASK-004  Compose Dashboard ↔ Bound Service ↔ Simulation Engine (complete)
TASK-005  Compose Dashboard ↔ AIDL ↔ Service ↔ Simulation Engine
```

- **UI:** displays bound Service state through the ViewModel and sends Start/Stop Intent actions to the Service.
- **Simulation Engine:** Kotlin logic that simulates vehicle state, separate from the UI so it can be tested.
- **Service:** manages the simulation Job and coroutine scope according to the Android lifecycle.
- **Bound Service:** exposes local `StateFlow` values through `LocalBinder`; the Activity owns its binding lifecycle.
- **AIDL:** defines an IPC contract when the project crosses a process boundary; add it after the basic Bound Service flow works.

Keep the structure simple and in one module until a concrete requirement calls for more. See [`PROJECT.md`](PROJECT.md) for scope and task order.
