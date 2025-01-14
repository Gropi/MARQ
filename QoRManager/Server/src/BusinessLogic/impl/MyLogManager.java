package BusinessLogic.impl;

import Monitoring.Event.Logging.ILogInformation;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.nio.file.Path;

public class MyLogManager {
    private static final Logger _Logger = LogManager.getLogger(MyLogManager.class);
    private ILogInformation _LogInformation;

    public MyLogManager(ILogInformation logInformation){
        _LogInformation = logInformation;
    }

    public void start(){
        configureLogger();
        var context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        //System.setProperty(property.basePath, _LogInformation.getLogAddress());
//        System.setProperty("property.basePath",
//                _LogInformation.getLogAddress());
//        context.reconfigure();
//        System.setProperty("rootLogger.level",
//                _LogInformation.getLogLevel());
//        context.reconfigure();
        Configurator.setRootLevel(Level.toLevel(_LogInformation.getLogLevel()));
        _Logger.trace("LogAddress is " + _LogInformation.getLogAddress());
        _Logger.trace("LogLevel is " + _LogInformation.getLogLevel());
    }
    private static void configureLogger() {
        var cwd = Path.of("").toAbsolutePath().toString();
        var context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        var subPath = createCompletePath(new String[] {"Server", "src", "resources", "log4j2.properties"});
        var file = new File(cwd + subPath);

        if (file.exists())
            context.setConfigLocation(file.toURI());
    }

    private static String createCompletePath(String[] subPath) {
        var returnValue = "";
        for (var sub : subPath) {
            returnValue += File.separator;
            returnValue += sub;
        }
        return returnValue;
    }

}
