package Network.Connection;

public interface IConnectionInformation {
    /**
     * True if the services should start as a client, otherwise false.
     * @return True if the services should start as a client, otherwise false.
     */
    boolean isClient();

    String getIPAddress();

    int getManagementPort();
}
