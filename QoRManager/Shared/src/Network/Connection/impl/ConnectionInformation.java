package Network.Connection.impl;

import Network.Connection.IConnectionInformation;

public class ConnectionInformation implements IConnectionInformation {
    private boolean _IsClient;
    private String _IPAddress;
    private int _CommonManagementPort;

    public ConnectionInformation() {
        this("127.0.0.1", 2000);
    }

    public ConnectionInformation(String ipAddress){
        this(ipAddress, 2000);
    }

    public ConnectionInformation(String ipAddress, int port){
        _IPAddress = ipAddress;
        _CommonManagementPort = port;
        _IsClient = false;
    }

    public ConnectionInformation(String[] parameters) {
        this();
        parseFromParameters(parameters);
    }

    public void parseFromParameters(String[] parameters) {
        if (parameters == null) {
            return;
        }
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equalsIgnoreCase("-c"))
                _IsClient = true;
            else if (parameters[i].equalsIgnoreCase("-s"))
                _IsClient = false;
            else
                parsePortAndIp(i, parameters);
        }
    }

    private void parsePortAndIp(int index, String[] parameters) {
        if (parameters.length >= index + 1) {
            if (parameters[index].equalsIgnoreCase("-p"))
                _CommonManagementPort = Integer.parseInt(parameters[index + 1]);
            else if (parameters[index].equalsIgnoreCase("-i"))
                _IPAddress = parameters[index + 1];
        }
    }

    @Override
    public boolean isClient() {
        return _IsClient;
    }

    @Override
    public String getIPAddress() {
        return _IPAddress;
    }

    @Override
    public int getManagementPort() {
        return _CommonManagementPort;
    }
}
