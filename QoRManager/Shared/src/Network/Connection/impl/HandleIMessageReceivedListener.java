package Network.Connection.impl;

import Events.IMessageReceivedListener;
import Network.Connection.IHandleMessageReceivedListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentLinkedDeque;

public class HandleIMessageReceivedListener implements IHandleMessageReceivedListener {
    private final ConcurrentLinkedDeque<IMessageReceivedListener> _Listeners = new ConcurrentLinkedDeque<>();

    public void addListener(IMessageReceivedListener listener) {
        if (!_Listeners.contains(listener) && listener != null)
            _Listeners.add(listener);
    }

    public void removeListener(IMessageReceivedListener listener) {
        _Listeners.remove(listener);
    }

    public void sendMessageToListeners(byte[] message, InetAddress from) throws IOException {
        var connectionInformation = new ConnectionInformation(from.getHostAddress());
        for(var it = _Listeners.iterator(); it.hasNext(); ) {
            var receiver = it.next();
            if (receiver != null) {
                receiver.messageReceived(message, connectionInformation);
            }
        }
    }
}
