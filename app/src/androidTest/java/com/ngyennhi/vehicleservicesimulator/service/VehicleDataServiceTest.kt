package com.ngyennhi.vehicleservicesimulator.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class VehicleDataServiceTest {
    @Test
    fun remoteCallbacksReceiveInitialStateAndUnregisterStopsDelivery() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val stop = Intent(context, VehicleDataService::class.java)
            .setAction(VehicleDataService.ACTION_STOP)
        val start = Intent(context, VehicleDataService::class.java)
            .setAction(VehicleDataService.ACTION_START)

        val binders = LinkedBlockingQueue<IBinder>()
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                binders.offer(binder)
            }

            override fun onServiceDisconnected(name: ComponentName) = Unit
        }
        val bound = context.bindService(
            Intent(context, VehicleDataService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        assertTrue(bound)

        try {
            val binder = binders.poll(3, TimeUnit.SECONDS)
            assertNotNull(binder)
            assertFalse(binder is IVehicleDataService.Stub)
            val service = IVehicleDataService.Stub.asInterface(binder)
            val firstSnapshots = LinkedBlockingQueue<VehicleSnapshot>()
            val secondSnapshots = LinkedBlockingQueue<VehicleSnapshot>()
            val firstCallback = snapshotCallback(firstSnapshots)
            val secondCallback = snapshotCallback(secondSnapshots)

            try {
                service.registerCallback(firstCallback)
                assertNotNull(firstSnapshots.poll(3, TimeUnit.SECONDS))
                service.registerCallback(secondCallback)
                assertNotNull(secondSnapshots.poll(3, TimeUnit.SECONDS))

                context.startService(stop)
                assertTrue(waitUntil(3_000) { firstSnapshots.poll()?.isRunning == false })
                assertTrue(waitUntil(3_000) { secondSnapshots.poll()?.isRunning == false })
                service.unregisterCallback(firstCallback)
                context.startService(start)
                assertTrue(waitUntil(3_000) { secondSnapshots.poll()?.isRunning == true })
                assertNull(firstSnapshots.poll(500, TimeUnit.MILLISECONDS))
            } finally {
                service.unregisterCallback(firstCallback)
                service.unregisterCallback(secondCallback)
            }
        } finally {
            context.startService(stop)
            context.unbindService(connection)
            context.stopService(Intent(context, VehicleDataService::class.java))
        }
    }

    @Test
    fun registrationFailureDisconnectsAndRejectsOldCallbacks() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val viewModel = VehicleViewModel()
        val firstService = CallbackCapturingService()
        val failingService = FailingRegistrationService()
        val state = VehicleSnapshot(42, 80, 30, false, true)

        try {
            instrumentation.runOnMainSync { viewModel.connect(firstService) }
            assertTrue(waitUntil { firstService.callback != null })
            firstService.callback!!.onSnapshot(state)
            assertTrue(waitUntil { viewModel.isConnected.value && viewModel.isRunning.value })

            instrumentation.runOnMainSync { viewModel.connect(failingService) }
            assertTrue(waitUntil {
                failingService.unregisterCalls.get() == 1 &&
                    !viewModel.isConnected.value && !viewModel.isRunning.value
            })

            firstService.callback!!.onSnapshot(VehicleSnapshot(1, 1, 1, true, false))
            instrumentation.waitForIdleSync()
            assertEquals(state.toVehicleState(), viewModel.vehicleState.value)
        } finally {
            instrumentation.runOnMainSync { viewModel.disconnect() }
        }
    }

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
    fun movingActivityToBackgroundReleasesBinding() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val activity = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as MainActivity
        lateinit var viewModel: VehicleViewModel
        instrumentation.runOnMainSync {
            viewModel = ViewModelProvider(activity)[VehicleViewModel::class.java]
        }

        try {
            assertTrue(waitUntil { viewModel.isConnected.value })
            instrumentation.runOnMainSync { assertTrue(activity.moveTaskToBack(true)) }
            assertTrue(waitUntil { !viewModel.isConnected.value })
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
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
            assertTrue(waitUntil(8_000) { viewModel.vehicleState.value != initial })

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
            assertTrue(waitUntil(8_000) { secondViewModel.vehicleState.value != stateAtRebind })
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

    private fun snapshotCallback(queue: LinkedBlockingQueue<VehicleSnapshot>) =
        object : IVehicleStateCallback.Stub() {
            override fun onSnapshot(snapshot: VehicleSnapshot) {
                queue.offer(snapshot)
            }
        }

    private class CallbackCapturingService : IVehicleDataService.Stub() {
        @Volatile var callback: IVehicleStateCallback? = null

        override fun registerCallback(callback: IVehicleStateCallback) {
            this.callback = callback
        }

        override fun unregisterCallback(callback: IVehicleStateCallback) = Unit
    }

    private class FailingRegistrationService : IVehicleDataService.Stub() {
        val unregisterCalls = AtomicInteger()

        override fun registerCallback(callback: IVehicleStateCallback): Unit =
            throw RemoteException("Registration failed")

        override fun unregisterCallback(callback: IVehicleStateCallback) {
            unregisterCalls.incrementAndGet()
        }
    }
}
