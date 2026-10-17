# Architecture

## Current State

The project has one Android module, `:app`, and `VehicleDataService` runs in the app's `:vehicle` process. `VehicleState` and `VehicleSimulationEngine` remain plain Kotlin. The Started Service owns the simulation Job and keeps its state in that process. Its AIDL Binder accepts callback registration; `VehicleSnapshot` is the Parcelable IPC DTO carrying vehicle values and running status. A single background coroutine serializes callback registration and delivery through `RemoteCallbackList`, so Binder threads do not touch the simulation Job and callback calls do not block its main thread.

`MainActivity` binds in `onStart` and unbinds in `onStop`. `VehicleViewModel` registers the callback on `Dispatchers.IO`, converts snapshots to `VehicleState`, and ignores callbacks from previous connections. A dead binding gets at most one explicit rebind per Activity start; ordinary Service disconnects rely on Android's existing binding to reconnect. Start/Stop remain explicit Intent actions. Repeated Start does not launch another loop; Stop cancels the Job, retains the latest state, and stops the started Service (a bound instance remains until unbound). The Service uses `START_NOT_STICKY`; it is not a Foreground Service, so Android may stop it in the background. State resets if the `:vehicle` process dies.

The app uses a dark Material 3 palette by default. Dynamic system colors are disabled by default so the dashboard keeps its defined colors.

```text
VehicleSimulationEngine → VehicleDataService (:vehicle) → VehicleSimulationState
MainActivity --Start/Stop Intent--> VehicleDataService (:vehicle)
MainActivity --bind/unbind--> IVehicleDataService (AIDL)
IVehicleStateCallback --VehicleSnapshot--> VehicleViewModel → VehicleDashboard
```

## Task Evolution

```text
TASK-001  Compose Dashboard ← static sample data (complete)
TASK-002  Compose Dashboard ← ViewModel StateFlow ← Simulation Engine (complete)
TASK-003  Simulation Engine ← lifecycle managed by Started Service (complete)
TASK-004  Compose Dashboard ↔ Bound Service ↔ Simulation Engine (complete)
TASK-005  Compose Dashboard ↔ AIDL ↔ Service ↔ Simulation Engine (complete)
TASK-006  Tests, lifecycle checks, and demo documentation (implemented locally; CI run pending)
```

- **UI:** displays bound Service state through the ViewModel and sends Start/Stop Intent actions to the Service.
- **Simulation Engine:** Kotlin logic that simulates vehicle state, separate from the UI so it can be tested.
- **Service:** manages the simulation Job and coroutine scope according to the Android lifecycle.
- **Bound Service:** exposes an AIDL Binder; the Activity owns its binding lifecycle.
- **AIDL:** registers callbacks and delivers Parcelable snapshots from `:vehicle` to the UI process.

## Verification and remaining checks

Unit tests cover engine bounds, per-step changes, battery depletion, and door behavior. Instrumentation tests cover Parcelable round trips, real cross-process callback registration/unregistration, repeated Service commands, Activity unbind/rebind and background unbind, and ViewModel handling of stale callbacks and registration failure. The existing CI workflow runs build, unit tests, and lint, then starts an API 35 emulator for instrumentation tests. The new emulator step has not yet been verified on GitHub.

Process death, `onBindingDied` recovery, and return from background still need manual checks on a device or emulator. The Service does not persist state across process death or promise continuous background updates.

Keep the structure simple and in one module until a concrete requirement calls for more. See [`PROJECT.md`](PROJECT.md) for scope and task order.
