package ApplicationSupport;

import java.util.List;

public interface IApplicationParameter {
    String DECISION_MAKER = "decisionMaker";
    String MAX_PICTURE_COUNT = "maxPictureCount";
    String START_DEADLINE = "deadline";
    String TEST_REPEATS = "deadline";
    String CONTENT_LOCATION = "contentLocation";
    String APPLICATION_NAME = "applicationName";
    String GRAPH_LOCATION = "graphLocation";
    String GRAPH_FOLDER = "graphFolder";
    String SIMULATION_MODE = "simulation";
    String STORE_RANDOMIZED_GRAPHS = "storeGraphs";
    String DEADLINE_STEP_SIZE = "deadlineStepSize";
    String PICTURE_PER_CYCLE = "picturesPerCycle";
    String TARGET_LOCATION = "targetLocation";

    String getApplicationName();

    List<String> getGraphLocations();

    int getTestRepeats();

    boolean getSimulationMode();

    int getInitialDeadline();

    int getDeadlineStepSize();

    int getFinalDeadline();

    String getDecisionMakerToUse();

    String getTargetLocation();

    int getPictureBatchSize();

    int getMaxPictureCount();

    String getContentLocation();

    boolean storeRandomizedGraphs();
}
