package com.ngyennhi.vehicleservicesimulator.ipc

import android.os.Parcel
import android.os.Parcelable
import com.ngyennhi.vehicleservicesimulator.model.VehicleState

data class VehicleSnapshot(
    val speed: Int,
    val battery: Int,
    val temperature: Int,
    val doorOpen: Boolean,
    val isRunning: Boolean
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        speed = parcel.readInt(),
        battery = parcel.readInt(),
        temperature = parcel.readInt(),
        doorOpen = parcel.readInt() != 0,
        isRunning = parcel.readInt() != 0
    )

    fun toVehicleState() = VehicleState(speed, battery, temperature, doorOpen)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(speed)
        parcel.writeInt(battery)
        parcel.writeInt(temperature)
        parcel.writeInt(if (doorOpen) 1 else 0)
        parcel.writeInt(if (isRunning) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VehicleSnapshot> {
        fun from(state: VehicleState, isRunning: Boolean) = VehicleSnapshot(
            state.speed, state.battery, state.temperature, state.doorOpen, isRunning
        )

        override fun createFromParcel(parcel: Parcel) = VehicleSnapshot(parcel)

        override fun newArray(size: Int): Array<VehicleSnapshot?> = arrayOfNulls(size)
    }
}
