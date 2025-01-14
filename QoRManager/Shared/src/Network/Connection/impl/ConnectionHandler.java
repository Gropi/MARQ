package Network.Connection.impl;

import Network.Connection.ICommunicationHelper;
import Network.Connection.IHandleMessageReceivedListener;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    private final Logger m_Logger;
    private final ICommunicationHelper m_CommunicationHelper;
    private final Socket m_Socket;
    private final IHandleMessageReceivedListener m_Listeners;

    public ConnectionHandler(Socket socket, Logger logger, ICommunicationHelper communicationHelper,
                             IHandleMessageReceivedListener listeners) {
        m_Socket = socket;
        m_Logger = logger;
        m_CommunicationHelper = communicationHelper;
        m_Listeners = listeners;
    }

    @Override
    public void run() {
        try {
            m_Logger.debug("Client is connected. IP:" + m_Socket.getInetAddress() + " Port: " + m_Socket.getPort());
            var message = m_CommunicationHelper.receiveMessageAsByteArray(m_Socket);
            m_Listeners.sendMessageToListeners(message, m_Socket.getInetAddress());
        } catch (IOException e) {
            m_Logger.fatal("Exception: ", e);
        }
    }
}
