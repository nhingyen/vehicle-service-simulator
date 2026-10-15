package com.ngyennhi.vehicleservicesimulator.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngyennhi.vehicleservicesimulator.data.SampleVehicleData
import com.ngyennhi.vehicleservicesimulator.model.VehicleState
import com.ngyennhi.vehicleservicesimulator.simulation.VehicleSimulationEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VehicleViewModel(
    private val engine: VehicleSimulationEngine = VehicleSimulationEngine()
) : ViewModel() {
    private val _vehicleState = MutableStateFlow(SampleVehicleData.vehicleState)
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var simulationJob: Job? = null

    fun start() {
        if (simulationJob?.isActive == true) return

        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _vehicleState.value = engine.next(_vehicleState.value)
            }
        }
        _isRunning.value = true
    }

    fun stop() {
        simulationJob?.cancel()
        simulationJob = null
        _isRunning.value = false
    }
}
