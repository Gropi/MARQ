package BusinessLogic.impl;

import BusinessLogic.IApplication;
import DecisionMaking.IDecisionMaking;
import AvailableResources.IServiceHandler;
import DecisionMaking.MobiDic.MobiDiCManager;
import Helper.VectorClock;
import IO.IManagingMeasurement;
import Measurement.MicroserviceChainMeasurement;
import Monitoring.Enums.MeasurableValues;
import Network.Connection.IConnectionInformation;
import Network.DataModel.CommunicationMessages;
import Services.IMicroservice;
import Structures.Graph.Edge;
import Structures.Graph.interfaces.IVertex;
import com.google.protobuf.Any;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.atomfinger.touuid.UUIDs.toUUID;

public class ApplicationManager implements IApplication {
    private final Logger _Logger;
    private final IManagingMeasurement _MeasurementLogger;
    private final IConnectionInformation _MyConnectionInformation;

    private final IServiceHandler _ServiceHandler;
    private final IDecisionMaking _DecisionMaker;
    private final AtomicBoolean _Running;
    private long _StartTime;
    private final Map<IVertex, List<Integer>> _CurrentExecutingVertices;
    private final List<IMicroservice> _RelevantMicroservices;
    private final List<String> _InitialContent;
    private final int _PicturesBatchSize;
    private final int _Iteration;

    // MICROSERVICE COORDINATION
    private final Map<String, List<CommunicationMessages.Endpoint>> _CurrentFollowers;
    private final Map<Integer, Map<String, List<CommunicationMessages.Endpoint>>> _FollowersForProcessState;
    private final Map<String, List<Integer>> _CurrentSGIndices;
    private final Map<Integer, Map<String, List<Integer>>> _SGIndicesForProcessState;
    private final VectorClock _Clock;

    //COLLECTOR PARAMETERS
    private final int m_CollectorFrequency;
    private final int m_PositivePercentage;
    private final int m_NegativePercentage;

    //ONLY FOR TESTING
    private final boolean _Simulation;
    private boolean _SetupLog;
    private int _PictureBatchStart;
    private MicroserviceChainMeasurement _Measurement;
    private final int _Deadline;
    private final String _NameDecisionMaker;

    public ApplicationManager(IServiceHandler serviceHandler, Logger logger, IManagingMeasurement measurementLogger,
                              List<Object> timesMeasured, int baseImage, IDecisionMaking decisionMaker, IConnectionInformation myConnectionInformation,
                              List<String> initialContent, int picturesPerCycle, boolean simulation, boolean setupLog, int deadline, String nameDecisionMaker,
                              int iteration) {
        _Logger = logger;
        _MeasurementLogger = measurementLogger;
        _Running = new AtomicBoolean(false);
        _MyConnectionInformation = myConnectionInformation;
        _Deadline = deadline;
        _Iteration = iteration;
        _NameDecisionMaker = nameDecisionMaker;

        _ServiceHandler = serviceHandler;
        _DecisionMaker = decisionMaker;
        _RelevantMicroservices = new ArrayList<>();
        _CurrentExecutingVertices = new HashMap<>();

        _InitialContent = initialContent;
        _PicturesBatchSize = picturesPerCycle;
        _SetupLog = setupLog;

        // MICROSERVICE COORDINATION
        var processID = "app_" + UUID.randomUUID();
        _CurrentFollowers = new HashMap<>();
        _FollowersForProcessState = new HashMap<>();
        _Clock = new VectorClock(processID);

        _CurrentSGIndices = new HashMap<>();
        _SGIndicesForProcessState = new HashMap<>();

        //COLLECTOR PARAMETERS
        m_CollectorFrequency = 10;
        m_PositivePercentage = 5;
        m_NegativePercentage = 5;

        //ONLY FOR TESTING
        _Simulation = simulation;
        _PictureBatchStart = baseImage;
    }

