package Network.Connection;

import com.google.protobuf.Message;

import java.io.IOException;
import java.net.Socket;

public interface ICommunicationHelper {
    byte[] receiveMessageAsByteArray(Socket socket) throws IOException;

    String receiveMessageAsString(Socket socket) throws IOException;

    void sendMessageToStream(String message, Socket socket) throws IOException;

    <T extends Message> void sendMessageToStream(T message, Socket socket) throws IOException;
}
