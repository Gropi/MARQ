package Structures.Graph;

import Structures.Graph.SerialParallel.SerialParallelPart;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;

import java.util.*;

public class Graph implements IGraph {
    private final UUID _GraphID;
    private final String _GraphLocation;
    private final Map<Integer, List<IVertex>> serialParts;
    private final Map<Integer, List<IVertex>> parallelParts;
    private final List<IVertex> _Vertices;
    private final List<Edge> _Edges;
    private IVertex _StartVertex;
    private IVertex _EndVertex;
    private final boolean _IsDirected;

    public Graph(UUID id, IVertex startVertex, String graphLocation) {
        this(id, startVertex, true, graphLocation);
    }

    public Graph(UUID id, IVertex startVertex, boolean isDirected, String graphLocation) {
        if (startVertex == null)
            throw new NullPointerException("Start vertex must be not null!");
        _GraphLocation = graphLocation;
        _GraphID = id;
        _IsDirected = isDirected;
        _StartVertex = startVertex;
        _Vertices = new LinkedList<>();
        _Edges = new LinkedList<>();
        serialParts = new HashMap<>();
        parallelParts = new HashMap<>();
        addVertex(startVertex);
    }

    @Override public UUID getGraphID() {
        return _GraphID;
    }

    @Override
    public IVertex getStart() {
        return _StartVertex;
    }

    @Override
    public IVertex getEnd() {
        return _EndVertex;
    }

    public IVertex getVertexById(UUID id) {
        return _Vertices.stream().filter(x -> x.getId().equals(id)).findAny().orElse(null);
    }

    @Override
    public String getGraphLocation() {
        return _GraphLocation;
    }

    @Override
    public IVertex getVertexByIdentifier(String identifier) {
        return _Vertices.stream().filter(x -> x.getLabel().equals(identifier)).findAny().orElse(null);
    }

    @Override
    public void addEdge(Edge edge) {
        _Edges.add(edge);
        setNewEnd();

        // We want to check, if an end vertex has more than one incoming application index
        handleDecisionMakingVertex(edge.getDestination());
    }

    @Override
    public void addEdge(UUID startID, UUID endID, UUID edgeCount) {
        var startVertex = getVertexById(startID);
        var endVertex = getVertexById(endID);
        var newEdge = new Edge(startVertex, endVertex, edgeCount);
        _Edges.add(newEdge);
        setNewEnd();

        // We want to check, if an end vertex has more than one incoming application index
        handleDecisionMakingVertex(endVertex);
    }

    /**
     * This method allows us to figure out if a vertex has multiple incoming edges with different
     * application id's as source of the edge. If this is the case, this vertex is an "end" vertex
     * and has to make some decisions later on. We will set the flag on the vertex if so.
     * @param vertexToCheck The vertex you want to check for.
     */
    private void handleDecisionMakingVertex(IVertex vertexToCheck) {
        var edges = getEdges(false, true, vertexToCheck);
        if (!edges.isEmpty()) {
            var id = edges.get(0).getSource().getApplicationIndex();
            for (int i = 1; i < edges.size(); i++) {
                if (edges.get(i).getSource().getApplicationIndex() != id) {
                    vertexToCheck.setDecisionMakingVertex(true);
                    return;
                }
            }
        }
    }

    private void setNewEnd() {
        var vertices = getAllVertices();
        for (var edge : _Edges) {
            vertices.remove(edge.getSource());
        }
        if (!vertices.isEmpty())
            _EndVertex = vertices.get(0);
    }

    private void setNewStart() {
        var vertices = getAllVertices();
        for (var edge : _Edges) {
            vertices.remove(edge.getDestination());
        }
        if (!vertices.isEmpty())
            _StartVertex = vertices.get(0);
    }

    @Override
    public List<Edge> getEdgesBetweenVertices(IVertex outgoing, IVertex ingoing) {
        var outgoingEdges = getOutgoingEdges(outgoing);
        var edges = new LinkedList<Edge>();
        for (var edge : outgoingEdges) {
            if (edge.getDestination().equals(ingoing))
                edges.add(edge);
        }
        return edges;
    }

