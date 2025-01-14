package AvailableResources.impl;

import AvailableResources.IServiceHandler;
import Network.Connection.IConnectionInformation;
import Network.DataModel.CommunicationMessages;
import Network.Facade.IConnectionFacade;
import Services.IMicroservice;
import Services.impl.Microservice;
import Structures.Graph.interfaces.IVertex;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MicroserviceHandler implements IServiceHandler {
    private final Map<String, List<IMicroservice>> _Server;
    private final Logger _Logger;
    private final IConnectionFacade _ConnectionFacade;

    public MicroserviceHandler(Logger logger, IConnectionFacade connectionFacade) {
        _Server = new ConcurrentHashMap<>();
        _ConnectionFacade = connectionFacade;
        _Logger = logger;
    }

    @Override
    public IMicroservice bindService(IVertex vertex){
        var service = findOrAssignMicroserviceToTask(vertex);
        service.allocate();
        vertex.bindMicroservice(service);
        return service;
    }

    @Override
    public IMicroservice getServiceByVertex(IVertex vertex) {
        for(var microservices : _Server.values()) {
            var microservice = microservices.stream().filter(service -> service.getExecution().equals(vertex.getServiceName())).findAny().orElse(null);
            if (microservice != null)
                return microservice;
        }
        return null;
    }

    @Override
    public IMicroservice peekServiceName(String serviceName) {
        _Logger.debug("HAD TO PEEK SERVICE NAME!");
        var serverList = _Server.values();
        IMicroservice microservice = null;

        for(var services : serverList) {
            microservice = services.stream().filter(candidate -> candidate.getState().getValue() == 0)
                    .filter(candidate -> (candidate.getExecution() != null && candidate.getExecution().equals(serviceName))).findAny().orElse(null);

            if(microservice != null)
                break;
        }

        return microservice;
    }

    @Override
    public int availableServices() {
        var amount = 0;
        for (var key : _Server.keySet()) {
            amount += _Server.get(key).size();
        }
        return amount;
    }

    @Override
    public void unbindMicroservice(IMicroservice microservice){
        microservice.release();
    }

    @Override
    public void releaseMicroservice(String id) {
        var serverList = _Server.values();

        for(var microservices: serverList) {
            var microservice = microservices.stream().filter(service -> service.ID().equals(id)).findAny().orElse(null);

            if(microservice != null){
                _Server.get(microservice.getAddress()).remove(microservice);
            }
        }
    }

    @Override
    public void addMicroservice(String id, IConnectionInformation connectionInformation) {
        var ip = connectionInformation.getIPAddress();

        var ports = new ArrayList<CommunicationMessages.PurposePort>();
        var port = CommunicationMessages.PurposePort.newBuilder()
                .setPort(connectionInformation.getManagementPort())
                .build();
        ports.add(port);

        var microservice = new Microservice(id, connectionInformation.getIPAddress(), ports, 2001, _ConnectionFacade, _Logger);

        var list = _Server.putIfAbsent(ip, Collections.synchronizedList(new ArrayList<>()));
        if (list == null) {
            list = _Server.putIfAbsent(ip, Collections.synchronizedList(new ArrayList<>()));
        }
        list.add(microservice);
        _Logger.info("Registered service with id: " + id);
    }

    @Override
    public void addMicroservice(String ip, CommunicationMessages.ServiceRegistrationMessage serviceRegistrationMessage) {
        var outputAddition = ""; //FOR LOGGING ONLY
        var id = serviceRegistrationMessage.getID();
        var ports = serviceRegistrationMessage.getPurposePortList();

        Microservice microservice;

        if(serviceRegistrationMessage.hasExecutionName()) {
            var executionName = serviceRegistrationMessage.getExecutionName();
            outputAddition = ": \"" + executionName + "\"";
            microservice = new Microservice(id, ip, ports, executionName, serviceRegistrationMessage.getHandlerPort(), _ConnectionFacade, _Logger);
        }
        else {
            microservice = new Microservice(id, ip, ports, serviceRegistrationMessage.getHandlerPort(), _ConnectionFacade, _Logger);
        }

        var list = _Server.putIfAbsent(ip, Collections.synchronizedList(new ArrayList<>()));
        if (list == null) {
            list = _Server.putIfAbsent(ip, Collections.synchronizedList(new ArrayList<>()));
        }
        list.add(microservice);
        _Logger.info("Registered service " + id + outputAddition +".");
    }

    private IMicroservice findOrAssignMicroserviceToTask(IVertex vertex) {
        var serverList = _Server.values();
        IMicroservice microservice = null;

        //Look at each server and search for a microservice that already holds the preferred execution in memory
        for(var services : serverList) {
            microservice = services.stream().filter(candidate -> candidate.getState().getValue() == 0)
                    .filter(candidate -> (candidate.getExecution() != null && candidate.getExecution().equals(vertex.getServiceName()))).findAny().orElse(null);

            if(microservice != null)
                break;
        }

        //If there was no matching service search for any available
        if(microservice == null){

            for(var services : serverList) {
                microservice = services.stream().filter(candidate -> candidate.getState().getValue() == 0)
                        .findAny().orElse(null);

                if(microservice != null)
                    break;
            }

            //If there is an available service load the execution
            if(microservice != null)
                microservice.loadExecution(vertex.getServiceName());

            //Else request a new service
            else {
                throw new RuntimeException("No service available to bind to vertex.");
                //TODO: HANDLE NO AVAILABLE SERVICES (PROBABLY INITIATE NEW ONE)
            }
        }
        _Logger.debug("Binding service to vertex: \n Service: " + microservice.ID() + "\n Vertex: " + vertex.getLabel());
        return microservice;
    }
}
