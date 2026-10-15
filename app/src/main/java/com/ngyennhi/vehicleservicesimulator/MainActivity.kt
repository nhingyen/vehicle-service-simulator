package com.ngyennhi.vehicleservicesimulator

import android.graphics.Color
import android.os.Bundle
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
import com.ngyennhi.vehicleservicesimulator.ui.dashboard.VehicleDashboard
import com.ngyennhi.vehicleservicesimulator.ui.dashboard.VehicleViewModel
import com.ngyennhi.vehicleservicesimulator.ui.theme.VehicleServiceSimulatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        val viewModel = ViewModelProvider(this)[VehicleViewModel::class.java]
        setContent {
            val vehicleState by viewModel.vehicleState.collectAsState()
            val isRunning by viewModel.isRunning.collectAsState()
            VehicleServiceSimulatorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    VehicleDashboard(
                        vehicleState = vehicleState,
                        isRunning = isRunning,
                        onStart = viewModel::start,
                        onStop = viewModel::stop,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
