package Network.Facade;

import Network.Connection.IClient;
import Network.Connection.IConnectionInformation;
import Network.Connection.IServer;

public interface IConnectionFacade {

    IServer startServer(int port);

    IClient startClient(String serverIPAddress, int port);

    IClient startClient(IConnectionInformation connectionInformation);
}
