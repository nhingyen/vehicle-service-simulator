package com.ngyennhi.vehicleservicesimulator.ipc;

import com.ngyennhi.vehicleservicesimulator.ipc.IVehicleStateCallback;

interface IVehicleDataService {
    void registerCallback(IVehicleStateCallback callback);
    void unregisterCallback(IVehicleStateCallback callback);
}
