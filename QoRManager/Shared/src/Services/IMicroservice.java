package Services;

import com.google.protobuf.Any;
import com.google.protobuf.Message;

import java.io.IOException;
import java.util.List;

public interface IMicroservice {
    String HANDLER_PORT_IDENTIFIER = "handler";

    ServiceState getState();

    String ID();

    int getHandlerPort();

    int getPort(String purpose);

    String getAddress();

    void allocate();
    void release();

    void loadExecution(String execution);

    String getExecution();

    <T extends Message> void sendMessage(T message, String portIdentifier) throws IOException, NullPointerException;

    void sendMessage(String message, String portIdentifier) throws IllegalArgumentException, IOException;

    void sendMessages(List<Any> messages, String portIdentifier) throws IOException;
}
