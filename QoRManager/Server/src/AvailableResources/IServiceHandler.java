package AvailableResources;

import Network.Connection.IConnectionInformation;
import Network.DataModel.CommunicationMessages;
import Services.IMicroservice;
import Structures.Graph.interfaces.IVertex;

/**
 * The goal of this interface is to handle the different services (microservices) that are available in your current
 * environment. You can (un)bind services but also handle the existence of the different services.
 */
public interface IServiceHandler {

    /**
     * Allows you to bind a microservice to a vertex
     * @param vertex The vertex that has to be used.
     * @return The microservice that is bound to the vertex.
     */
    IMicroservice bindService(IVertex vertex);

    IMicroservice getServiceByVertex(IVertex vertex);

    IMicroservice peekServiceName(String serviceName);

    int availableServices();

    /**
     * allows you to unbind a service from a vertex.
     * @param microservice The microservice that is free again.
     */
    void unbindMicroservice(IMicroservice microservice);

    /**
     * Releases a microservice of the pool of usable microservices.
     * @param id The id that references to the microservice.
     */
    void releaseMicroservice(String id);

    /**
     * Allows you to add a new microservice to the pool of managed microservices. When ever you add a new microservice
     * a new ID is created. This one is returned for later use.
     * @param connectionInformation The connection information to the microservice.
     * @return The created id to identify the microservice.
     */
    void addMicroservice(String id, IConnectionInformation connectionInformation);

    void addMicroservice(String ip, CommunicationMessages.ServiceRegistrationMessage serviceRegistrationMessage);
}
