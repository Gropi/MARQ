package Network.Connection;

import java.io.IOException;

public interface ICommunicationPartner {
    void sendMessageTo(String message) throws IOException;
}
