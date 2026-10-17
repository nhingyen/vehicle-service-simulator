package com.ngyennhi.vehicleservicesimulator.service

import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ngyennhi.vehicleservicesimulator.MainActivity
import com.ngyennhi.vehicleservicesimulator.ipc.IVehicleDataService
import com.ngyennhi.vehicleservicesimulator.ipc.IVehicleStateCallback
import com.ngyennhi.vehicleservicesimulator.ipc.VehicleSnapshot
import com.ngyennhi.vehicleservicesimulator.ui.dashboard.VehicleViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehicleDataServiceTest {
    @Test
    fun lateCallbackFromOldConnectionIsIgnored() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val viewModel = VehicleViewModel()
        val firstService = CallbackCapturingService()
        val secondService = CallbackCapturingService()

        try {
            instrumentation.runOnMainSync { viewModel.connect(firstService) }
            assertTrue(waitUntil { firstService.callback != null })
            val oldCallback = firstService.callback!!

            instrumentation.runOnMainSync { viewModel.connect(secondService) }
            assertTrue(waitUntil { secondService.callback != null })
            val current = VehicleSnapshot(42, 80, 30, false, true)
            secondService.callback!!.onSnapshot(current)
            assertTrue(waitUntil { viewModel.vehicleState.value == current.toVehicleState() })

            oldCallback.onSnapshot(VehicleSnapshot(1, 1, 1, true, false))
            instrumentation.waitForIdleSync()
            assertEquals(current.toVehicleState(), viewModel.vehicleState.value)
            assertTrue(viewModel.isConnected.value)

            instrumentation.runOnMainSync { viewModel.disconnect() }
            oldCallback.onSnapshot(VehicleSnapshot(2, 2, 2, true, true))
            instrumentation.waitForIdleSync()
            assertFalse(viewModel.isConnected.value)
            assertEquals(current.toVehicleState(), viewModel.vehicleState.value)
        } finally {
            instrumentation.runOnMainSync { viewModel.disconnect() }
        }
    }

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

    private class CallbackCapturingService : IVehicleDataService.Stub() {
        @Volatile var callback: IVehicleStateCallback? = null

        override fun registerCallback(callback: IVehicleStateCallback) {
            this.callback = callback
        }

        override fun unregisterCallback(callback: IVehicleStateCallback) = Unit
    }
}
