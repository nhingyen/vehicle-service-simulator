package com.ngyennhi.vehicleservicesimulator.model

data class VehicleState(
    val speed: Int,
    val battery: Int,
    val temperature: Int,
    val doorOpen: Boolean
)