    @Override
    public List<Edge> getEdgesBetweenVertices(UUID outgoingVertexID, UUID ingoingVertexID) {
        var outgoing = getVertexById(outgoingVertexID);
        var ingoing = getVertexById(ingoingVertexID);
        return getEdgesBetweenVertices(outgoing, ingoing);
    }

    @Override
    public List<Edge> getOutgoingEdges(IVertex currentVertex) {
        return getEdges(true, false, currentVertex);
    }

    @Override
    public boolean isEndVertex(IVertex vertex) {
        return getOutgoingEdges(vertex).isEmpty();
    }

    @Override
    public List<Edge> getAllEdges(IVertex currentVertex) {
        return getEdges(true, true, currentVertex);
    }

    public List<Edge> getAllEdges() {
        return _Edges;
    }

    private List<Edge> getEdges(boolean outgoing, boolean ingoing, IVertex currentVertex) {
        var listOfEdges = new LinkedList<Edge>();
        for (var edge : _Edges) {
            if (outgoing && edge.getSource().equals(currentVertex)) {
                listOfEdges.add(edge);
            } else if (ingoing && edge.getDestination().equals(currentVertex)) {
                listOfEdges.add(edge);
            }
        }
        return listOfEdges;
    }

    public void addVertex(IVertex vertex) {
        if (!_Vertices.contains(vertex)) {
            int stage = vertex.getStage();
            if (serialParts.get(stage) == null) {
                serialParts.computeIfAbsent(stage, k -> new ArrayList<>()).add(vertex);
            } else {
                parallelParts.computeIfAbsent(stage, k -> new ArrayList<>()).add(vertex);
            }
            _Vertices.add(vertex);
        }
    }

    public void removeVertex(IVertex vertex) {
        if (_Vertices.contains(vertex)) {
            var edges = getAllEdges(vertex);
            for (var edge: edges) {
                _Edges.remove(edge);
            }
            _Vertices.remove(vertex);
            if (!removeFromMap(serialParts, vertex)) {
                removeFromMap(parallelParts, vertex);
            }

            if(vertex == _StartVertex) {
                setNewStart();
            }
        }
    }

    private boolean removeFromMap(Map<Integer, List<IVertex>> mapToInspect, IVertex vertexToRemove) {
        for (Map.Entry<Integer, List<IVertex>> entry : mapToInspect.entrySet()) {
            if (entry.getValue().contains(vertexToRemove)) {
                entry.getValue().remove(vertexToRemove);
                return true;
            }
        }
        return false;
    }

    public List<IVertex> getAllVertices() {
        return new LinkedList<>(_Vertices);
    }

    public List<IVertex> getAllNextVertices(IVertex currentVertex) {
        var vertexToSearch = new LinkedList<IVertex>();
        var edges = getOutgoingEdges(currentVertex);
        if (edges.isEmpty()) {
            vertexToSearch.add(currentVertex);
        } else {
            for (var edge : edges) {
                vertexToSearch.add(edge.getDestination());
            }
        }
        return vertexToSearch;
    }

    public void uniteWithGraph(Graph graph){
        var verticesToAdd = graph.getAllVertices();

        for(var vertex : verticesToAdd) {
            if(getVertexByIdentifier(vertex.getLabel()) == null) {
                addVertex(vertex.clone());
            }
        }

        for(var vertex : _Vertices) {
            if(verticesToAdd.stream().noneMatch(x -> x.getId().equals(vertex.getId())))
                continue;

            for(var v : verticesToAdd){
                if(!v.getLabel().equals(vertex.getLabel()))
                    continue;

                var newEdges = graph.getOutgoingEdges(v);
                var existingEdges = getOutgoingEdges(vertex);
                for(var newEdge : newEdges){
                    var contains = false;

                    var startID = newEdge.getSource().getId();
                    var endID = newEdge.getDestination().getId();

                    for(var edge : existingEdges) {
                        if (edge.getSource().getId().equals(startID) && edge.getDestination().getId().equals(endID)) {
                            contains = true;
                            break;
                        }
                    }

                    var start = getVertexById(startID);
                    var end = getVertexById(endID);

                    if(!contains && start != null && end != null)
                        addEdge(start.getId(), end.getId(), newEdge.id());
                }
            }
        }
    }

