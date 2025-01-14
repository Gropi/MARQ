package Monitoring.Event;

import Monitoring.Enums.MeasurableValues;

import java.io.IOException;
import java.util.UUID;

public interface IMeasurementUpdate {
    void MeasurementUpdated(MeasurableValues valueUpdated, int newValue, UUID identifier) throws IOException;
}
