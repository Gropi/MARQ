package DecisionMaking.MobiDic;

import DecisionMaking.IDecisionMaking;
import DecisionMaking.MobiDic.algorithms.MobiDiC;
import Measurement.MicroserviceChainMeasurement;
import Structures.Graph.Edge;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;

import java.util.*;

public class MobiDiCManager implements IDecisionMaking {
    private final MobiDiC _MobiDic;
    private final String _ComparatorToUse;
    private final IGraph _InitialGraph;
    private IGraph _UsedGraph;

    public MobiDiCManager(IGraph graph) {
        this(graph, "costComparator");
    }
    public MobiDiCManager(IGraph graph, String comparatorToUse) {
        _InitialGraph = graph;
        _MobiDic = new MobiDiC();
        _ComparatorToUse = comparatorToUse;
    }

    @Override
    public UUID getGraphID() {
        return _InitialGraph.getGraphID();
    }

    @Override
    public String getGraphLocation() {
        return _UsedGraph.getGraphLocation();
    }

    @Override
    public List<IGraph> getSubgraphs() {
        return _MobiDic.constructSubgraphsWithCleanup(_UsedGraph).stream().map(x -> (IGraph) x.getGraphFromSubgraph()).toList();
    }

    @Override
    public boolean isEndVertex(IVertex vertex) {
        if(_UsedGraph == null)
            return false;
        return _UsedGraph.isEndVertex(vertex);
    }

    @Override
    public boolean isEndVertex(String uuid) {
        return _UsedGraph.getEnd().getId().equals(UUID.fromString(uuid));
    }

    @Override
    public Map<IVertex, List<Integer>> getNextVerticesFrom(IVertex vertex, Long startOfCalculation) {
        if (_UsedGraph != null) {
            var followingVertices = new ArrayList<IVertex>();
            var returnMap = new HashMap<IVertex, List<Integer>>();

            var edges = _UsedGraph.getOutgoingEdges(vertex);
            if (edges != null && !edges.isEmpty()) {
                for (var edge : edges) {
                    followingVertices.add(edge.getDestination());
                }
            } else {
                followingVertices.add(_UsedGraph.getStart());
            }

            for(var follower : followingVertices) {
                returnMap.put(follower, null);
            }
            return returnMap;
        }
        return null;
    }

    @Override
    public List<Edge> getAllEdges() {
        return _UsedGraph.getAllEdges();
    }

    @Override
    public List<Edge> getAllEdges(IVertex from) {
        return _UsedGraph.getAllEdges(from);
    }

    @Override
    public Map<Edge, List<Integer>> getInitialSelection(int deadline, MicroserviceChainMeasurement measurement) {
        _UsedGraph = _MobiDic.getApproximateWorkflow(measurement, _InitialGraph, deadline, _ComparatorToUse, false);
        if (_UsedGraph == null)
            return new HashMap<>();
        else {
            var result = new HashMap<Edge, List<Integer>>();

            var subgraphs = _MobiDic.constructSubgraphsWithCleanup(_UsedGraph);
            for(var edge : _UsedGraph.getAllEdges()) {
                var sgIndices = new LinkedList<Integer>();
                for(int i = 0; i < subgraphs.size(); i++) {
                    var sg = subgraphs.get(i);
                    var vertices = sg.getVertices();
                    if(vertices.contains(edge.getSource()) && vertices.contains(edge.getDestination())) {
                        sgIndices.add(i);
                    }
                }
                result.put(edge, sgIndices);
            }
            return result;
        }
    }

    @Override
    public List<IVertex> getAllVertices() {
        return _UsedGraph.getAllVertices();
    }

    @Override
    public List<Edge> getOutgoingEdge(IVertex vertex) {
        if(_UsedGraph == null)
            return new ArrayList<>();

        return _UsedGraph.getOutgoingEdges(vertex);
    }

    @Override
    public void updateRemainingVertices(List<IVertex> vertices, List<IVertex> updatedVertices, String value, int newValue, String identifier){
        for(var vertex : vertices) {
            updateRemainingVertices(vertex, updatedVertices, value, newValue, identifier);
        }
    }

    private void updateRemainingVertices(IVertex vertex, List<IVertex> updatedVertices, String value, int newValue, String identifier){
        // Not applicable in MobiDiC
    }

    @Override
    public void updateRemainingEdges(List<IVertex> vertices, List<IVertex> processedVertices, String value, int newValue, String identifier, String targetIdentifier) {
        for(var vertex : vertices) {
            updateRemainingEdges(vertex, processedVertices, value, newValue, identifier, targetIdentifier);
        }
    }
    private void updateRemainingEdges(IVertex vertex, List<IVertex> processedVertices, String value, int newValue, String identifier, String targetIdentifier) {
        // Not applicable in MobiDiC
    }

    @Override
    public IVertex getVertexByID(String uuid) {
        return _UsedGraph.getVertexById(UUID.fromString(uuid));
    }

    @Override
    public IVertex getVertexByIP(String successorIP, int successorPort) {
        if(_UsedGraph == null)
            return null;

        for (var vertex : _UsedGraph.getAllVertices()) {
            var microservice = vertex.getMicroservice();
            if (microservice != null && microservice.getAddress().equalsIgnoreCase(successorIP) && microservice.getPort("inference") == successorPort) {
                return vertex;
            }
        }
        return null;
    }

    @Override
    public IVertex getVertexMicroserviceID(String uuid) {
        if(_UsedGraph == null)
            return null;

        for (var vertex: _UsedGraph.getAllVertices()) {
            if (vertex.getMicroservice() != null && vertex.getMicroservice().ID().equals(uuid))
                return vertex;
        }
        return null;
    }

    @Override
    public void cleanUp() {
        for (var vertex : _InitialGraph.getAllVertices()) {
            vertex.unbindMicroservice();
        }
        _UsedGraph = null;
    }

    @Override
    public IVertex getEndVertex() {
        return _UsedGraph.getEnd();
    }
}