    public void recalculateGraphStages() {
        _StartVertex.setStage(0);
        setStages(_StartVertex);
    }

    private void setStages(IVertex currentVertex){
        var edges = getOutgoingEdges(currentVertex);

        for(var edge : edges){
            if(edge.getDestination().getStage() < currentVertex.getStage() + 1) {
                edge.getDestination().setStage(currentVertex.getStage() + 1);
            }
        }

        for(var edge : edges){
            setStages(edge.getDestination());
        }
    }

    public void spanGraph(IGraph fromGraph) {
        spanEdgesFromVertices(fromGraph, _StartVertex);
    }

    private void spanGraphFromVertex(IGraph fromGraph, IVertex vertex){
        if(_Vertices.contains(vertex))
            return;

        addVertex(vertex.clone());
        spanEdgesFromVertices(fromGraph, vertex);
    }

    private void spanEdgesFromVertices(IGraph fromGraph, IVertex vertex) {
        var edges = fromGraph.getOutgoingEdges(vertex);

        for(var e : edges) {
            spanGraphFromVertex(fromGraph, e.getDestination());
            var source = _Vertices.stream().filter(x -> x.getId().equals(e.getSource().getId())).findFirst().orElse(null);
            var destination = _Vertices.stream().filter(x -> x.getId().equals(e.getDestination().getId())).findFirst().orElse(null);
            var newEdge = new Edge(source, destination, UUID.randomUUID());
            _Edges.add(newEdge);
        }
    }

    @Override
    public boolean isDirected(){
        return _IsDirected;
    }

    @Override
    public List<List<Edge>> findAllPaths() {
        return findAllPaths(_StartVertex, _EndVertex);
    }

    public List<List<Edge>> findAllPaths(IVertex startVertex, IVertex endVertex) {
        var allPaths = new ArrayList<List<Edge>>();
        var currentPath = new ArrayList<Edge>();
        var visitedVertices = new HashSet<UUID>();
        findAllPathsRecursive(startVertex, endVertex, currentPath, visitedVertices, allPaths);
        return allPaths;
    }

    private void findAllPathsRecursive(
            IVertex currentVertex, IVertex endVertex,
            List<Edge> currentPath, Set<UUID> visitedVertices,
            List<List<Edge>> allPaths) {
        visitedVertices.add(currentVertex.getId());

        if (currentVertex.equals(endVertex)) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            for (Edge edge : _Edges) {
                if (edge.getSource().equals(currentVertex) && !visitedVertices.contains(edge.getDestination().getId())) {
                    var destination = edge.getDestination();
                    if (destination.getApplicationIndex() == currentVertex.getApplicationIndex()) {
                        // Combine vertices with the same application ID but different approximation IDs into an alternative path
                        currentPath.add(edge);
                        findAllPathsRecursive(destination, endVertex, currentPath, visitedVertices, allPaths);
                        currentPath.remove(currentPath.size() - 1);
                    } else {
                        // Add vertices with different application IDs as separate paths
                        var newPath = new ArrayList<>(currentPath);
                        newPath.add(edge);
                        findAllPathsRecursive(destination, endVertex, newPath, visitedVertices, allPaths);
                    }
                }
            }
        }

        visitedVertices.remove(currentVertex.getId());
    }

    public List<SerialParallelPart> getSerialParallelParts() {
        var serialParallelParts = new ArrayList<SerialParallelPart>();

        for (var entry : serialParts.entrySet()) {
            int stage = entry.getKey();
            var serialVertices = entry.getValue();
            var parallelVertices = parallelParts.getOrDefault(stage, new ArrayList<>());

            var part = new SerialParallelPart(serialVertices, parallelVertices);
            serialParallelParts.add(part);
        }

        return serialParallelParts;
    }
}
