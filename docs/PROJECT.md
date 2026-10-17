# Vehicle Service Simulator

## Goal

Learn native Android development with Kotlin and Jetpack Compose, focusing on data flow and communication between the UI and Android Services. This is a practice project for an Android Developer Fresher application at LGEDV. All vehicle data is simulated.

## Current State

- One `:app` module; package/namespace and application ID: `com.ngyennhi.vehicleservicesimulator`.
- `MainActivity` binds while started to `VehicleDataService` in the `:vehicle` process. AIDL callbacks deliver vehicle snapshots to `VehicleViewModel`, and the dashboard shows connection status. Start/Stop Intent actions still control the single Service simulation loop, which updates state once per second.
- Current configuration: Kotlin 2.2.10, Android Gradle Plugin 9.4.1, Gradle wrapper 9.6.0, Compose Material 3, `minSdk` 26, and `compileSdk`/`targetSdk` 37.

## MVP Scope

- A dashboard displays simulated vehicle state, such as speed, battery/fuel level, temperature, and operating status.
- A simulation engine generates bounded state over time while the dashboard is running.
- The simulation moves into an Android Service; the UI then connects through a Bound Service, followed by AIDL practice.
- Add suitable tests for simulation logic and important integration flows.

## Out of Scope

- Real vehicles, CAN/OBD connections, hardware, or real telematics data.
- Backend services, accounts, cloud synchronization, or long-term storage.
- Multiple modules or new libraries without a concrete need.

## Roadmap

| Task | Expected outcome |
| --- | --- |
| TASK-001 | Compose dashboard with static sample data and no Service. |
| TASK-002 | Simulation engine generates and updates mock vehicle state; the UI consumes its data. |
| TASK-003 | Android Service owns the simulation lifecycle and exposes state through a suitable mechanism. |
| TASK-004 | UI connects to and receives state through a Bound Service. |
| TASK-005 | Add AIDL communication to practice crossing a process boundary. |
| TASK-006 | Test key flows, handle lifecycle and error cases, and finish demo documentation. |

TASK-001 through TASK-005 are complete. Start later tasks only when requested.
