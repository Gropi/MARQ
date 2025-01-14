package Network.Connection;

import Events.IMessageReceivedListener;

import java.io.IOException;

public interface ICommunication extends Runnable {

    /**
     * This function allows you to get informed when ever the server gets a message from a remote computer.
     * @param listener The listener that needs to be informed.
     */
    void addMessageReceivedListener(IMessageReceivedListener listener);

    /**
     * Removes a listener from the list of all listeners that get informed when a message is received by the server.
     * @param listener The listener that does not want to be informed.
     */
    void removeMessageReceivedListener(IMessageReceivedListener listener);

    /**
     * Allows you to check whether the communication channel is already or still open.
     * @return True if open, otherwise false.
     */
    boolean isOpen();

    /**
     * Allows you to start the communication. If this is not called beforehand, send message will not work.
     * @throws IOException If the communication can not be established, because of used port or something like that.
     */
    boolean start() throws IOException;

    /**
     * This method closes the connection. If the connection is already closed, there is no failure. However, if there
     * is something wrong with the close process, you get an IOException.
     * @return true if closed, otherwise false.
     * @throws IOException Exception thrown by IO if something went wrong.
     */
    boolean close() throws IOException;
}
