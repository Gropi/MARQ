package Network.Facade.impl;

import Monitoring.Facade.IMonitoringFacade;
import Network.Connection.IClient;
import Network.Connection.ICommunication;
import Network.Connection.IConnectionInformation;
import Network.Connection.IServer;
import Network.Connection.impl.Client;
import Network.Connection.impl.ConnectionInformation;
import Network.Connection.impl.Server;
import Network.Facade.IConnectionFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionFacade implements IConnectionFacade {
    private static final Logger _Logger = LogManager.getRootLogger();
    private final Map<Integer, IServer> _CurrentlyRunningServer;
    private Map<String, IClient> _OpenClients;

    public ConnectionFacade() {
        _CurrentlyRunningServer = new HashMap();
        _OpenClients = new ConcurrentHashMap<>();
    }

    @Override
    public IServer startServer(int port) {
        IServer communication;
        if (_CurrentlyRunningServer.containsKey(port)) {
            communication = _CurrentlyRunningServer.get(port);
            _Logger.debug("Took server from already running for port: " + port);
        } else {
            communication = new Server(port);
            _CurrentlyRunningServer.put(port, communication);
            _Logger.debug("Started server on port: " + port + " successfully.");
        }
        return communication;
    }

    @Override
    public IClient startClient(String serverIPAddress, int port) {
        var connectionInformation = new ConnectionInformation(serverIPAddress, port);
        return startClient(connectionInformation);
    }

    @Override
    public IClient startClient(IConnectionInformation connectionInformation) {
        var identifier = connectionInformation.getIPAddress() + connectionInformation.getManagementPort();
        if (_OpenClients.containsKey(identifier)) {
            var connection = _OpenClients.get(identifier);
            if (connection.isOpen())
                return connection;
            else
                _OpenClients.remove(connectionInformation.getIPAddress());
        }
        var connection = new Client(connectionInformation);
        _OpenClients.put(identifier, connection);
        return connection;
    }
}
