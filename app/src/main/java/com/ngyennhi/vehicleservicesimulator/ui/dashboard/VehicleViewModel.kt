package com.ngyennhi.vehicleservicesimulator.ui.dashboard

import androidx.lifecycle.ViewModel
import com.ngyennhi.vehicleservicesimulator.simulation.VehicleSimulationState

class VehicleViewModel : ViewModel() {
    val vehicleState = VehicleSimulationState.vehicleState
    val isRunning = VehicleSimulationState.isRunning
}
