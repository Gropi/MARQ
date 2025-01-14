package Network.Connection;

import Events.IMessageReceivedListener;

import java.io.IOException;
import java.net.InetAddress;

public interface IHandleMessageReceivedListener {

    void addListener(IMessageReceivedListener listener);

    void removeListener(IMessageReceivedListener listener);

    void sendMessageToListeners(byte[] message, InetAddress from) throws IOException;
}
