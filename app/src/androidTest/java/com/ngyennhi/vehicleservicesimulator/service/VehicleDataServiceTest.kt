package com.ngyennhi.vehicleservicesimulator.service

import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ngyennhi.vehicleservicesimulator.MainActivity
import com.ngyennhi.vehicleservicesimulator.ui.dashboard.VehicleViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehicleDataServiceTest {
    @Test
    fun repeatedCommandsStopUpdatesAndKeepLastState() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val activity = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as MainActivity
        lateinit var viewModel: VehicleViewModel
        instrumentation.runOnMainSync {
            viewModel = ViewModelProvider(activity)[VehicleViewModel::class.java]
        }
        val start = Intent(context, VehicleDataService::class.java)
            .setAction(VehicleDataService.ACTION_START)
        val stop = Intent(context, VehicleDataService::class.java)
            .setAction(VehicleDataService.ACTION_STOP)

        try {
            assertTrue(waitUntil { viewModel.isConnected.value })
            assertFalse(viewModel.isRunning.value)
            context.startService(start)
            context.startService(start)
            assertTrue(waitUntil { viewModel.isRunning.value })

            val initial = viewModel.vehicleState.value
            assertTrue(waitUntil(5_000) { viewModel.vehicleState.value != initial })

            context.startService(stop)
            context.startService(stop)
            assertTrue(waitUntil { !viewModel.isRunning.value })
            val stopped = viewModel.vehicleState.value
            SystemClock.sleep(1_200)
            assertEquals(stopped, viewModel.vehicleState.value)
            assertFalse(viewModel.isRunning.value)
            assertTrue(viewModel.isConnected.value)
            instrumentation.runOnMainSync { activity.finish() }
            assertTrue(waitUntil { !viewModel.isConnected.value })
        } finally {
            context.stopService(Intent(context, VehicleDataService::class.java))
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test
    fun startedSimulationSurvivesActivityUnbindAndRebind() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val firstActivity = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as MainActivity
        lateinit var firstViewModel: VehicleViewModel
        instrumentation.runOnMainSync {
            firstViewModel = ViewModelProvider(firstActivity)[VehicleViewModel::class.java]
        }
        var secondActivity: MainActivity? = null

        try {
            assertTrue(waitUntil { firstViewModel.isConnected.value })
            context.startService(
                Intent(context, VehicleDataService::class.java)
                    .setAction(VehicleDataService.ACTION_START)
            )
            assertTrue(waitUntil { firstViewModel.isRunning.value })
            instrumentation.runOnMainSync { firstActivity.finish() }
            assertTrue(waitUntil { !firstViewModel.isConnected.value })

            val reopened = instrumentation.startActivitySync(
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ) as MainActivity
            secondActivity = reopened
            lateinit var secondViewModel: VehicleViewModel
            instrumentation.runOnMainSync {
                secondViewModel = ViewModelProvider(reopened)[VehicleViewModel::class.java]
            }
            assertTrue(waitUntil { secondViewModel.isConnected.value })
            assertTrue(secondViewModel.isRunning.value)
            val stateAtRebind = secondViewModel.vehicleState.value
            assertTrue(waitUntil(5_000) { secondViewModel.vehicleState.value != stateAtRebind })
        } finally {
            context.stopService(Intent(context, VehicleDataService::class.java))
            instrumentation.runOnMainSync {
                secondActivity?.finish()
                firstActivity.finish()
            }
        }
    }

    private fun waitUntil(timeoutMs: Long = 2_000, condition: () -> Boolean): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return true
            SystemClock.sleep(20)
        }
        return condition()
    }
}
