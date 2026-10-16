# TASK-004: Local Bound Service + Binder

## Goal

Connect the dashboard to `VehicleDataService` with a local Binder and observe its vehicle state.

## Scope

- Expose vehicle state and running status through `LocalBinder`.
- Bind while `MainActivity` is started and unbind when it stops; show connection status.
- Keep Start/Stop Intent commands and the single Service owned simulation loop from TASK-003.
- Test state delivery, repeated commands, and Activity unbind/rebind.

## Not Included

AIDL, remote process, Foreground Service, or a change to background policy.
