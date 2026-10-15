package com.ngyennhi.vehicleservicesimulator.ui.dashboard

import com.ngyennhi.vehicleservicesimulator.data.SampleVehicleData
import com.ngyennhi.vehicleservicesimulator.simulation.VehicleSimulationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {
    @Test
    fun viewModelProviderCanUseNoArgConstructor() {
        VehicleViewModel::class.java.getDeclaredConstructor().newInstance()
    }

    @Test
    fun startRunsOncePerSecondAndStopCancelsUpdates() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = VehicleViewModel(VehicleSimulationEngine(Random(7)))
        val expectedEngine = VehicleSimulationEngine(Random(7))

        try {
            val initial = SampleVehicleData.vehicleState
            assertEquals(initial, viewModel.vehicleState.value)
            assertFalse(viewModel.isRunning.value)

            viewModel.start()
            viewModel.start()
            runCurrent()
            assertTrue(viewModel.isRunning.value)
            advanceTimeBy(999)
            runCurrent()
            assertEquals(initial, viewModel.vehicleState.value)

            advanceTimeBy(1)
            runCurrent()
            val firstTick = expectedEngine.next(initial)
            assertEquals(firstTick, viewModel.vehicleState.value)

            viewModel.stop()
            assertFalse(viewModel.isRunning.value)
            advanceTimeBy(5_000)
            runCurrent()
            assertEquals(firstTick, viewModel.vehicleState.value)

            viewModel.start()
            runCurrent()
            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(expectedEngine.next(firstTick), viewModel.vehicleState.value)
        } finally {
            viewModel.stop()
            Dispatchers.resetMain()
        }
    }
}
