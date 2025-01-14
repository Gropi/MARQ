package Events;

import Network.Connection.IConnectionInformation;

import java.io.IOException;

public interface IMessageReceivedListener {
   void messageReceived(byte[] message, IConnectionInformation from) throws IOException;
}
