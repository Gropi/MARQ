import ApplicationSupport.impl.ApplicationParameter;
import AvailableResources.impl.MicroserviceHandler;
import Network.Connection.impl.ConnectionInformation;
import Console.ThreadSafeConsoleHandler;
import BusinessLogic.impl.MyLogManager;
import BusinessLogic.impl.QoRManager;
import Monitoring.Event.Logging.impl.LogInformation;
import Network.Facade.impl.ConnectionFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Startup {
    private static final Logger _Logger = LogManager.getLogger("executionLog");

    public static void main(String[] args) throws IOException {
        var logParameters = new LogInformation(args);
        var qorLogManager = new MyLogManager(logParameters);
        qorLogManager.start();
        var startParameters = new ConnectionInformation(args);
        var consoleHandler = new ThreadSafeConsoleHandler();
        var testbedParameters = getTestbedParameters(args);

        var connectionFacade = new ConnectionFacade();

        var microserviceHandler = new MicroserviceHandler(_Logger, connectionFacade);

        QoRManager qorManager;
        if(testbedParameters.isEmpty()) {
            qorManager = new QoRManager(startParameters, microserviceHandler, connectionFacade);
        }else{
            var parameter = new ApplicationParameter(testbedParameters);
            qorManager = new QoRManager(startParameters, microserviceHandler, connectionFacade, parameter);
        }
        consoleHandler.addListener(qorManager);
        consoleHandler.start();
        qorManager.start();
    }

    private static Map<String, String> getTestbedParameters(String[] args) {
        var parameters = new HashMap<String, String>();

        for(int i = 0; i < args.length; i += 2) {
            if(args[i].equalsIgnoreCase("-dm")){
                parameters.put(ApplicationParameter.DECISION_MAKER, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-cl")){
                parameters.put(ApplicationParameter.CONTENT_LOCATION, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-mpc")){
                parameters.put(ApplicationParameter.MAX_PICTURE_COUNT, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-an")){
                parameters.put(ApplicationParameter.APPLICATION_NAME, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-gl")){
                parameters.put(ApplicationParameter.GRAPH_LOCATION, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-gf")){
                parameters.put(ApplicationParameter.GRAPH_FOLDER, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-ppc")){
                parameters.put(ApplicationParameter.PICTURE_PER_CYCLE, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-tl")){
                parameters.put(ApplicationParameter.TARGET_LOCATION, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-sim")){
                parameters.put(ApplicationParameter.SIMULATION_MODE, String.valueOf(true));
                i--;
            }
            else if(args[i].equalsIgnoreCase("-tr")){
                parameters.put(ApplicationParameter.TEST_REPEATS, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-dlr")){
                parameters.put(ApplicationParameter.DEADLINE_STEP_SIZE, args[i + 1]);
            }
            else if(args[i].equalsIgnoreCase("-dl")){
                parameters.put(ApplicationParameter.START_DEADLINE, args[i + 1]);
            }
            else if (args[i].equalsIgnoreCase("-sg")){
                parameters.put(ApplicationParameter.STORE_RANDOMIZED_GRAPHS, String.valueOf(true));
                i--;
            }
        }

        return parameters;
    }
}
