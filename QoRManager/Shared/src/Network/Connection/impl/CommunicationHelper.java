package Network.Connection.impl;

import Network.Connection.ICommunicationHelper;
import com.google.protobuf.Message;

import java.io.*;
import java.net.Socket;

public class CommunicationHelper implements ICommunicationHelper {
    public byte[] receiveMessageAsByteArray(Socket socket) throws IOException {
        var inputStream = socket.getInputStream();
        return inputStream.readAllBytes();
    }

    public String receiveMessageAsString(Socket socket) throws IOException {
        var inputStream = socket.getInputStream();
        var reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        String completeLine = "";
        while((line = reader.readLine()) != null)
        {
            completeLine += line;
        }
        return completeLine;
    }

    public void sendMessageToStream(String message, Socket socket) throws IOException {
        var outputStream = socket.getOutputStream();
        var writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(message);
        writer.flush();
        writer.close();
    }

    public <T extends Message> void sendMessageToStream(T message, Socket socket) throws IOException {
        var outputStream = socket.getOutputStream();
        message.writeTo(outputStream);
    }
}
