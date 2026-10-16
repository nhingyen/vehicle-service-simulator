package com.ngyennhi.vehicleservicesimulator.service

import android.content.Intent
import android.os.SystemClock
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
        )
        val viewModel = VehicleViewModel()
        val start = Intent(context, VehicleDataService::class.java)
            .setAction(VehicleDataService.ACTION_START)
        val stop = Intent(context, VehicleDataService::class.java)
            .setAction(VehicleDataService.ACTION_STOP)

        try {
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
        } finally {
            context.stopService(Intent(context, VehicleDataService::class.java))
            instrumentation.runOnMainSync { activity.finish() }
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