    private synchronized void saveCurrentStateOnVectorClockIncrease(Map<String, List<CommunicationMessages.Endpoint>> currentFollowers,
                                                                    Map<String, List<Integer>> currentSGIndices) {
        var stateToStore = new HashMap<String, List<CommunicationMessages.Endpoint>>();
        var SGIndicesToStore = new HashMap<String, List<Integer>>();
        for(var key : currentFollowers.keySet()) {
            stateToStore.put(key, currentFollowers.get(key));
        }
        for(var key : currentSGIndices.keySet()) {
            SGIndicesToStore.put(key, currentSGIndices.get(key));
        }
        var stateID = _Clock.getValueForProcessID(_Clock.getProcessKey());
        _FollowersForProcessState.put(stateID, stateToStore);
        _SGIndicesForProcessState.put(stateID, SGIndicesToStore);
        _Clock.increase();
    }

    private int findRelevantProcessState(List<CommunicationMessages.ProcessState> processStates) {
        var relevantProcessState = -1;
        for(var state : processStates) {
            if(state.getKey().equalsIgnoreCase(_Clock.getProcessKey())) {
                relevantProcessState = state.getValue();
            }
        }
        if(relevantProcessState < 0) {
            throw new RuntimeException("The key of the application-process must be part of the services vector clock. Key wasn't found.");
        }
        return relevantProcessState;
    }

    @Override
    public void run() {
        if (!_Running.get()) {
            _Running.set(true);
        } else {
            return;
        }

        setupLogging();

        if (initializeServiceLandscape(_Deadline)) {
            var followingVertices = _DecisionMaker.getNextVerticesFrom(null, _StartTime);
            var followers = followingVertices.keySet();
            for (var follower : followers) {
                if(!_Simulation && follower.getStage() == 0) {
                    _CurrentExecutingVertices.put(follower, followingVertices.get(follower));
                    // The initial vertex in the blurring pipeline is an empty model therefore it has no microservice
                    _CurrentSGIndices.put(follower.getId().toString(), followingVertices.get(follower));
                    continue;
                }
                _CurrentExecutingVertices.put(follower, followingVertices.get(follower));
                _CurrentSGIndices.put(follower.getMicroservice().ID(), followingVertices.get(follower));
            }

            _Logger.debug("Found " + followingVertices.size() + " followers");

            addTask(_PictureBatchStart, _PicturesBatchSize);
            getStartPictureForNextBatch(_PicturesBatchSize);

            if (!(_DecisionMaker instanceof MobiDiCManager)) {
                try {
                    subscribeToMeasurements();
                } catch (IOException e) {
                    _Logger.fatal("Failed tu subscribe measurement. Exception: ", e);
                }
            }
        } else {
            exit();
        }
    }

    private VectorClock toVectorClock(String processKey, List<CommunicationMessages.ProcessState> processStates) {
        var newClock = new HashMap<String, Integer>();

        for(var processState : processStates) {
            newClock.put(processState.getKey(), processState.getValue());
        }

        return new VectorClock(processKey, newClock);
    }

