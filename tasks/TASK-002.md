# TASK-002: Vehicle Simulation Engine

## Goal

Generate bounded mock vehicle data once per second while the user runs the simulation.

## Scope

- Keep the simulation logic separate from Compose.
- Expose vehicle state and running status through a ViewModel and StateFlow.
- Start one coroutine Job at a time; Stop cancels it and leaves the last values visible.
- Connect the existing dashboard controls and metrics without changing its layout.
- Unit test value bounds and Start/Stop timing.

## Not Included

- Android Service, IPC, AIDL, persistence, or background execution after the ViewModel is cleared.

## Completion Criteria

- Start updates the dashboard every second; repeated Start does not multiply updates.
- Stop prevents further updates; all metrics stay in reasonable ranges.
- Debug build and unit tests pass.
