package Monitoring.Facade.impl;

import Monitoring.Event.IMeasurementUpdate;
import Monitoring.Facade.IMonitoringFacade;
import Monitoring.MeasurementUnit.impl.NetworkMeasurement;

public class MonitoringFacade implements IMonitoringFacade {
    private final NetworkMeasurement _networkMeasurement;

    public MonitoringFacade() {
        _networkMeasurement = new NetworkMeasurement();
    }

    @Override
    public void registerMeasurementUpdateNotification(IMeasurementUpdate notifier) {
        _networkMeasurement.registerMeasurementUpdateNotification(notifier);
    }

    @Override
    public void unregisterMeasurementUpdateNotification(IMeasurementUpdate notifier) {
        _networkMeasurement.unregisterMeasurementUpdateNotification(notifier);
    }

    @Override
    public void startCollectingCPU() {

    }

    @Override
    public void startCollectingRAM() {

    }
}
