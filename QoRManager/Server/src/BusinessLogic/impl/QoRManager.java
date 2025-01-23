package BusinessLogic.impl;

import ApplicationSupport.IApplicationParameter;
import BusinessLogic.IApplication;
import BusinessLogic.QoRHelper.QoRTestRunHelper;
import Comparator.DecisionAid.DataModel.NormalizationMode;
import DecisionMaking.EdgeDiC.EdgeDiCManager;
import DecisionMaking.IDecisionMaking;
import DecisionMaking.MobiDic.MobiDiCManager;
import AvailableResources.IServiceHandler;
import DecisionMaking.SupportedDecisionMakers;
import Events.IConsoleInputListener;
import Events.IMessageReceivedListener;
import IO.IManagingMeasurement;
import IO.impl.AccessDrive;
import IO.impl.ManagingMeasurement;
import Monitoring.Enums.MeasurableValues;
import Network.Connection.ICommunication;
import Network.Connection.IConnectionInformation;
import Network.DataModel.CommunicationMessages;
import Network.Facade.IConnectionFacade;
import Parser.Graph.GraphOnlineParser;
import Structures.Graph.Generator;
import Structures.Graph.SimplifiedGraph.GraphSimplifier;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import CreatorTestData.TestGraphCreator;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static io.github.atomfinger.touuid.UUIDs.toUUID;

/**
 * Responsible for handling the different running applications and to gather and providing necessary parameters for decision-making
 * Side note: Runs constantly, waiting for requests to start and manage applications
 */
public class QoRManager implements IConsoleInputListener, IMessageReceivedListener {
    private static final Logger _Logger = LogManager.getLogger("executionLog");
    private static final IManagingMeasurement _MeasurementLogger = new ManagingMeasurement();
    private final IConnectionInformation _QoRManagementServerInformation;
    private final IConnectionFacade _ConnectionFacade;
    private final ConcurrentLinkedDeque<IApplication> _Applications;
    private ICommunication _Communication;
    private final List<SupportedDecisionMakers> m_DecisionMakersDone;
    private final IServiceHandler _Services;
    private Thread _ServerThread;
    private IGraph m_CurrentGraph;

    //TestbedParameters
    private String _CurrentGraphLocation;
    private int _CurrentTestRun = 0;
    private Boolean _SetupLog = true;
    private int _CurrentDeadline = 0;
    private SupportedDecisionMakers _CurrentDecisionMaker;
    private IApplicationParameter _ApplicationParameter;
    private int _ProcessedPictures;

    public void addApplication(IApplication application){
        if(!_Applications.contains(application))
            _Applications.add(application);
    }

    public void removeApplication(IApplication application){
        _Applications.remove(application);
    }


    public QoRManager(IConnectionInformation qoRManagementServerInformation, IServiceHandler serviceHandler, IConnectionFacade connectionFacade) {
        _ConnectionFacade = connectionFacade;
        _QoRManagementServerInformation = qoRManagementServerInformation;
        _Applications = new ConcurrentLinkedDeque<>();
        _Services = serviceHandler;
        m_DecisionMakersDone = new ArrayList<>();

        //TESTING ONLY
        _ProcessedPictures = 0;
    }

    public QoRManager(IConnectionInformation qoRManagementServerInformation, IServiceHandler serviceHandler,
                      IConnectionFacade connectionFacade, IApplicationParameter applicationParameter) {
        this(qoRManagementServerInformation, serviceHandler, connectionFacade);
        _ApplicationParameter = applicationParameter;
    }

    public void start() throws IOException {
        _Logger.debug("Start server with parameter: Port: " + _QoRManagementServerInformation.getManagementPort());
        _Communication = _ConnectionFacade.startServer(_QoRManagementServerInformation.getManagementPort());
        _Communication.addMessageReceivedListener(this);
        _ServerThread = new Thread(_Communication);
        _ServerThread.start();
    }

