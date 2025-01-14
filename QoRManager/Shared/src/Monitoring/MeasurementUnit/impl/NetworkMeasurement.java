package Monitoring.MeasurementUnit.impl;

import Monitoring.Event.IMeasurementUpdate;
import Monitoring.Facade.IMonitoringFacade;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkMeasurement implements IMonitoringFacade, Runnable {
    private static final Logger _Logger = LogManager.getLogger(NetworkMeasurement.class);
    private final List<IMeasurementUpdate> _registeredMeasurementUpdateConsumer;

    public NetworkMeasurement() {
        _registeredMeasurementUpdateConsumer = new CopyOnWriteArrayList<>();
        _Logger.trace("Initialize network logger ");
    }

    @Override
    public void run() {

    }

    @Override
    public void registerMeasurementUpdateNotification(IMeasurementUpdate notifier) {
        if (!_registeredMeasurementUpdateConsumer.contains(notifier))
            _registeredMeasurementUpdateConsumer.add(notifier);
    }

    @Override
    public void unregisterMeasurementUpdateNotification(IMeasurementUpdate notifier) {
        _registeredMeasurementUpdateConsumer.remove(notifier);
    }

    @Override
    public void startCollectingCPU() {

    }

    @Override
    public void startCollectingRAM() {

    }
}
