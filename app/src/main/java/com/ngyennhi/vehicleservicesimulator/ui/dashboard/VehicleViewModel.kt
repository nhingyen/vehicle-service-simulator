package com.ngyennhi.vehicleservicesimulator.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngyennhi.vehicleservicesimulator.data.SampleVehicleData
import com.ngyennhi.vehicleservicesimulator.model.VehicleState
import com.ngyennhi.vehicleservicesimulator.service.VehicleDataService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class VehicleViewModel : ViewModel() {
    private val _vehicleState = MutableStateFlow(SampleVehicleData.vehicleState)
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var stateJob: Job? = null
    private var runningJob: Job? = null

    fun connect(binder: VehicleDataService.LocalBinder) {
        disconnect()
        val serviceState = binder.vehicleState
        val serviceRunning = binder.isRunning
        _vehicleState.value = serviceState.value
        _isRunning.value = serviceRunning.value
        _isConnected.value = true
        stateJob = viewModelScope.launch {
            serviceState.collect { _vehicleState.value = it }
        }
        runningJob = viewModelScope.launch {
            serviceRunning.collect { _isRunning.value = it }
        }
    }

    fun disconnect() {
        stateJob?.cancel()
        runningJob?.cancel()
        stateJob = null
        runningJob = null
        _isConnected.value = false
    }
}
