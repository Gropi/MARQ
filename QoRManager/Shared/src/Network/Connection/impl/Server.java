package Network.Connection.impl;

import Events.IMessageReceivedListener;
import Network.Connection.ICommunicationHelper;
import Network.Connection.IHandleMessageReceivedListener;
import Network.Connection.IServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;

public class Server implements IServer, Runnable {
    private static final Logger _Logger = LogManager.getRootLogger();
    private final int _port;
    private ServerSocket _ServerSocket;

    private boolean _stopServer = false;
    private final IHandleMessageReceivedListener _HandleListener;
    private final ICommunicationHelper _CommunicationHelper;

    public Server(int port) {
        if (port <= 0)
            throw new IllegalArgumentException("The socket is not initiated correctly. You need to set a correct port.");
        _HandleListener = new HandleIMessageReceivedListener();
        _CommunicationHelper = new CommunicationHelper();
        _port = port;
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
    public boolean isOpen() {
        return _ServerSocket != null && !_ServerSocket.isClosed();
    }

    public boolean start() throws IOException {
        _stopServer = false;
        _ServerSocket = new ServerSocket(_port);
        _ServerSocket.setReuseAddress(true);

        _Logger.debug("Server is listening on port " + _port);

        while(!_stopServer) {
            if (_ServerSocket == null)
                throw new NullPointerException("The socket is null.");

            var socket = _ServerSocket.accept();

            // Handle socket connection in other thread:
            var connectionHandler = new ConnectionHandler(socket, _Logger, _CommunicationHelper, _HandleListener);
            new Thread(connectionHandler).start();
        }
        close();
        return true;
    }

    @Override
    public boolean close() {
        _stopServer = true;
        if (_ServerSocket != null) {
            try {
                _Logger.debug("Server socket was closed. Port: " + _ServerSocket.getLocalPort());
                _ServerSocket.close();
            } catch (Exception ex) {
                _Logger.warn("The socket was already closed.");
            }
            _ServerSocket = null;
        }
        return true;
    }

    @Override
    public void run() {
        try {
            start();
        } catch (IOException e) {
            _Logger.fatal("failed to initiate the server.", e);
            throw new RuntimeException(e);
        }
    }
}
