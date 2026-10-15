package com.ngyennhi.vehicleservicesimulator.simulation

import com.ngyennhi.vehicleservicesimulator.model.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class VehicleSimulationEngineTest {
    private val engine = VehicleSimulationEngine(Random(42))

    @Test
    fun valuesRemainWithinBounds() {
        var state = VehicleState(speed = 500, battery = 150, temperature = -30, doorOpen = true)

        repeat(5_000) {
            state = engine.next(state)
            assertTrue(state.speed in 0..120)
            assertTrue(state.battery in 0..100)
            assertTrue(state.temperature in 15..110)
            if (state.speed > 0) assertFalse(state.doorOpen)
        }
    }

    @Test
    fun depletedBatteryKeepsVehicleStationary() {
        val state = engine.next(VehicleState(80, 0, 40, true))

        assertEquals(0, state.speed)
        assertEquals(0, state.battery)
    }
}
