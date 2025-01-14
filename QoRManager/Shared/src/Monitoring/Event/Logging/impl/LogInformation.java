package Monitoring.Event.Logging.impl;
import Monitoring.Event.Logging.ILogInformation;

public class LogInformation implements ILogInformation{
    private String _LogAddress;
    private String _LogLevel;

    public LogInformation() {
        _LogAddress = "log";
        _LogLevel = "debug";
    }

    public LogInformation(String[] parameters) {
        this();
        parseFromParameters(parameters);
    }

    public void parseFromParameters(String[] parameters) {
        if (parameters == null) {
            return;
        }
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equalsIgnoreCase("-l")){
                _LogAddress = parameters[i+1];
            }
            else if (parameters[i].equalsIgnoreCase("-loglevel")){
                _LogLevel = parameters[i+1];
            }
        }
    }


    public String getLogAddress(){return _LogAddress;}

    public String getLogLevel(){return _LogLevel;}
}
