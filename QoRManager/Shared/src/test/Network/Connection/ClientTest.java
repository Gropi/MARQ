package Network.Connection;

import Network.Connection.impl.Client;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientTest {

    @Test
    public void Constructor_SocketHasNoPort_ExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> new Client(new Socket()));

        var mockedSocket = mock(Socket.class);
        var mockedInternetAddress= mock(InetAddress.class);
        when(mockedSocket.getPort()).thenReturn(0);
        when(mockedSocket.getInetAddress()).thenReturn(mockedInternetAddress);
        assertThrows(IllegalArgumentException.class, () -> new Client(mockedSocket));
    }

    @Test
    public void Constructor_SocketIsNull_ExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> {
            Socket nullSocket = null;
            new Client(nullSocket);
        });
    }

    @Test
    public void Constructor_SocketIsNotCorrectInitiated_ExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> { new Client(new Socket()); });
    }

    @Test
    public void Start_SocketIsOpen_WithSocketInConstructor(){
        var mockedSocket = mock(Socket.class);
        var mockedInternetAddress= mock(InetAddress.class);
        var mockedHostAddress = "42.42.42.42";
        when(mockedSocket.getPort()).thenReturn(2000);
        when(mockedInternetAddress.getHostAddress()).thenReturn(mockedHostAddress);
        when(mockedSocket.getInetAddress()).thenReturn(mockedInternetAddress);
        when(mockedSocket.isConnected()).thenReturn(true);

        try {
            when(mockedSocket.getInputStream()).thenReturn(mock(InputStream.class));
        } catch (IOException e) {
            throw new AssertionFailedError();
        }
        try {
            when(mockedSocket.getOutputStream()).thenReturn(mock(OutputStream.class));
        } catch (IOException e) {
            throw new AssertionFailedError();
        }

        var instanceUnderTest = new Client(mockedSocket);

        instanceUnderTest.start();
        assertTrue(instanceUnderTest.isOpen());
    }

    @Test
    public void Test_SendMessage() throws IOException {
        var mockedSocket = mock(Socket.class);
        var mockedInternetAddress= mock(InetAddress.class);
        var mockedHostAddress = "69.69.69.69";
        when(mockedSocket.getPort()).thenReturn(2000);
        when(mockedSocket.getInetAddress()).thenReturn(mockedInternetAddress);
        when(mockedInternetAddress.getHostAddress()).thenReturn(mockedHostAddress);
        when(mockedSocket.isConnected()).thenReturn(true);

        when(mockedSocket.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockedSocket.getOutputStream()).thenReturn(mock(OutputStream.class));

        var messageToSend = "Message to send test";
        var instanceUnderTest = new Client(mockedSocket);
        instanceUnderTest.start();


        assertTrue(instanceUnderTest.sendMessage(messageToSend));
    }
}
