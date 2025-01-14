package ApplicationSupport.impl;

import ApplicationSupport.IApplicationParameter;
import IO.impl.AccessDrive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApplicationParameter implements IApplicationParameter {
    private static final Logger _Logger = LogManager.getLogger("executionLog");
    private List<String> _GraphLocations;
    private int _TestRepeats = 0;
    private final boolean _Simulation;
    private int _InitialDeadline = 100000;
    private int _LastDeadline = -1;
    private int _DeadlineStepSize = -1;
    private final String _DecisionMakerToUse;
    private final String _TargetLocation;
    private int _MaxPictureCount;
    private int _PictureBatchSize;
    private final String _ContentLocation;
    private final String _ApplicationName;
    private boolean _StoreRandomizedGraphs;

    public ApplicationParameter(Map<String, String> testbedParameters) throws IOException {
        _DecisionMakerToUse = testbedParameters.get(DECISION_MAKER);

        _ContentLocation = testbedParameters.get(CONTENT_LOCATION);
        getGraphs(testbedParameters);

        if(testbedParameters.get(MAX_PICTURE_COUNT) != null)
            _MaxPictureCount = Integer.parseInt(testbedParameters.get(MAX_PICTURE_COUNT));
        _ApplicationName = testbedParameters.get(APPLICATION_NAME);
        if(testbedParameters.get(PICTURE_PER_CYCLE) != null)
            _PictureBatchSize =  Integer.parseInt(testbedParameters.get(PICTURE_PER_CYCLE));
        if (testbedParameters.get(TEST_REPEATS) != null)
            _TestRepeats = Integer.parseInt(testbedParameters.get(TEST_REPEATS));
        if (testbedParameters.get(START_DEADLINE) != null)
            _InitialDeadline = Integer.parseInt(testbedParameters.get(START_DEADLINE));
        if (testbedParameters.get(DEADLINE_STEP_SIZE) != null) {
            var stringToSplit = testbedParameters.get(DEADLINE_STEP_SIZE);
            var withSteps = stringToSplit.split(",");
            var SPLITTING_REGEX = "\\.\\.\\.";
            if (withSteps.length > 1) {
                _InitialDeadline = Integer.parseInt(withSteps[0]);
                var range = withSteps[1].split(SPLITTING_REGEX);
                _DeadlineStepSize = Integer.parseInt(range[0]);
                _LastDeadline = Integer.parseInt(range[1]);

            } else {
                var range = stringToSplit.split(SPLITTING_REGEX);
                _InitialDeadline = Integer.parseInt(range[0]);
                _LastDeadline = Integer.parseInt(range[1]);
            }
        }
        _StoreRandomizedGraphs = testbedParameters.containsKey(STORE_RANDOMIZED_GRAPHS) && Boolean.parseBoolean(testbedParameters.get(STORE_RANDOMIZED_GRAPHS));
        _TargetLocation = testbedParameters.get(TARGET_LOCATION);
        _Simulation = testbedParameters.containsKey(SIMULATION_MODE);
    }

    public String getApplicationName() {
        return _ApplicationName;
    }

    public List<String> getGraphLocations() {
        return _GraphLocations;
    }

    public int getTestRepeats() {
        return _TestRepeats;
    }

    public boolean getSimulationMode() {
        return _Simulation;
    }

    public int getInitialDeadline() {
        return _InitialDeadline;
    }

    public int getDeadlineStepSize() {
        return _DeadlineStepSize;
    }

    public int getFinalDeadline() {
        return _LastDeadline;
    }

    public String getDecisionMakerToUse() {
        return _DecisionMakerToUse;
    }

    public String getTargetLocation() {
        return _TargetLocation;
    }

    public int getPictureBatchSize() {
        return _PictureBatchSize;
    }

    public int getMaxPictureCount() {
        return _MaxPictureCount;
    }

    public String getContentLocation() {
        return _ContentLocation;
    }

    public boolean storeRandomizedGraphs() {
        return _StoreRandomizedGraphs;
    }

    private void getGraphs(Map<String, String> testbedParameters) throws IOException {
        if (testbedParameters.containsKey(GRAPH_LOCATION)) {
            _GraphLocations = new LinkedList<>();
            _GraphLocations.add(testbedParameters.get(GRAPH_LOCATION));
        } else {
            var loadFiles = new AccessDrive();
            _Logger.info("Start loading data from disk.");
            _GraphLocations = loadFiles.listFilesUsingFilesList(testbedParameters.get(GRAPH_FOLDER));
            _Logger.info("Loading data from disk completed.");
        }
    }
}