    @Override
    public boolean onMicroserviceTerminated(String senderID, List<CommunicationMessages.ProcessState> processStates,
                                            CommunicationMessages.TerminationMessage terminationMessage) throws InterruptedException, IOException {
        synchronized (_Clock) {
            //Check message for relevance and update clock
            var otherClock = toVectorClock(senderID, processStates);
            if(!_Clock.isNewer(otherClock)){
                _Logger.debug("Received old message.");
                return false;
            }
            _Clock.addSingleProcessState(otherClock);

            //Store clock-state before any calculations to avoid race-conditions
            var currentClockState = _Clock.toProcessState();
            var relevantProcessState= findRelevantProcessState(processStates);

            var terminatedVertex = _DecisionMaker.getVertexMicroserviceID(terminationMessage.getUUID());
            var listOfEdges = _DecisionMaker.getOutgoingEdge(terminatedVertex);
            if (listOfEdges.isEmpty()) {
                _Logger.info("ATTENZIONE/ACHTUNG/ATTENTION PLEASE/BLYAT/GING ZHU YI/JA MOIN: No outgoing edges. This should only happen if graph terminates.");
            }

            synchronized (_Measurement) {
                _Logger.debug(terminatedVertex.getServiceName() + "; Service terminated: " + terminationMessage.getUUID());
                var indicesForState = _SGIndicesForProcessState.get(relevantProcessState);
                var indices = indicesForState.get(senderID);
                if(indices != null) {
                    _Measurement.addSelectedVertex(terminatedVertex, terminationMessage, indices);
                }
            }

            if (_DecisionMaker.isEndVertex(terminatedVertex) || terminationMessage.hasError()) {
                unsubscribeFromMeasurements();

                _Logger.debug("Graph terminated");
                if (terminationMessage.hasError()) {
                    _MeasurementLogger.writeLine(terminatedVertex.getServiceName() + "; " + terminationMessage.getError());
                } else {
                    logFinalSelection(_Measurement);
                }

                uninitializeServiceLandscape();
                _DecisionMaker.cleanUp();
                exit();
                return true;
            } else {
                _CurrentExecutingVertices.remove(terminatedVertex);

                var states = _FollowersForProcessState.get(relevantProcessState);
                var successors = states.get(senderID);

                for(var successorInfo : successors) {
                    var successor = _DecisionMaker.getVertexByIP(successorInfo.getIP(), successorInfo.getPort());
                    _CurrentExecutingVertices.put(successor, _SGIndicesForProcessState.get(relevantProcessState).get(senderID));
                    var nextVertices = _DecisionMaker.getNextVerticesFrom(successor, _StartTime);
                    if (successor != null) {
                        if (!_DecisionMaker.isEndVertex(successor))
                            sendTargetUpdate(successor, nextVertices, currentClockState);
                        else {
                            if(successor.getMicroservice() != null) {
                                var mySuccessorList = new ArrayList<CommunicationMessages.Endpoint>();
                                mySuccessorList.add(
                                        CommunicationMessages.Endpoint.newBuilder()
                                                .setIP(_MyConnectionInformation.getIPAddress())
                                                .setPort(_MyConnectionInformation.getManagementPort())
                                                .build()
                                );
                                sendTargetUpdate(successor.getMicroservice(), mySuccessorList, currentClockState);
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    @Override
    public void onMeasurementUpdate(CommunicationMessages.MeasurementEvent event, IConnectionInformation from){
        //CHECK WHETHER MEASUREMENT IS OF INTEREST FOR THIS APPLICATION MANAGER
        var measurementOfInterest = false;
        IMicroservice handledService = null;
        var vertex = _DecisionMaker.getVertexByIP(from.getIPAddress(), from.getManagementPort());
        var vertices = new ArrayList<IVertex>();
        vertices.add(vertex);

        for(var service : _RelevantMicroservices) {
            if(event.getUUID().equalsIgnoreCase(service.ID())){
                measurementOfInterest = true;
                handledService = service;
            }
        }

        if(!measurementOfInterest) {
            _Logger.debug("NO INTEREST IN: " + event.getUUID());
            return;
        }

        //HANDLE MEASUREMENT OF INTEREST
        if(event.hasCpu()){
            var cpuMeasurement = event.getCpu();
            _Logger.debug(handledService.getAddress() + " " + cpuMeasurement.getDate() + " " + cpuMeasurement.getTime() +" - CPU : "+ cpuMeasurement.getCpuUsage());

            _DecisionMaker.updateRemainingVertices(vertices, new ArrayList<>(), MeasurableValues.CPU.name(), ((int) cpuMeasurement.getCpuUsage()*100), event.getUUID());
        }

        if(event.hasRam()){
            var ramMeasurement = event.getRam();
            _Logger.debug(handledService.getAddress() + " " + ramMeasurement.getDate() + " " + ramMeasurement.getTime() +" - RAM : "+ ramMeasurement.getAvailableMemory());
            _DecisionMaker.updateRemainingVertices(vertices, new ArrayList<>(), MeasurableValues.RAM.name(), ramMeasurement.getAvailableMemory() * (-1) ,event.getUUID());
            //BECAUSE THE ALGORITHM SEARCHES FOR A MINIMUM WE WANT THE AVAILABLE RAM TO BE NEGATIVE
        }

        if(event.hasNetwork()){
            var networkMeasurement = event.getNetwork();
            _Logger.debug(handledService.getAddress() + " " + networkMeasurement.getDate() + " " + networkMeasurement.getTime() +" - RTT : "+ networkMeasurement.getRtt());
            _DecisionMaker.updateRemainingEdges(vertices, new ArrayList<>(), MeasurableValues.LATENCY.name(), (int)networkMeasurement.getRtt(), event.getUUID(), networkMeasurement.getTargetID());
        }

        //Store clock-state BEFORE calculation to avoid message race-conditions
        var currentClockState = _Clock.toProcessState();
        var vertexCandidates = _DecisionMaker.getNextVerticesFrom(vertex, _StartTime);
        if (vertex != null && vertexCandidates != null && !vertexCandidates.isEmpty())
            sendTargetUpdate(vertex, vertexCandidates, currentClockState);
    }

    private void addTask(int startImage, int range) {
        startObjectDetectionStage(startImage, range);
        if (!_Simulation)
            sendImagesToDeployer(startImage, range);
    }

    private void getStartPictureForNextBatch(int range) {
        _PictureBatchStart += range;
    }

    private void startObjectDetectionStage(int startImage, int range) {
        try{
            Map<IVertex, List<Integer>> currentVertices;

            if(_Simulation) {
                currentVertices = _CurrentExecutingVertices;
            } else {
                currentVertices = _DecisionMaker.getNextVerticesFrom((IVertex) _CurrentExecutingVertices.keySet().toArray()[0], _StartTime);
            }

            _Logger.debug("Vertices on start: " + currentVertices.size());

            for(var currentVertex : currentVertices.keySet()) {
                var nextVertices = _DecisionMaker.getNextVerticesFrom(currentVertex, _StartTime);
                var currentMicroservice = currentVertex.getMicroservice();

                var serviceID = currentVertex.getMicroservice().ID();

                var targetUpdates = new ArrayList<CommunicationMessages.Endpoint>();

                for(var nextVertex : nextVertices.keySet()) {
                    var targetIP = nextVertex.getMicroservice().getAddress();
                    var targetPort = nextVertex.getMicroservice().getPort("inference");

                    var targetUpdate = CommunicationMessages.Endpoint.newBuilder()
                            .setIP(targetIP)
                            .setPort(targetPort)
                            .build();

                    targetUpdates.add(targetUpdate);
                    _CurrentSGIndices.put(nextVertex.getMicroservice().ID(), nextVertices.get(nextVertex));
                }

                var execMessageBuilder = CommunicationMessages.TaskRequest.newBuilder()
                        .addAllTargets(targetUpdates)
                        .setManagementIP(_MyConnectionInformation.getIPAddress())
                        .setManagementPort(_MyConnectionInformation.getManagementPort());

                for (int i = startImage; i < startImage + range; i++) {
                    execMessageBuilder.addContent(_InitialContent.get(i));
                }

                var execMessage = execMessageBuilder.build();
                var taskLifecycle = CommunicationMessages.TaskLifecycle.newBuilder()
                        .setSenderID(_Clock.getProcessKey())
                        .setAppID(_Clock.getProcessKey())
                        .addAllProcessStates(_Clock.toProcessState())
                        .setTaskRequest(execMessage)
                        .build();
                var message = Any.pack(taskLifecycle);

                _CurrentFollowers.put(serviceID, targetUpdates);
                saveCurrentStateOnVectorClockIncrease(new HashMap<>(_CurrentFollowers), new HashMap<>(_CurrentSGIndices));

                currentMicroservice.sendMessage(message, "inference");

                _StartTime = System.currentTimeMillis();

                _Logger.debug("First message sent!");
                _Logger.debug("Receiver: " + currentVertex.getLabel() +  " at " + currentMicroservice.getAddress() + ":" +
                        currentMicroservice.getPort("inference"));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendImagesToDeployer(int startImage, int range) {
        var execMessageBuilder = CommunicationMessages.TaskRequest.newBuilder()
                .addContent("upload_base_images")
                .setManagementPort(2000);

        for (int i = startImage; i < startImage + range; i++) {
            execMessageBuilder.addContent(_InitialContent.get(i));
        }

        var processStates = _Clock.toProcessState();

        var execMessage = execMessageBuilder.build();
        var taskLifecycle = CommunicationMessages.TaskLifecycle.newBuilder()
                .setTaskRequest(execMessage)
                .setAppID(_Clock.getProcessKey())
                .addAllProcessStates(processStates)
                .setSenderID(_Clock.getProcessKey())
                .build();
        var message = Any.pack(taskLifecycle);

        saveCurrentStateOnVectorClockIncrease(new HashMap<>(_CurrentFollowers), new HashMap<>(_CurrentSGIndices));

        var vertices = _DecisionMaker.getAllVertices();

        for(var vertex : vertices) {
            var service = vertex.getMicroservice();
            if(service == null)
                continue;

            if(service.getExecution().equalsIgnoreCase("deploy")){
                try {
                    service.sendMessage(message, "inference");
                } catch (IOException e){
                    _Logger.error("Failed to send client to deployer " + service.ID() + "!");
                }
            }
        }
    }

    private void sendTargetUpdate(IVertex messageToVertex, Map<IVertex, List<Integer>> nextInChain, List<CommunicationMessages.ProcessState> processStates) {
        if (messageToVertex.getMicroservice() == null) {
            // Check for start vertex
            if (messageToVertex.getId().equals(toUUID(0)))
                return;
        }

        var successors = new ArrayList<CommunicationMessages.Endpoint>();
        for (var next : nextInChain.keySet()) {
            successors.add(CommunicationMessages.Endpoint.newBuilder()
                    .setIP(next.getMicroservice().getAddress())
                    .setPort(next.getMicroservice().getPort("inference"))
                    .build()
            );
            _CurrentSGIndices.put(next.getMicroservice().ID(), nextInChain.get(next));
        }

        sendTargetUpdate(messageToVertex.getMicroservice(), successors, processStates);
    }

    private void sendTargetUpdate(IMicroservice messageToService, List<CommunicationMessages.Endpoint> successors, List<CommunicationMessages.ProcessState> processStates) {
        try {
            var targetUpdates = CommunicationMessages.TargetUpdate.newBuilder();

            for(var successor : successors) {
                var targetUpdate = CommunicationMessages.Endpoint.newBuilder()
                        .setIP(successor.getIP())
                        .setPort(successor.getPort())
                        .build();
                targetUpdates.addTargets(targetUpdate);
            }

            var taskLifecycle = CommunicationMessages.TaskLifecycle.newBuilder()
                    .setTargetUpdate(targetUpdates.build())
                    .addAllProcessStates(processStates)
                    .setAppID(_Clock.getProcessKey())
                    .setSenderID(_Clock.getProcessKey())
                    .build();
            var message = Any.pack(taskLifecycle);

            _CurrentFollowers.put(messageToService.ID(), new ArrayList<>(targetUpdates.getTargetsList()));
            saveCurrentStateOnVectorClockIncrease(new HashMap<>(_CurrentFollowers), new HashMap<>(_CurrentSGIndices));

            messageToService.sendMessage(message, "inference");
        } catch (IOException e) {
            _Logger.fatal("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    private boolean initializeServiceLandscape(int deadline) {
        var measurementExpectations = new MicroserviceChainMeasurement(deadline);
        var flow = _DecisionMaker.getInitialSelection(deadline, measurementExpectations);
        var wasInitialized = false;
        if (flow.isEmpty()){ // || !_DecisionMaker.isEndVertex(flow.get(flow.size() - 1).getDestination())) {
            _MeasurementLogger.writeLine(";" + _NameDecisionMaker + ";Failure - no valid configuration;" + deadline);
        } else {
            logInitialSelection(flow.keySet().stream().toList(), measurementExpectations);
            var vertices = _DecisionMaker.getAllVertices();

            _Logger.debug("Found " + flow.size() + " edges and " + vertices.size() + " vertices.");

            for (var vertex : vertices) {
                //The start-vertex represents the QoR-Manager and therefore doesn't need to have a microservice allocated
                if(vertex.getServiceName().equalsIgnoreCase("start"))
                    continue;

                var microservice = _ServiceHandler.bindService(vertex);
                vertex.bindMicroservice(microservice);
                _RelevantMicroservices.add(microservice);
            }

            for (var vertex : vertices) {
                var outgoingEdges = flow.keySet().stream().filter(x -> x.getSource().equals(vertex)).toList();
                var followers = new HashMap<IVertex, List<Integer>>();
                for(var outgoingEdge : outgoingEdges) {
                    List<Integer> subgraphIndexList = new ArrayList<>();
                    if(followers.containsKey(outgoingEdge.getDestination())) {
                        subgraphIndexList = followers.get(outgoingEdge.getDestination());
                    }

                    for(var index : flow.get(outgoingEdge)) {
                        if(!subgraphIndexList.contains(index)){
                            subgraphIndexList.add(index);
                        }
                    }
                    followers.put(outgoingEdge.getDestination(), subgraphIndexList);
                }

                sendTargetUpdate(vertex, followers, _Clock.toProcessState());
            }
            wasInitialized = true;
        }
        return wasInitialized;
    }

    private void uninitializeServiceLandscape() throws InterruptedException, IOException {
        Thread.sleep(10);
        var vertices = _DecisionMaker.getAllVertices();

        for(var vertex : vertices) {
            //The start-vertex represents the QoR-Manager and therefore doesn't need to have a microservice allocated
            if(vertex.getServiceName().equalsIgnoreCase("start"))
                continue;

            var microservice = vertex.getMicroservice();
            if (microservice != null) {
                sendProcessEndsMessage(microservice);
                _ServiceHandler.unbindMicroservice(microservice);
            }
        }
    }

    private void sendProcessEndsMessage(IMicroservice microservice) throws IOException {
        var message = CommunicationMessages.ApplicationEndMessage.newBuilder()
                .setApplicationID(_Clock.getProcessKey())
                .build();
        microservice.sendMessage(message, "inference");
    }

    private void subscribeToMeasurements() throws IOException {
        var vertices = _DecisionMaker.getAllVertices();

        //MEASUREMENT INFORMATION
        var measurementInformation = CommunicationMessages.MeasurementInformation.newBuilder()
                .setExpectedValue(Integer.MIN_VALUE)
                .setNegativeFailure(m_NegativePercentage)
                .setPositiveFailure(m_PositivePercentage)
                .setFrequency(m_CollectorFrequency)
                .build();


        //CPU SUBSCRIPTION
        var cpuSubscription = CommunicationMessages.Subscribe.newBuilder()
                .setType(CommunicationMessages.Types.CPU)
                .setInformation(measurementInformation)
                .build();

        for(var vertex : vertices) {
            //The start-vertex represents the QoR-Manager and therefore doesn't have a microservice
            if(vertex.getServiceName().equalsIgnoreCase("start"))
                continue;

            var microservice = vertex.getMicroservice();
            if (microservice != null) {
                var messages = new ArrayList<Any>();

                messages.add(Any.pack(CommunicationMessages.SubscriptionLifecycle.newBuilder()
                        .setSub(cpuSubscription)
                        .setTargetID(microservice.ID())
                        .build()));
                //SUBSCRIPTIONS TO RELEVANT NETWORK INFORMATION
                var msEges = _DecisionMaker.getOutgoingEdge(vertex);
                for(var msEdge : msEges) {
                    var target = msEdge.getDestination().getMicroservice();
                    var request = CommunicationMessages.NetworkRequest.newBuilder()
                            .setType(CommunicationMessages.NetworkRequestType.ICMP)
                            .setTarget(target.getAddress())
                            .build();
                    var networkSubscription = CommunicationMessages.Subscribe.newBuilder()
                            .setType(CommunicationMessages.Types.LATENCY)
                            .setInformation(measurementInformation)
                            .setRequest(request)
                            .build();
                    messages.add(Any.pack(CommunicationMessages.SubscriptionLifecycle.newBuilder()
                            .setSub(networkSubscription)
                            .setTargetID(microservice.ID())
                            .build()));
                }

                microservice.sendMessages(messages, IMicroservice.HANDLER_PORT_IDENTIFIER);
            }
        }
        _Logger.debug("SEND SUBSCRIPTION TO EVERYONE");
    }

    private void unsubscribeFromMeasurements() throws IOException {
        var vertices = _DecisionMaker.getAllVertices();

        //CPU SUBSCRIPTION
        var cpuUnsubscription = CommunicationMessages.Unsubscribe.newBuilder()
                .setType(CommunicationMessages.Types.CPU)
                .build();
        //RAM SUBSCRIPTION
        var ramUnsubscription = CommunicationMessages.Unsubscribe.newBuilder()
                .setType(CommunicationMessages.Types.RAM)
                .build();

        for(var vertex : vertices) {
            //The start-vertex represents the QoR-Manager and therefore doesn't have a microservice
            if(vertex.getServiceName().equalsIgnoreCase("start"))
                continue;

            var microservice = vertex.getMicroservice();
            if (microservice != null) {
                var messages = new ArrayList<Any>();

                messages.add(Any.pack(CommunicationMessages.SubscriptionLifecycle.newBuilder()
                        .setUnsubscribe(cpuUnsubscription)
                        .setTargetID(microservice.ID())
                        .build()));
                messages.add(Any.pack(CommunicationMessages.SubscriptionLifecycle.newBuilder()
                        .setUnsubscribe(ramUnsubscription)
                        .setTargetID(microservice.ID())
                        .build()));
                //SUBSCRIPTIONS TO RELEVANT NETWORK INFORMATION
                var msEdges = _DecisionMaker.getOutgoingEdge(vertex);
                for(var msEdge : msEdges) {
                    var target = msEdge.getDestination().getMicroservice();
                    var request = CommunicationMessages.NetworkRequest.newBuilder()
                            .setType(CommunicationMessages.NetworkRequestType.ICMP)
                            .setTarget(target.getAddress())
                            .build();
                    var networkUnsubscription = CommunicationMessages.Unsubscribe.newBuilder()
                            .setType(CommunicationMessages.Types.LATENCY)
                            .setRequest(request)
                            .build();
                    messages.add(Any.pack(CommunicationMessages.SubscriptionLifecycle.newBuilder()
                            .setUnsubscribe(networkUnsubscription)
                            .setTargetID(microservice.ID())
                            .build()));
                }

                microservice.sendMessages(messages, IMicroservice.HANDLER_PORT_IDENTIFIER);
            }
        }
        _Logger.debug("SEND UNSUBSCRIPTION TO EVERYONE");
    }

    @Override
    public void exit() {
        _Running.set(false);
    }

    @Override
    public boolean isRunning(){
        return _Running.get();
    }

    private void setupLogging() {
        _Measurement = new MicroserviceChainMeasurement(_Deadline);
        if (_SetupLog) {
            var availableService = _ServiceHandler.availableServices();
            var line = new StringBuilder("GraphLocation;Iteration;Graph-UUID;Method;Type;Deadline;");
            line.append("Total Time; Total QoR;Total Energy;Total Costs;;");
            for (int i = 0; i < availableService; i++) {
                line.append(String.format("Node %1$d Name; Node %1$d Execution Time; Node %1$d Idle Time; Node %1$d Transmission Time; Node %1$d QoR;", i));
            }
            _MeasurementLogger.writeLine(line.toString());
            _SetupLog = false;
        }
    }

    private void logInitialSelection(List<Edge> flow, MicroserviceChainMeasurement measurementExpectations) {
        var handledEdgeIds = new ArrayList<UUID>();
        var firstEdge = flow.get(0);
        var startVertex = firstEdge.getSource();
        var line = new StringBuilder(
                String.format("%1$s;%2$d;%3$s;%4$s;Initial;%5$d;%6$f;%7$d;%8$d;%9$d;;%10$s;%11$d;0;0;%12$d;",
                        _DecisionMaker.getGraphLocation(),
                        _Iteration,
                        _DecisionMaker.getGraphID().toString(),
                        _NameDecisionMaker,
                        measurementExpectations.getDeadline(),
                        measurementExpectations.getInitialTotalTimeTaken(),
                        measurementExpectations.getInitialQoRAcrossPaths(),
                        measurementExpectations.getInitialEnergyAcrossPaths(),
                        measurementExpectations.getInitialCostAcrossPaths(),
                        startVertex.getServiceName(),
                        (int)startVertex.getWeight(MeasurableValues.TIME.name()).getValue(),
                        startVertex.getQoR()));
        for (var edge : flow) {
            if(handledEdgeIds.contains(edge.id())) {
                continue;
            }

            handledEdgeIds.add(edge.id());
            var destination = edge.getDestination();
            if (destination != null) {
                line.append(String.format("%1$s;%2$d;0;%3$d;%4$d;",
                        destination.getServiceName(),
                        (int)destination.getWeight(MeasurableValues.TIME.name()).getValue(),
                        (int)(edge.getWeight(MeasurableValues.LATENCY.name()) != null ? edge.getWeight(MeasurableValues.LATENCY.name()).getValue() : 0),
                        destination.getQoR()));
            }
        }
        _MeasurementLogger.writeLine(line.toString());
    }

    private void logFinalSelection(MicroserviceChainMeasurement measurement) {
        var finalTime = System.currentTimeMillis() - _StartTime;
        var line = new StringBuilder(String.format("%1$s;%2$d;%3$s;%4$s;Measurement;%5$d;%6$d;%7$d;%8$d;%9$d;;",
                _DecisionMaker.getGraphLocation(), _Iteration, _DecisionMaker.getGraphID(), _NameDecisionMaker,
                _Measurement.getDeadline(), finalTime, measurement.getTotalQoRAcrossPaths(), measurement.getTotalEnergy(), measurement.getTotalCost()));
        var additionalInformation = measurement.getAdditionalInformation();
        var subgraphs = additionalInformation.keySet();

        for(var graph : subgraphs) {
            var informationForSG = additionalInformation.get(graph);
            var verticesInSG = informationForSG.keySet();

            for (var vertex : verticesInSG) {
                var infoForVertex = informationForSG.get(vertex);
                line.append(String.format("%1$s;%2$f;%3$f;%4$f;%5$s;",
                        vertex.getServiceName(),
                        (double)infoForVertex.get(MicroserviceChainMeasurement.MAP_KEY_EXECUTION_TIME_CONSUMED),
                        (double)infoForVertex.get(MicroserviceChainMeasurement.MAP_KEY_IDLE_TIME_CONSUMED),
                        (double)infoForVertex.get(MicroserviceChainMeasurement.MAP_KEY_TRANSMISSION_CONSUMED),
                        infoForVertex.get(MicroserviceChainMeasurement.MAP_KEY_QOR)));
            }
        }
        _MeasurementLogger.writeLine(line.toString());
    }
}
