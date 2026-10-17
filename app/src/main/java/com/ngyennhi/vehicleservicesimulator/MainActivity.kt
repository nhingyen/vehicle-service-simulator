package com.ngyennhi.vehicleservicesimulator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.ngyennhi.vehicleservicesimulator.ipc.IVehicleDataService
import com.ngyennhi.vehicleservicesimulator.service.VehicleDataService
import com.ngyennhi.vehicleservicesimulator.ui.dashboard.VehicleDashboard
import com.ngyennhi.vehicleservicesimulator.ui.dashboard.VehicleViewModel
import com.ngyennhi.vehicleservicesimulator.ui.theme.VehicleServiceSimulatorTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: VehicleViewModel
    private var isBound = false
    private var bindingRecoveryUsed = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            if (isBound) viewModel.connect(IVehicleDataService.Stub.asInterface(binder))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            viewModel.disconnect()
        }

        override fun onBindingDied(name: ComponentName) {
            viewModel.disconnect()
            if (isBound) {
                unbindService(this)
                isBound = false
                if (!bindingRecoveryUsed) {
                    bindingRecoveryUsed = true
                    bindVehicleService()
                }
            }
        }

        override fun onNullBinding(name: ComponentName) {
            viewModel.disconnect()
            if (isBound) {
                unbindService(this)
                isBound = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        viewModel = ViewModelProvider(this)[VehicleViewModel::class.java]
        setContent {
            val vehicleState by viewModel.vehicleState.collectAsState()
            val isRunning by viewModel.isRunning.collectAsState()
            val isConnected by viewModel.isConnected.collectAsState()
            VehicleServiceSimulatorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    VehicleDashboard(
                        vehicleState = vehicleState,
                        isRunning = isRunning,
                        isConnected = isConnected,
                        onStart = { sendServiceCommand(VehicleDataService.ACTION_START) },
                        onStop = { sendServiceCommand(VehicleDataService.ACTION_STOP) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindingRecoveryUsed = false
        bindVehicleService()
    }

    private fun bindVehicleService() {
        if (isBound) return
        isBound = bindService(
            Intent(this, VehicleDataService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        if (!isBound) viewModel.disconnect()
    }

    override fun onStop() {
        viewModel.disconnect()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onStop()
    }

    private fun sendServiceCommand(action: String) {
        startService(Intent(this, VehicleDataService::class.java).setAction(action))
    }
}
