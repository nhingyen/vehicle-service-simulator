package com.ngyennhi.vehicleservicesimulator.simulation

import com.ngyennhi.vehicleservicesimulator.model.VehicleState
import kotlin.random.Random

class VehicleSimulationEngine(private val random: Random = Random.Default) {
    fun next(current: VehicleState): VehicleState {
        val battery = current.battery.coerceIn(0, 100)
        val speed = if (battery == 0) {
            0
        } else {
            (current.speed.coerceIn(0, 120) + random.nextInt(-4, 7)).coerceIn(0, 120)
        }
        val nextBattery = (battery - if (speed > 0 && random.nextInt(10) == 0) 1 else 0)
            .coerceIn(0, 100)
        val nextSpeed = if (nextBattery == 0) 0 else speed
        val temperature = (current.temperature.coerceIn(15, 110) + random.nextInt(-1, 2))
            .coerceIn(15, 110)
        val doorOpen = when {
            nextSpeed > 0 -> false
            random.nextInt(20) == 0 -> !current.doorOpen
            else -> current.doorOpen
        }

        return VehicleState(nextSpeed, nextBattery, temperature, doorOpen)
    }
}