    private void initializeServers() throws IOException {
        if (_ApplicationParameter.getSimulationMode()) {
            var subgraphVertices = new GraphSimplifier().getSubgraphs(m_CurrentGraph).stream().map(IGraph::getAllVertices).toList();
            var graphLoader = new GraphOnlineParser(_Logger);
            for (var vertex : m_CurrentGraph.getAllVertices()) {
                var parameter = graphLoader.handleCostForVertex(null, vertex);

                var hasToWaitFor = 0;

                for(var sgv : subgraphVertices) {
                    if(!sgv.contains(vertex)) {
                        continue;
                    }
                    var invalid = false;
                    for(var v : sgv) {
                        if(v.getStage() == vertex.getStage() && v.getApplicationIndex() == vertex.getApplicationIndex()) {
                            invalid = true;
                        }
                    }
                    if(invalid) {
                        continue;
                    }
                    hasToWaitFor++;
                }
                if(hasToWaitFor < 1) {
                    hasToWaitFor = 1;
                }

                sendSimulation(vertex, parameter, hasToWaitFor, m_CurrentGraph.isEndVertex(vertex));
            }
        }
    }

    private void sendSimulation(IVertex vertex, List<CommunicationMessages.TestSetupParameter> parameters, int hasToWaitFor, boolean isEndVertex) {
        try {
            var service = _Services.getServiceByVertex(vertex);
            if (service == null) {
                // Check for start vertex
                if (vertex.getId().equals(toUUID(0)))
                    return;
                /** If the vertex isn't the start vertex and has no service then the services have not been bound yet
                 * this is the case for our standard simulation and as it is only for testing purposes we peek in the
                 * service names here to find the service that will be bound to the corresponding vertex however this
                 * is NOT recommended for real usecases!
                 */
                service = _Services.peekServiceName(vertex.getServiceName());

                if(service == null)
                    throw new RuntimeException("Couldn't find bound service or service with matching service name for" +
                             " vertex " + vertex.getLabel() + " with servicename " + vertex.getServiceName());

            }
            var simulationMessageBuilder = CommunicationMessages.TestSetupMessage.newBuilder()
                    .setJunkMB(0);

            CommunicationMessages.TestSetupMessage simulationMessage;
            if (!parameters.isEmpty()) {
                simulationMessage = simulationMessageBuilder
                        .addAllSimulatedParameters(parameters)
                        .setCallsToWaitFor(hasToWaitFor)
                        .setIsEndVertex(isEndVertex)
                        .build();
            } else {
                simulationMessage = simulationMessageBuilder
                        .setCallsToWaitFor(hasToWaitFor)
                        .setIsEndVertex(isEndVertex)
                        .build();
            }

            var message = Any.pack(simulationMessage);
            service.sendMessage(message, "inference");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleNextIteration() throws InterruptedException, IOException {
        if (!_Applications.isEmpty())
            return;

        var continueTestRun = prepareNextIteration(false);
        if (continueTestRun) {
            Thread.sleep(10);
            startApplication(buildStartMessage());
        } else {
            _Logger.info("Execution is done.");
        }
    }

    /**
     * This method checks for all possible combinations of decision maker, test retries, deadlines and graphs. It returns
     * false, if all test runs are done, otherwise true.
     * @param isApplicationStartup determines whether the current iteration is triggered by the application startup.
     *                             If so, we set the deadline to initial deadline.
     * @return false if done, otherwise true.
     */
    private boolean prepareNextIteration(boolean isApplicationStartup) {
        var isNextIterationAvailable = false;
        if (isApplicationStartup) {
            _CurrentDeadline = _ApplicationParameter.getInitialDeadline();
            setNextGraph();
            _Logger.info("Start graph 1 from " + _ApplicationParameter.getGraphLocations().size());
        }

        var indexCurrentGraph = _ApplicationParameter.getGraphLocations().indexOf(_CurrentGraphLocation);
        if (_ApplicationParameter.getDecisionMakerToUse().equals("all")) {
            // Check if all decision makers had the chance to process the graph.
            // we have to check for length - 1, because this method is called before the m_DecisionMakersDone is updated.
            if (m_DecisionMakersDone.size() == SupportedDecisionMakers.values().length - 1) {
                _CurrentDecisionMaker = null;
                m_CurrentGraph = null;
                m_DecisionMakersDone.clear();
                _CurrentTestRun++;

                // 1. check if we have to increase the deadline for the graph
                if (_CurrentTestRun >= _ApplicationParameter.getTestRepeats() && !isApplicationStartup) {
                    var helper = new QoRTestRunHelper();
                    var nextDeadline = helper.nextDeadline(_ApplicationParameter, _CurrentDeadline);
                    if (nextDeadline != _CurrentDeadline) {
                        if (nextDeadline > 0) {
                            _CurrentTestRun = 0;
                            _CurrentDeadline = nextDeadline;
                            isNextIterationAvailable = true;
                        }
                        // 2. if not, check if we have to change the graph
                        else if (indexCurrentGraph < _ApplicationParameter.getGraphLocations().size() - 1) {
                            _CurrentTestRun = 0;
                            _CurrentDeadline = _ApplicationParameter.getInitialDeadline();
                            setNextGraph();
                            _Logger.info("Start graph " + indexCurrentGraph + " from " + _ApplicationParameter.getGraphLocations().size());
                            isNextIterationAvailable = true;
                        }
                    }
                } else {
                    isNextIterationAvailable = true;
                }
            } else {
                if (_CurrentDecisionMaker != null)
                    m_DecisionMakersDone.add(_CurrentDecisionMaker);
                isNextIterationAvailable = true;
            }
        } else {
            if (_CurrentTestRun < _ApplicationParameter.getTestRepeats() && _CurrentDeadline <= _ApplicationParameter.getFinalDeadline()) {
                _CurrentTestRun++;
                isNextIterationAvailable = true;
            } else if (indexCurrentGraph < _ApplicationParameter.getGraphLocations().size() - 1) {
                _CurrentTestRun = 0;
                _CurrentDeadline = _ApplicationParameter.getInitialDeadline();
                setNextGraph();
                _Logger.info("Start graph " + indexCurrentGraph + " from " + _ApplicationParameter.getGraphLocations().size());
                isNextIterationAvailable = true;
            }
        }
        if (isNextIterationAvailable) {
            setNextDecisionMaker();
        }

        return isNextIterationAvailable;
    }

    private void setNextDecisionMaker() {
        if (_ApplicationParameter.getDecisionMakerToUse().equalsIgnoreCase("all")) {
            if (m_DecisionMakersDone.contains(_CurrentDecisionMaker) || _CurrentDecisionMaker == null) {
                if (!m_DecisionMakersDone.contains(SupportedDecisionMakers.TOPSIS))
                    _CurrentDecisionMaker = SupportedDecisionMakers.TOPSIS;
                else if (!m_DecisionMakersDone.contains(SupportedDecisionMakers.ECONSTRAINT))
                    _CurrentDecisionMaker = SupportedDecisionMakers.ECONSTRAINT;
                else if (!m_DecisionMakersDone.contains(SupportedDecisionMakers.MOBIDIC))
                    _CurrentDecisionMaker = SupportedDecisionMakers.MOBIDIC;
                else
                    _CurrentDecisionMaker = null;
            }
        } else {
            _CurrentDecisionMaker = SupportedDecisionMakers.valueOf(_ApplicationParameter.getDecisionMakerToUse());
        }
    }

    private void setNextGraph() {
        var graphLocations = _ApplicationParameter.getGraphLocations();
        if (_CurrentGraphLocation == null || _CurrentGraphLocation.isEmpty()) {
            _CurrentGraphLocation = graphLocations.get(0);
        } else {
            var index = graphLocations.indexOf(_CurrentGraphLocation);
            index++;
            if (index < graphLocations.size()) {
                _CurrentGraphLocation = graphLocations.get(index);
            }
        }
        _Logger.info("Select new graph. Graph selected: " + _CurrentGraphLocation);
    }

    private void startApplication(CommunicationMessages.ApplicationStartMessage startMessage){
        _Logger.debug("Request start of new application.");
        try {
            var timesMeasured = Collections.synchronizedList(new ArrayList<>());
            var generationStartTime = System.currentTimeMillis();
            if (m_CurrentGraph == null) {
                var graphLocation = startMessage.getGraphLocation();

                if(graphLocation.toLowerCase().endsWith("xlsx")){
                    var generator = new Generator(graphLocation);
                    m_CurrentGraph = generator.generateGraph();
                } else {
                    var graphLoader = new GraphOnlineParser(_Logger);

                    //Because we want to randomize the costs here we choose a new UUID for our graph
                    var graphID = UUID.randomUUID();
                    var creator = new TestGraphCreator(_Logger);
                    m_CurrentGraph = creator.randomizeGraphCostWithAdvancedParameters(graphLoader.loadBaseGraph(graphLocation, graphID));
                    creator.setPathWithPerfectQoRAcrossSubgraphs(m_CurrentGraph);
                    if (_ApplicationParameter.storeRandomizedGraphs())
                        graphLoader.saveGraphToXML(m_CurrentGraph, "logs/savedGraphs/graph_" + graphID + ".graphml");
                }
            }
            initializeServers();

            timesMeasured.add(System.currentTimeMillis() - generationStartTime);
            var decisionMakingInitialization = System.currentTimeMillis();

            var valuesOfInterest = new LinkedList<Pair<String, Boolean>>();
            valuesOfInterest.add(new Pair<>(MeasurableValues.TIME.name(), false));
            valuesOfInterest.add(new Pair<>(MeasurableValues.ENERGY.name(), false));
            valuesOfInterest.add(new Pair<>(MeasurableValues.COST.name(), false));
            valuesOfInterest.add(new Pair<>(MeasurableValues.QoR.name(), true));

            var valueOfMostInterest = new Pair<>(MeasurableValues.QoR.name(), true);

            var constraints = new HashMap<String, Number>();
            constraints.put(MeasurableValues.TIME.name(), _CurrentDeadline);

            var weights = new Double[valuesOfInterest.size()];
            //Arrays.fill(weights, 1d);
            weights[0] = 1.0d;
            weights[1] = 0.25d;
            weights[2] = 0.25d;
            weights[3] = 0.6d;

            var normalizingMode = NormalizationMode.LINEAR;

            IDecisionMaking decisionMaker;
            if (SupportedDecisionMakers.MOBIDIC.equalsName(startMessage.getApplicationName())){
                decisionMaker = new MobiDiCManager(m_CurrentGraph);
            } else {
                decisionMaker = new EdgeDiCManager(m_CurrentGraph, valuesOfInterest, constraints, weights, normalizingMode, valueOfMostInterest, _Logger, startMessage.getDecisionMaker());
            }

            timesMeasured.add(System.currentTimeMillis() - decisionMakingInitialization);

            var app = new ApplicationManager(_Services, _Logger, _MeasurementLogger, timesMeasured, 0, decisionMaker,
                    _QoRManagementServerInformation, startMessage.getContentList(), _ApplicationParameter.getPictureBatchSize(),
                    _ApplicationParameter.getSimulationMode(), _SetupLog, _CurrentDeadline, startMessage.getDecisionMaker(), _CurrentTestRun);
            _SetupLog = false;

            addApplication(app);
            _Logger.info("Starting application \"" + startMessage.getApplicationName() + "\"...");
            app.run();
            Thread.sleep(50);

            if (!app.isRunning()) {
                removeApplication(app);
                handleNextIteration();
            }
        } catch (IOException | InterruptedException e) {
            _Logger.error("Exception thrown: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void HandleConsoleInput(String message) {
        message = message.toLowerCase(Locale.ROOT).trim();

        if (message.equals("exit")) {
            if (_Communication != null) {
                try {
                    _Communication.close();
                } catch (IOException e) {
                    _Logger.fatal("Exception: ", e);
                }
            }
            System.exit(0);
        }

        if(message.equals("start")){
            _CurrentTestRun = 0;
            _CurrentDeadline = _ApplicationParameter.getInitialDeadline();
            _CurrentGraphLocation = null;
            try {
                prepareNextIteration(true);
                startApplication(buildStartMessage());
            } catch (Exception e) {
                _Logger.fatal("Exception: ", e);
            }
        }
    }

    @Override
    public void messageReceived(byte[] message, IConnectionInformation from){
        if (message != null)
        {
            try {
                var anyMessage = Any.parseFrom(ByteString.copyFrom(message));

                //MEASUREMENT
                if (anyMessage.is(CommunicationMessages.MeasurementEvent.class)) {
                    onMeasurementHappened(anyMessage, from);
                }

                //STARTMESSAGE
                else if(anyMessage.is(CommunicationMessages.ApplicationStartMessage.class)){
                    var startMessage = anyMessage.unpack(CommunicationMessages.ApplicationStartMessage.class);
                    _Logger.debug("Starting application...");
                    startApplication(startMessage);
                }

                //REGISTER SERVICE
                else if(anyMessage.is(CommunicationMessages.ServiceRegistrationMessage.class)){
                    var serviceRegistrationMessage = anyMessage.unpack(CommunicationMessages.ServiceRegistrationMessage.class);
                    _Services.addMicroservice(from.getIPAddress(), serviceRegistrationMessage);
                }

                //TASK MESSAGE
                else if(anyMessage.is(CommunicationMessages.TaskLifecycle.class)){
                    var taskLifecylce = anyMessage.unpack(CommunicationMessages.TaskLifecycle.class);
                    //TASK REQUEST
                    if(taskLifecylce.hasTaskRequest()) {
                        var taskMessage = taskLifecylce.getTaskRequest();
                        for(int i = 0; i < taskMessage.getContentCount(); i++) {
                            _Logger.debug(taskMessage.getContent(i));
                        }
                    }
                    //RETURN MESSAGE
                    else if(taskLifecylce.hasReturnMessage()){
                        var returnMessage = taskLifecylce.getReturnMessage();
                        for(var result : returnMessage.getResultList()){
                            var imageByte = Base64.getDecoder().decode(result);
                            var bis = new ByteArrayInputStream(imageByte);
                            try {
                                var image = ImageIO.read(bis);
                                bis.close();

                                var outputfile = getOutputFile();
                                var driveHandler = new AccessDrive();
                                driveHandler.writeImageToDisk(image, outputfile, "jpg");
                            } catch (Exception e){
                                _Logger.fatal("Exception: ", e);
                            }
                            _ProcessedPictures++;
                        }

                        while(!_Applications.isEmpty() && _Applications.getFirst().isRunning()) {
                            try {
                                Thread.sleep(1);
                            }
                            catch (InterruptedException ignored) {}
                        }

                        _Applications.remove(0);

                        if(_ProcessedPictures < _ApplicationParameter.getMaxPictureCount()) {
                            startApplication(buildStartMessage());
                        }
                    }
                    //TERMINATION MESSAGE
                    else if(taskLifecylce.hasTerminationMessage()) {
                        var senderID = taskLifecylce.getSenderID();
                        var processStates = taskLifecylce.getProcessStatesList();
                        var terminationMessage = taskLifecylce.getTerminationMessage();
                        for(var it = _Applications.iterator(); it.hasNext(); ) {
                            var app = it.next();
                            if (app.onMicroserviceTerminated(senderID, processStates, terminationMessage)) {
                                it.remove();
                                handleNextIteration();
                            }
                        }
                    }
                }

            } catch (InterruptedException | IOException e) {
                _Logger.error("Exception: ", e.getMessage());
            }
        }
    }

    private File getOutputFile() {
        var fileName = "";

        if(_ApplicationParameter.getDecisionMakerToUse().equalsIgnoreCase("mobidic"))
            fileName += "MOBIDIC_resultImage";
        else
            fileName += "EDGEDIC_"+ _ApplicationParameter.getDecisionMakerToUse() +"_resultImage";

        fileName += (_ProcessedPictures + 1 + ".jpg");

        return new File(_ApplicationParameter.getTargetLocation() + fileName);
    }


    private CommunicationMessages.ApplicationStartMessage buildStartMessage() throws IOException {
        var message = CommunicationMessages.ApplicationStartMessage.newBuilder()
                .setApplicationName(_ApplicationParameter.getApplicationName())
                .setGraphLocation(_CurrentGraphLocation);

        message = message.setDecisionMaker(_CurrentDecisionMaker.name());

        var accessDrive = new AccessDrive();
        for(int i = 1; i <= _ApplicationParameter.getPictureBatchSize(); i++){
            var content = accessDrive.getContentFromFileAsString(_ApplicationParameter.getContentLocation() + "Picture" + (i + _ProcessedPictures) + ".jpg");
            message.addContent(content);
        }

        return message.build();
    }

    private void HandleMeasurementEvent(CommunicationMessages.MeasurementEvent event, IConnectionInformation from) {
        for(var it = _Applications.iterator(); it.hasNext(); ) {
            var app = it.next();
            app.onMeasurementUpdate(event, from);
        }
    }

    private void onMeasurementHappened(Any anyMessage, IConnectionInformation from) throws InvalidProtocolBufferException {
        var messageFromServer = anyMessage.unpack(CommunicationMessages.MeasurementEvent.class);

        if(messageFromServer.hasRam())
            _Logger.debug(messageFromServer.getUUID() + " RAM " + messageFromServer.getRam().getAvailableMemory());
        if(messageFromServer.hasCpu())
            _Logger.debug(messageFromServer.getUUID() + " CPU " + messageFromServer.getCpu().getCpuUsage());
        if(messageFromServer.hasNetwork())
            _Logger.debug(messageFromServer.getUUID() + " LATENCY " + messageFromServer.getNetwork().getRtt());

        HandleMeasurementEvent(messageFromServer, from);
    }
}