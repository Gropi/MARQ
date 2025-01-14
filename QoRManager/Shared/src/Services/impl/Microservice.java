package Services.impl;

import Network.Connection.IClient;
import Network.DataModel.CommunicationMessages;
import Network.Facade.IConnectionFacade;
import Services.IMicroservice;
import Services.ServiceState;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Microservice implements IMicroservice {
    private String _ExecutionInformation;
    private ServiceState _State;
    private final String _ID;
    private final String _IP;
    private final Map<String, Integer> _Ports;
    private final IConnectionFacade _ConnectionFacade;
    private final Logger _Logger;
    private final Map<String, IClient> _ClientConnections;

    public Microservice(String id, String ip, List<CommunicationMessages.PurposePort> purposePorts, int handlerPort,
                        IConnectionFacade connectionFacade, Logger logger) {
        _ClientConnections = new HashMap<>();

        _Logger = logger;
        _ConnectionFacade = connectionFacade;
        _ID = id;
        _IP = ip;
        _State = ServiceState.FREE;

        var ports = new HashMap<String, Integer>();
        for(var purposePort : purposePorts){
            ports.put(purposePort.getPurpose(), purposePort.getPort());
        }
        _Ports = ports;
        _Ports.put(HANDLER_PORT_IDENTIFIER, handlerPort);
    }

    public Microservice(String id, String ip, List<CommunicationMessages.PurposePort> purposePorts,
                        String executionInformation, int handlerPort, IConnectionFacade connectionFacade, Logger logger){
        this(id, ip, purposePorts, handlerPort, connectionFacade, logger);
        _ExecutionInformation = executionInformation;
    }

    public ServiceState getState() {
        return _State;
    }

    public String ID() {
        return _ID;
    }

    public int getHandlerPort(){
        return _Ports.get(HANDLER_PORT_IDENTIFIER);
    }

    public int getPort(String purpose) {
        if(!_Ports.containsKey(purpose))
            return -1;

        return _Ports.get(purpose);
    }

    public String getAddress(){
        return _IP;
    }

    public void allocate() {
        _State = ServiceState.PENDING;
    }
    public void release() {
        _State = ServiceState.FREE;
    }

    public void loadExecution(String execution) {
        _ExecutionInformation = execution;
    }

    public String getExecution() {
        return _ExecutionInformation;
    }

    public <T extends Message> void sendMessage(T message, String portIdentifier) throws IOException, NullPointerException {
        IClient client = null;
        var port = getPort(portIdentifier);

        try {
            client = getClient(portIdentifier, port);
            client.sendMessage(message);
        } catch (IOException e) {
            _Logger.fatal("Failed to send message to: " + _IP + ":" + port);
            throw e;
        }
        finally {
            if (client != null)
                client.close();
        }
    }

    public void sendMessage(String message, String portIdentifier) throws IllegalArgumentException, IOException {
        IClient client = null;
        var port = getPort(portIdentifier);

        try {
            client = getClient(portIdentifier, port);
            client.sendMessage(message);
        } catch (IOException e) {
            _Logger.fatal("Failed to send message to: " + _IP + ":" + port);
            throw e;
        }
        finally {
            if (client != null)
                client.close();
        }
    }

    private IClient getClient(String portIdentifier, int port) throws IOException {
        IClient client;
        if (!_ClientConnections.containsKey(portIdentifier)) {
            client = _ConnectionFacade.startClient(_IP, port);
            _ClientConnections.put(portIdentifier, client);
        } else {
            client = _ClientConnections.get(portIdentifier);
        }

        if (!client.start()) {
            client = _ConnectionFacade.startClient(_IP, port);
            _ClientConnections.put(portIdentifier, client);
            client.start();
        }

        return client;
    }

    public void sendMessages(List<Any> messages, String portIdentifier) throws IOException {
        IClient client = null;
        var port = getPort(portIdentifier);

        try {
            client = getClient(portIdentifier, port);
            for (var message : messages) {
                client.sendMessage(message);
            }
        } catch (IOException e) {
            _Logger.fatal("Failed to send message to: " + _IP + ":" + port);
            throw e;
        }
        finally {
            if (client != null)
                client.close();
        }
    }
}