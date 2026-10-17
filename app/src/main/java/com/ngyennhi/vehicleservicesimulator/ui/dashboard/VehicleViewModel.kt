package com.ngyennhi.vehicleservicesimulator.ui.dashboard

import android.os.RemoteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngyennhi.vehicleservicesimulator.data.SampleVehicleData
import com.ngyennhi.vehicleservicesimulator.ipc.IVehicleDataService
import com.ngyennhi.vehicleservicesimulator.ipc.IVehicleStateCallback
import com.ngyennhi.vehicleservicesimulator.ipc.VehicleSnapshot
import com.ngyennhi.vehicleservicesimulator.model.VehicleState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleViewModel : ViewModel() {
    private val _vehicleState = MutableStateFlow(SampleVehicleData.vehicleState)
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var registrationJob: Job? = null
    private var generation = 0

    fun connect(service: IVehicleDataService) {
        disconnect()
        val session = generation
        val callback = object : IVehicleStateCallback.Stub() {
            override fun onSnapshot(snapshot: VehicleSnapshot) {
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (session != generation) return@launch
                    _vehicleState.value = snapshot.toVehicleState()
                    _isRunning.value = snapshot.isRunning
                    _isConnected.value = true
                }
            }
        }

        registrationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                service.registerCallback(callback)
                awaitCancellation()
            } catch (_: RemoteException) {
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (session == generation) disconnect()
                }
            } finally {
                try {
                    service.unregisterCallback(callback)
                } catch (_: RemoteException) {
                    // The remote process has already gone away.
                }
            }
        }
    }

    fun disconnect() {
        generation++
        registrationJob?.cancel()
        registrationJob = null
        _isConnected.value = false
        _isRunning.value = false
    }
}
