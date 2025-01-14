package Network.Connection;

import com.google.protobuf.Message;

import java.io.IOException;

public interface IClient extends ICommunication {
    boolean sendMessage(String message) throws IOException, NullPointerException;

    <T extends Message> boolean sendMessage(T message) throws IOException, NullPointerException;

    String sendMessageWithResponse(String message) throws IOException, NullPointerException;
}
