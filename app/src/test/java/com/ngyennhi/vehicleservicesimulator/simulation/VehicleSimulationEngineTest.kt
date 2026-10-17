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

    @Test
    fun eachStepChangesValuesWithinExpectedRange() {
        val current = VehicleState(60, 100, 40, true)

        repeat(1_000) {
            val next = engine.next(current)
            assertTrue(next.speed - current.speed in -4..6)
            assertTrue(next.battery in 99..100)
            assertTrue(next.temperature - current.temperature in -1..1)
            assertFalse(next.doorOpen)
        }
    }

    @Test
    fun consumingLastBatteryStopsVehicleImmediately() {
        val next = VehicleSimulationEngine(ZeroRandom).next(
            VehicleState(speed = 60, battery = 1, temperature = 40, doorOpen = false)
        )

        assertEquals(0, next.speed)
        assertEquals(0, next.battery)
    }

    @Test
    fun doorCanToggleOnlyWhenStationary() {
        val stationary = VehicleState(speed = 0, battery = 0, temperature = 40, doorOpen = false)
        val next = VehicleSimulationEngine(ZeroRandom).next(stationary)

        assertTrue(next.doorOpen)
        assertFalse(VehicleSimulationEngine(ZeroRandom).next(
            VehicleState(speed = 60, battery = 100, temperature = 40, doorOpen = true)
        ).doorOpen)
    }

    private object ZeroRandom : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }
}
