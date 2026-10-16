package com.ngyennhi.vehicleservicesimulator.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ngyennhi.vehicleservicesimulator.simulation.VehicleSimulationEngine
import com.ngyennhi.vehicleservicesimulator.simulation.VehicleSimulationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VehicleDataService : Service() {
    private val engine = VehicleSimulationEngine()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var simulationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}, startId=$startId")
        when (intent?.action) {
            ACTION_START -> startSimulation()
            ACTION_STOP -> {
                stopSimulation()
                stopSelf(startId)
            }
            else -> {
                Log.w(TAG, "Ignoring unknown or null action")
                if (simulationJob?.isActive != true) stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopSimulation()
        scope.cancel()
        super.onDestroy()
    }

    private fun startSimulation() {
        if (simulationJob?.isActive == true) {
            Log.d(TAG, "Simulation already running")
            return
        }

        VehicleSimulationState.setRunning(true)
        simulationJob = scope.launch {
            while (isActive) {
                delay(1_000)
                val next = engine.next(VehicleSimulationState.vehicleState.value)
                VehicleSimulationState.update(next)
                Log.d(TAG, "Simulation tick: $next")
            }
        }
        Log.d(TAG, "Simulation started")
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        VehicleSimulationState.setRunning(false)
        Log.d(TAG, "Simulation stopped")
    }

    companion object {
        const val ACTION_START = "com.ngyennhi.vehicleservicesimulator.action.START_SIMULATION"
        const val ACTION_STOP = "com.ngyennhi.vehicleservicesimulator.action.STOP_SIMULATION"
        private const val TAG = "VehicleDataService"
    }
}
