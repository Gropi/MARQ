package Network.Connection.impl;

import Events.IMessageReceivedListener;
import Network.Connection.IClient;
import Network.Connection.IConnectionInformation;
import com.google.protobuf.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements IClient {
    public static final Logger _Logger = LogManager.getRootLogger();
    private int _Port;
    private String _IPAddressOfServer;
    private Socket _Socket;
    private HandleIMessageReceivedListener _HandleListener;
    private CommunicationHelper _CommunicationHelper;

    public Client(IConnectionInformation connectionInformation) {
        initiateClass(connectionInformation.getManagementPort(), connectionInformation.getIPAddress());
    }

    public Client(Socket socket) {
        if (socket == null)
            throw new NullPointerException("You need to give a socket.");
        if (socket.getInetAddress() == null)
            throw new IllegalArgumentException("The socket is not initiated correctly. You need to give an IP address.");

        _Socket = socket;
        initiateClass(socket.getPort(), socket.getInetAddress().getHostAddress());
    }

    private void initiateClass(int port, String address) {
        if (port <= 0)
            throw new IllegalArgumentException("The socket is not initiated correctly. You need to set a correct port.");
        if (address == null || address.equals(""))
            throw new IllegalArgumentException("The socket is not initiated correctly. You need to give an IP address.");
        _HandleListener = new HandleIMessageReceivedListener();
        _CommunicationHelper = new CommunicationHelper();
        _Port = port;
        _IPAddressOfServer = address;
    }

    @Override
    public boolean start() {
        if (!isOpen()) {
            if(_Socket == null) {
                _Socket = new Socket();
            } else if (_Socket.isConnected()) {
                return true;
            }
            try {
                _Socket.setReuseAddress(true);
                _Socket.connect(new InetSocketAddress(_IPAddressOfServer, _Port));

            } catch (IOException e) {
                _Logger.fatal("Failed to initiate socket to the server. The given IP-Address is: " + _IPAddressOfServer
                        + ". The port is: " + _Port + ".", e);
                return false;
            }
        }
        _Logger.trace("Client connected successfully.");
        return true;
    }

    @Override
    public boolean isOpen() {
        return _Socket != null && _Socket.isConnected();
    }

    @Override
    public boolean close() {
        if (_Socket != null) {
            if (_Socket.isConnected()) {
                try {
                    _Socket.close();
                    _Logger.trace("Communication closed successfully.");
                } catch (IOException e) {
                    _Logger.fatal("Failed to close socket to the server.", e);
                    return false;
                }
            }
            _Socket = null;
        }
        return true;
    }

    @Override
    public void addMessageReceivedListener(IMessageReceivedListener listener) {
        _HandleListener.addListener(listener);
    }

    @Override
    public void removeMessageReceivedListener(IMessageReceivedListener listener) {
        _HandleListener.removeListener(listener);
    }

    @Override
    public boolean sendMessage(String message) throws NullPointerException {
        if (message == null)
            throw new NullPointerException("You cannot send a null message.");
        if (isOpen()) {
            try {
                _CommunicationHelper.sendMessageToStream(message, _Socket);
                _Logger.debug("Message send to:" + _IPAddressOfServer + " and port: " + _Port);
                _Logger.trace("Content of message: " + message);
                return true;
            } catch (IOException e) {
                _Logger.fatal("Failed to get output stream from socket.", e);
            }
        }
        return false;
    }

    @Override
    public <T extends Message> boolean sendMessage(T message) throws IOException, NullPointerException {
        if (message == null)
            throw new NullPointerException("You cannot send a null message.");
        if (isOpen()) {
            try {
                _Logger.trace("Performing client sendMessage with message: "+ message);
                _CommunicationHelper.sendMessageToStream(message, _Socket);
                _Logger.trace("Message send.");
                return true;
            } catch (IOException e) {
                _Logger.fatal("Failed to get output stream from socket.", e);
            }
        }
        return false;
    }

    @Override
    public String sendMessageWithResponse(String messageToSend) throws IOException, NullPointerException {
        if (messageToSend == null)
            throw new NullPointerException("You cannot send a null message.");
        if (isOpen()) {
            sendMessageWithResponse(messageToSend);

            _Logger.trace("Sent message. Currently waiting for response...");

            var answer = _CommunicationHelper.receiveMessageAsString(_Socket);
            _Logger.trace("Message: " + answer);

            return answer;
        }
        return "";
    }

    @Override
    public void run() {
        // TODO: Needs to be implemented, if needed.
    }
}
