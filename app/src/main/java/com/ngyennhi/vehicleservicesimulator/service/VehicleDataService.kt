package com.ngyennhi.vehicleservicesimulator.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import com.ngyennhi.vehicleservicesimulator.ipc.IVehicleDataService
import com.ngyennhi.vehicleservicesimulator.ipc.IVehicleStateCallback
import com.ngyennhi.vehicleservicesimulator.ipc.VehicleSnapshot
import com.ngyennhi.vehicleservicesimulator.simulation.VehicleSimulationEngine
import com.ngyennhi.vehicleservicesimulator.simulation.VehicleSimulationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VehicleDataService : Service() {
    private val engine = VehicleSimulationEngine()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var simulationJob: Job? = null
    private val callbacks = RemoteCallbackList<IVehicleStateCallback>()
    private val callbackEvents = Channel<CallbackEvent>(Channel.UNLIMITED)
    private val callbackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var latestSnapshot = snapshot()

    private val callbackJob = callbackScope.launch {
        try {
            for (event in callbackEvents) {
                when (event) {
                    is CallbackEvent.Register -> {
                        if (callbacks.register(event.callback)) {
                            try {
                                event.callback.onSnapshot(latestSnapshot)
                            } catch (_: RemoteException) {
                                callbacks.unregister(event.callback)
                            }
                        }
                    }
                    is CallbackEvent.Unregister -> callbacks.unregister(event.callback)
                    is CallbackEvent.Update -> broadcast(event.snapshot)
                }
            }
        } finally {
            callbacks.kill()
        }
    }

    private val binder = object : IVehicleDataService.Stub() {
        override fun registerCallback(callback: IVehicleStateCallback) {
            callbackEvents.trySend(CallbackEvent.Register(callback))
        }

        override fun unregisterCallback(callback: IVehicleStateCallback) {
            callbackEvents.trySend(CallbackEvent.Unregister(callback))
        }
    }

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

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopSimulation()
        scope.cancel()
        callbackEvents.close()
        callbackScope.cancel()
        super.onDestroy()
    }

    private fun startSimulation() {
        if (simulationJob?.isActive == true) {
            Log.d(TAG, "Simulation already running")
            return
        }

        VehicleSimulationState.setRunning(true)
        publishSnapshot()
        simulationJob = scope.launch {
            while (isActive) {
                delay(1_000)
                val next = engine.next(VehicleSimulationState.vehicleState.value)
                VehicleSimulationState.update(next)
                publishSnapshot()
                Log.d(TAG, "Simulation tick: $next")
            }
        }
        Log.d(TAG, "Simulation started")
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        VehicleSimulationState.setRunning(false)
        publishSnapshot()
        Log.d(TAG, "Simulation stopped")
    }

    private fun snapshot() = VehicleSnapshot.from(
        VehicleSimulationState.vehicleState.value,
        VehicleSimulationState.isRunning.value
    )

    private fun publishSnapshot() {
        latestSnapshot = snapshot()
        callbackEvents.trySend(CallbackEvent.Update(latestSnapshot))
    }

    private fun broadcast(snapshot: VehicleSnapshot) {
        val count = callbacks.beginBroadcast()
        try {
            for (index in 0 until count) {
                try {
                    callbacks.getBroadcastItem(index).onSnapshot(snapshot)
                } catch (_: RemoteException) {
                    // RemoteCallbackList removes clients when their binder dies.
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }

    private sealed interface CallbackEvent {
        data class Register(val callback: IVehicleStateCallback) : CallbackEvent
        data class Unregister(val callback: IVehicleStateCallback) : CallbackEvent
        data class Update(val snapshot: VehicleSnapshot) : CallbackEvent
    }

    companion object {
        const val ACTION_START = "com.ngyennhi.vehicleservicesimulator.action.START_SIMULATION"
        const val ACTION_STOP = "com.ngyennhi.vehicleservicesimulator.action.STOP_SIMULATION"
        private const val TAG = "VehicleDataService"
    }
}
