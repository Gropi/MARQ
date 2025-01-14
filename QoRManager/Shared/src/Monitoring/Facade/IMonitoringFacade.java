package Monitoring.Facade;

import Monitoring.Event.IMeasurementUpdate;

import java.io.IOException;

public interface IMonitoringFacade {
    void registerMeasurementUpdateNotification(IMeasurementUpdate notifier);

    void unregisterMeasurementUpdateNotification(IMeasurementUpdate notifier);

    void startCollectingCPU() throws IOException;

    void startCollectingRAM() throws IOException;
}
