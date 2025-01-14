package IO.impl;

import IO.IManagingMeasurement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ManagingMeasurement implements IManagingMeasurement {
    private final Logger m_Logger;
    private final String m_Separator;

    public ManagingMeasurement() {
        this("measurementLog");
    }

    public ManagingMeasurement(String loggerName) {
        this(loggerName, ";");
    }

    public ManagingMeasurement(String loggerName, String separator) {
        m_Logger = LogManager.getLogger(loggerName);
        m_Separator = separator;
    }

    public void writeLine(Object... objectToWrite) {
        var message = "";
        for (var part : objectToWrite) {
            message += part.toString() + m_Separator;
        }
        m_Logger.info(message);
    }
}
