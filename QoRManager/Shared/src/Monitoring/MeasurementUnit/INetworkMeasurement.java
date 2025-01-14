package Monitoring.MeasurementUnit;

import Monitoring.Event.IMeasurementUpdate;

public interface INetworkMeasurement {
    void registerMeasurementUpdateNotification(IMeasurementUpdate notifier);

    void unregisterMeasurementUpdateNotification(IMeasurementUpdate notifier);
}
