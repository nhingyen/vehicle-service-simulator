# TASK-003: Android Service & Lifecycle

## Goal

Move the simulation loop from `VehicleViewModel` into a Started Service.

## Scope

- Handle Start and Stop Intent actions safely, including repeated commands.
- Let the Service own the coroutine scope and simulation Job, and clean them up on destruction.
- Reuse `VehicleSimulationEngine` and log Service lifecycle events and simulation ticks.
- Keep the dashboard state available within the app process while the Service runs.

## Not Included

Bound Service, IPC, AIDL, Foreground Service, or persistence across process death.
