package com.ngyennhi.vehicleservicesimulator.simulation

import com.ngyennhi.vehicleservicesimulator.data.SampleVehicleData
import com.ngyennhi.vehicleservicesimulator.model.VehicleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** State owned by the Service process; the dashboard receives snapshots over AIDL. */
internal object VehicleSimulationState {
    private val _vehicleState = MutableStateFlow(SampleVehicleData.vehicleState)
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun update(state: VehicleState) {
        _vehicleState.value = state
    }

    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }
}
