package com.ngyennhi.vehicleservicesimulator.ipc

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ngyennhi.vehicleservicesimulator.model.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehicleSnapshotTest {
    @Test
    fun parcelRoundTripPreservesVehicleStateAndRunningStatus() {
        val state = VehicleState(speed = 42, battery = 80, temperature = 30, doorOpen = true)
        val original = VehicleSnapshot.from(state, isRunning = true)
        val parcel = Parcel.obtain()

        try {
            original.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            val restored = VehicleSnapshot.CREATOR.createFromParcel(parcel)

            assertEquals(original, restored)
            assertEquals(state, restored.toVehicleState())
        } finally {
            parcel.recycle()
        }
    }
}
