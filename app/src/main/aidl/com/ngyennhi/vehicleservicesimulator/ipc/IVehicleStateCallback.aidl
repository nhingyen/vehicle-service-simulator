package com.ngyennhi.vehicleservicesimulator.ipc;

import com.ngyennhi.vehicleservicesimulator.ipc.VehicleSnapshot;

oneway interface IVehicleStateCallback {
    void onSnapshot(in VehicleSnapshot snapshot);
}
