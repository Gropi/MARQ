package DecisionMaking;

import Measurement.MicroserviceChainMeasurement;
import Structures.Graph.Edge;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IDecisionMaking {

    UUID getGraphID();
    String getGraphLocation();
    List<IGraph> getSubgraphs();
    boolean isEndVertex(IVertex vertex);

    boolean isEndVertex(String uuid);

    Map<IVertex, List<Integer>> getNextVerticesFrom(IVertex vertex, Long startOfCalculation);

    List<Edge> getAllEdges();

    List<Edge> getAllEdges(IVertex from);

    Map<Edge, List<Integer>> getInitialSelection(int deadline, MicroserviceChainMeasurement measurement);

    List<Edge> getOutgoingEdge(IVertex vertex);

    List<IVertex> getAllVertices();

    void updateRemainingVertices(List<IVertex> vertices, List<IVertex> updatedVertices, String value, int newValue, String identifier);

    void updateRemainingEdges(List<IVertex> vertices, List<IVertex> processedVertices, String value, int newValue, String identifier, String targetIdentifier);

    IVertex getVertexByID(String uuid);

    IVertex getVertexByIP(String successorIP, int successorPort);

    IVertex getVertexMicroserviceID(String uuid);

    void cleanUp();

    IVertex getEndVertex();
}
