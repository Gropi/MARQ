package Network.Connection;

import Network.Connection.impl.ConnectionInformation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionInformationTest {
    private final String _IPAddress = "928.291.123.123";
    private final String _DefaultIPAddress = "127.0.0.1";
    private final Integer _Port = 2000;

    @Test
    public void DefaultConstructorTest() {
        var instanceUnderTest = new ConnectionInformation();
        checkForDefault(instanceUnderTest);
    }

    @Test
    public void ConstructorTestWithParameter() {
        var parameters = new String[] { "-c", "-p", _Port.toString(), "-i", _IPAddress };
        var instanceUnderTest = new ConnectionInformation(parameters);
        assertEquals(_Port, instanceUnderTest.getManagementPort());
        assertEquals(_IPAddress, instanceUnderTest.getIPAddress());
        assertTrue(instanceUnderTest.isClient());
    }

    @Test
    public void parseFromParameters_OK() {
        var parameters = new String[] { "-c", "-p", _Port.toString(), "-i", _IPAddress };
        var instanceUnderTest = new ConnectionInformation();
        instanceUnderTest.parseFromParameters(parameters);
        assertEquals(_Port, instanceUnderTest.getManagementPort());
        assertEquals(_IPAddress, instanceUnderTest.getIPAddress());
        assertTrue(instanceUnderTest.isClient());
    }

    @Test
    public void parseFromParameters_WithUpperLetters() {
        var parameters = new String[] { "-C", "-P", _Port.toString(), "-I", _IPAddress };
        var instanceUnderTest = new ConnectionInformation();
        instanceUnderTest.parseFromParameters(parameters);
        assertEquals(_Port, instanceUnderTest.getManagementPort());
        assertEquals(_IPAddress, instanceUnderTest.getIPAddress());
        assertTrue(instanceUnderTest.isClient());
    }

    @Test
    public void parseFromParameters_Server() {
        var parameters = new String[] { "-s", "-p", _Port.toString() };
        var instanceUnderTest = new ConnectionInformation();
        instanceUnderTest.parseFromParameters(parameters);
        assertEquals(_Port, instanceUnderTest.getManagementPort());
        assertEquals(_DefaultIPAddress, instanceUnderTest.getIPAddress());
        assertFalse(instanceUnderTest.isClient());
    }

    @Test
    public void testParseConstructorWithMissingIP() {
        var parameters = new String[] { "-s", "-p", _Port.toString() };
        var instanceUnderTest = new ConnectionInformation(parameters);
        assertEquals(_Port, instanceUnderTest.getManagementPort());
        assertEquals(_DefaultIPAddress, instanceUnderTest.getIPAddress());
        assertFalse(instanceUnderTest.isClient());
    }

    @Test
    public void parseFromParameters_Null() {
        var instanceUnderTest = new ConnectionInformation();
        instanceUnderTest.parseFromParameters(null);
        checkForDefault(instanceUnderTest);
    }

    private void checkForDefault(ConnectionInformation instanceUnderTest) {
        assertEquals(2000, instanceUnderTest.getManagementPort());
        assertEquals(_DefaultIPAddress, instanceUnderTest.getIPAddress());
        assertFalse(instanceUnderTest.isClient());
    }
}
