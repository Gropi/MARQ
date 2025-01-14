package DecisionMaking.EdgeDiC;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import DecisionMaking.IDecisionMaking;
import GraphPathSearch.Dijkstra;
import GraphPathSearch.IShortestPathAlgorithm;
import Measurement.MicroserviceChainMeasurement;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Edge;
import Structures.Graph.SimplifiedGraph.GraphSimplifier;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;
import Structures.Path.ShortestPath;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class EdgeDiCManager implements IDecisionMaking {
    private final Logger _Logger;
    private final IGraph _CurrentGraph;
    private final List<IGraph> _Subgraphs;
    private final List<Pair<String, Boolean>> _Conditions;
    private String m_DijkstraMode = "";
    private final Pair<String, Boolean> _MostImportantCriteria;
    private final Map<String, Number> _Constraints;
    private final Map<String, Number> _InitialConstraints;
    private Double[] _Weights;
    private final NormalizationMode _NormalizingMode;

    public EdgeDiCManager(IGraph graph, List<Pair<String, Boolean>> conditions, Map<String, Number> constraints, Double[] weights, NormalizationMode normalizingMode,
                          Pair<String, Boolean> mostImportantCriteria, Logger logger, String dijkstraMode) {
        m_DijkstraMode = dijkstraMode;
        _Logger = logger;
        _CurrentGraph = graph;
        _Conditions = conditions;

        _InitialConstraints = new HashMap<>();
        for(var constraint : constraints.keySet()) {
            _InitialConstraints.put(constraint, constraints.get(constraint));
        }

        _Constraints = constraints;
        _MostImportantCriteria = mostImportantCriteria;
        _Weights = weights;
        _NormalizingMode = normalizingMode;

        var simplifier = new GraphSimplifier();
        List<List<Integer>> _SubgraphIndices = simplifier.getSubgraphIndicesIterative(_CurrentGraph);
        _Subgraphs = simplifier.getSubgraphs(_CurrentGraph, _SubgraphIndices);
    }

    public EdgeDiCManager(IGraph graph, List<Pair<String, Boolean>> conditions, Logger logger, String dijkstraMode) {
        this(graph, conditions, new HashMap<>(), null, NormalizationMode.LINEAR, conditions.get(0), logger, dijkstraMode);
        _Weights = new Double[conditions.size()];
        Arrays.fill(_Weights, 1d);
    }

    @Override
    public UUID getGraphID() {
        return _CurrentGraph.getGraphID();
    }

    @Override
    public String getGraphLocation() {
        return _CurrentGraph.getGraphLocation();
    }

    @Override
    public List<IGraph> getSubgraphs() {
        return _Subgraphs;
    }

    @Override
    public boolean isEndVertex(IVertex vertex) {
        return _CurrentGraph.isEndVertex(vertex);
    }

    @Override
    public boolean isEndVertex(String uuid) {
        return _CurrentGraph.getEnd().getId().equals(UUID.fromString(uuid));
    }

    @Override
    public IVertex getVertexByID(String uuid) {
        return _CurrentGraph.getVertexById(UUID.fromString(uuid));
    }

    @Override
    public IVertex getVertexByIP(String successorIP, int successorPort) {
        var vertices = _CurrentGraph.getAllVertices();
        for (var vertex : vertices) {
            var microservice = vertex.getMicroservice();
            if(microservice == null)
                continue;
            if (microservice.getAddress().equalsIgnoreCase(successorIP) && microservice.getPort("inference") == successorPort) {
                return vertex;
            }
        }
        return null;
    }

    @Override
    public IVertex getVertexMicroserviceID(String uuid) {
        var vertices = _CurrentGraph.getAllVertices();

        for(var vertex : vertices) {
            var service = vertex.getMicroservice();
            if(service == null)
                continue;
            if(service.ID().equalsIgnoreCase(uuid))
                return vertex;
        }
        return null;
    }

    @Override
    public void cleanUp() {
        for (var vertex : _CurrentGraph.getAllVertices()) {
            vertex.unbindMicroservice();
        }
    }

    @Override
    public IVertex getEndVertex() {
        return _CurrentGraph.getEnd();
    }

    @Override
    public Map<IVertex, List<Integer>> getNextVerticesFrom(IVertex vertex, Long startOfCalculation) {
        if (vertex == null) {
            var followingVertices = new  HashMap<IVertex, List<Integer>>();
            var sgList = new ArrayList<Integer>();
            for(int i = 0; i < _Subgraphs.size(); i++) {
                sgList.add(i);
            }

            followingVertices.put(_CurrentGraph.getStart(), sgList);
            return followingVertices;
        }
        return getEstimatedVertices(vertex, startOfCalculation);
    }

    @Override
    public List<Edge> getAllEdges() {
        return _CurrentGraph.getAllEdges();
    }

    @Override
    public List<Edge> getAllEdges(IVertex from) {
        return _CurrentGraph.getAllEdges(from);
    }

    @Override
    public Map<Edge, List<Integer>> getInitialSelection(int deadline, MicroserviceChainMeasurement measurement) {
        var result = new HashMap<Edge, List<Integer>>();
        for(int i = 0; i < _Subgraphs.size(); i++) {
            var sg = _Subgraphs.get(i);
            var list = runChosenDijkstraVariation(measurement, _CurrentGraph.getStart(), sg, false);

            if(list.isEmpty()){
                return new HashMap<>();
            }

            for(var path : list){
                var edge = path.getEdge();
                if(!result.containsKey(edge)) {
                    var indexList = new ArrayList<Integer>();
                    indexList.add(i);
                    result.put(edge, indexList);
                }
                result.get(edge).add(i);
            }

        }
        return result;
    }

    @Override
    public List<Edge> getOutgoingEdge(IVertex vertex) {
        return _CurrentGraph.getOutgoingEdges(vertex);
    }

    @Override
    public List<IVertex> getAllVertices() {
        return _CurrentGraph.getAllVertices();
    }

    @Override
    public void updateRemainingVertices(List<IVertex> vertices, List<IVertex> updatedVertices, String value, int newValue, String identifier){
        for(var vertex : vertices) {
            updateRemainingVertices(vertex, updatedVertices, value, newValue, identifier);
        }
    }

    public void updateRemainingVertices(IVertex vertex, List<IVertex> updatedVertices, String value, int newValue, String identifier){
        if(vertex == null)
            return;

        _Logger.info("UPDATING BECAUSE OF MEASUREMENT");

        updatedVertices.add(vertex);
        var edges = _CurrentGraph.getOutgoingEdges(vertex).stream().filter(edge -> !updatedVertices.contains(edge.getDestination())).toList();

        for(var edge : edges){
            updateRemainingVertices(edge.getDestination(), updatedVertices, value, newValue, identifier);
        }

        if(vertex.getMicroservice().ID().equals(identifier)) {
            vertex.updateWeight(value, newValue);
        }
    }

    @Override
    public void updateRemainingEdges(List<IVertex> vertices, List<IVertex> processedVertices, String value, int newValue, String identifier, String targetIdentifier) {
        for (var vertex : vertices) {
            updateRemainingEdges(vertex, processedVertices, value, newValue, identifier, targetIdentifier);
        }
    }
    public void updateRemainingEdges(IVertex vertex, List<IVertex> processedVertices, String value, int newValue, String identifier, String targetIdentifier) {

        processedVertices.add(vertex);
        var edges = _CurrentGraph.getOutgoingEdges(vertex).stream().filter(edge -> !processedVertices.contains(edge.getDestination())).toList();

        for(var edge : edges) {
            if(identifier.equals(edge.getSource().getMicroservice().ID()) && edge.getDestination().getMicroservice().ID().equals(targetIdentifier)) {
                edge.updateWeight(value, newValue);
            }

            updateRemainingEdges(edge.getDestination(), processedVertices, value, newValue, identifier, targetIdentifier);
        }
    }

    /**
     * Returns the next vertex of the shortest path starting on current vertex with current conditions.
     * @return The vertex of that is the next
     */
    private Map<IVertex, List<Integer>> getEstimatedVertices(IVertex from, Long startOfCalculation){
        var estimatedVertices = new HashMap<IVertex, List<Integer>>();

        if(_Constraints.containsKey(MeasurableValues.TIME.name())) {
            var newDeadline =(int)(_InitialConstraints.get(MeasurableValues.TIME.name()).intValue() - (System.currentTimeMillis() - startOfCalculation));
            _Constraints.put(MeasurableValues.TIME.name(), newDeadline);
        }

        for(int sgindex = 0; sgindex < _Subgraphs.size(); sgindex++) {
            var subgraph = _Subgraphs.get(sgindex);
            if(!subgraph.getAllVertices().contains(from))
                continue;
            var list = runChosenDijkstraVariation(null, from, subgraph, true);
            if (list == null) {
                _Logger.fatal("WTF?");
                return new HashMap<>();
            }
            if (list.isEmpty())
                return new HashMap<>();

            var vertexToAdd = list.get(0).getEdge().getDestination();
            if(!estimatedVertices.containsKey(vertexToAdd)) {
                var sgList = new ArrayList<Integer>();
                sgList.add(sgindex);
                estimatedVertices.put(vertexToAdd, sgList);
            } else {
                estimatedVertices.get(vertexToAdd).add(sgindex);
            }
        }

        return !estimatedVertices.isEmpty() ? estimatedVertices : new HashMap<>();
    }

    private List<ShortestPath> runChosenDijkstraVariation(MicroserviceChainMeasurement measurement, IVertex from, IGraph graph, boolean mustFindSolutionAtAnyCost) {
        List<ShortestPath> result;

        if(m_DijkstraMode.equalsIgnoreCase(IShortestPathAlgorithm.MODE_TOPSIS)) {
            result = new Dijkstra(_Logger).runTopsisVariation(measurement, from, graph, _Conditions, _Constraints, _Weights, _NormalizingMode, mustFindSolutionAtAnyCost);
        } else if(m_DijkstraMode.equalsIgnoreCase(IShortestPathAlgorithm.MODE_ECONSTRAINT)) {
            result = new Dijkstra(_Logger).runEConstraintVariation(measurement, from, graph, _Conditions, _Constraints, _MostImportantCriteria, mustFindSolutionAtAnyCost);
        } else {
            result = new Dijkstra(_Logger).run(measurement, from, graph, _Conditions, _Constraints, mustFindSolutionAtAnyCost);
        }

        return result;
    }
}
