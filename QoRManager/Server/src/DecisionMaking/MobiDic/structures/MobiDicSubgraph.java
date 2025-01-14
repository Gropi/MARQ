package DecisionMaking.MobiDic.structures;

import Structures.Graph.Edge;
import Structures.Graph.Graph;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MobiDicSubgraph {
    private IGraph _completeGraph;
    private List<IVertex> _vertices;
    private List<Edge> _Edges;

    public MobiDicSubgraph(Graph graph){
        _vertices = new ArrayList<>();
        _completeGraph = graph;
    }

    public MobiDicSubgraph(IGraph graph, List<IVertex> vertices){
        _vertices = vertices;
        _completeGraph = graph;
    }

    /** Method to determine whether given vertex is child of any of the given vertices
     *
     * @param vertex the vertex to check
     * @param parentVertices the potential parents of the vertex
     * @return whether any potential parent vertex is the parent of the given vertex
     */
    private boolean isChildOfAny(IGraph graph, IVertex vertex, List<IVertex> parentVertices){
        for(var potentialParent : parentVertices) {
            var edges = graph.getOutgoingEdges(potentialParent);
            for(var edge : edges) {
                if(edge.getDestination() == vertex)
                    return true;
            }
        }

        return false;
    }

    /** Method to get all vertices which are reachable within the subgraph starting from a given vertex
     *
     * @param startVertex the vertex to start from
     * @param reachableVertices the reachable vertices
     */
    private void getReachableVertices(IGraph graph, IVertex startVertex, ArrayList<IVertex> reachableVertices){
        if(!_vertices.contains(startVertex))
            return;
        reachableVertices.add(startVertex);
        var edges = graph.getOutgoingEdges(startVertex);

        var children = new ArrayList<>(edges.stream().filter(edge -> edge.getSource() == startVertex).map(edge -> edge.getDestination()).toList());

        for(var v : children)
            getReachableVertices(graph, v, reachableVertices);
    }

    /** Method to filter a given stage for nodes of a certain task
     *
     * @param taskNumber task to filter
     * @param stage stage to filter
     */
    public void filterTaskStage(int taskNumber, int stage){
        _vertices = new ArrayList<>(_vertices.stream().filter(vertex -> vertex.getApplicationIndex() == taskNumber || vertex.getStage() != stage).toList());
    }

    /** Filter the nodes of a stage in such a way that only vertices reachable from nodes
     * of lower stages of the subgraph exsist in the subgraph
     *
     * @param stage The stage to be filtered
     */
    public void filterChildren(IGraph graph, int stage){
        var parentVertices = new ArrayList<>(_vertices.stream().filter(vertex -> vertex.getStage() <= stage).toList());

        _vertices = new ArrayList<>(_vertices.stream().filter(vertex -> isChildOfAny(graph, vertex, parentVertices)||vertex.getStage() != stage).toList());
    }

    /** Method to determine whether every vertex in the subgraph is reachable from
     * the given starting vertex by only processing through vertices of the MobiDicSubgraph
     *
     * @param vertex the vertex to start from
     * @return whether every vertex in the subgraph is reachable
     */
    public boolean reachableFrom(IGraph graph, IVertex vertex){
        boolean[] nodeReachable = new boolean[_vertices.size()];
        boolean reachable = true;

        var reachableVertices = new ArrayList<IVertex>();
        getReachableVertices(graph, vertex, reachableVertices);

        for(int i = 0; i < nodeReachable.length; i++)
            nodeReachable[i] = reachableVertices.contains(_vertices.get(i));

        for(var b : nodeReachable)
            reachable = reachable && b;

        return reachable;
    }

    /** Method to clone the MobiDicSubgraph at hand
     *
     * @return a clone of the MobiDicSubgraph at hand
     */
    public MobiDicSubgraph clone(){
        return new MobiDicSubgraph( _completeGraph, getVertices());
    }

    public List<IVertex> getVertices(){
        return new ArrayList<>(_vertices);
    }

    /** Method to generate a graph from the MobiDicSubgraph at hand which only consists of vertices the subgraphs contains
     * and edges between thoose vertices
     *
     * @return the graph of the given MobiDicSubgraph
     */
    public Graph getGraphFromSubgraph(){
        var newStartVertex = _completeGraph.getStart().clone();
        var graph = new Graph(UUID.randomUUID(), newStartVertex, _completeGraph.isDirected(), _completeGraph.getGraphLocation());

        var potentialNewVertices = _completeGraph.getAllVertices();
        var potentialNewEdges = _completeGraph.getAllEdges();

        var subgraphVertexIds = _vertices.stream().map(x -> x.getId()).toList();

        var ids = new ArrayList<UUID>();
        ids.add(newStartVertex.getId());

        for(var vertex : potentialNewVertices) {
            if(newStartVertex.getId().equals(vertex.getId()))
                continue;
            if(subgraphVertexIds.contains(vertex.getId())) {
                graph.addVertex(vertex.clone());
                ids.add(vertex.getId());
            }
        }

        for(var edge : potentialNewEdges) {
            var sourceID = edge.getSource().getId();
            var destinationID = edge.getDestination().getId();

            if(ids.contains(sourceID) && ids.contains(destinationID)) {
                var source = graph.getVertexById(sourceID);
                var destination = graph.getVertexById(destinationID);
                var newEdge = edge.clone(source, destination);
                graph.addEdge(newEdge);
            }
        }
        
        return graph;
    }

    /** Method to generate a graph from the MobiDicSubgraph at hand which only consists of vertices the subgraphs contains
     * and edges between thoose vertices
     *
     * @return the graph of the given MobiDicSubgraph
     */
    public Graph getGraphFromSubgraphWithSpanning(){
        var clonedVertices = new ArrayList<IVertex>();

        var newStartVertex = _completeGraph.getStart().clone(clonedVertices);
        var graph = new Graph(UUID.randomUUID(), newStartVertex, _completeGraph.isDirected(), _completeGraph.getGraphLocation());

        graph.spanGraph(_completeGraph);

        for(var vertex : graph.getAllVertices()){
            if(_vertices.contains(vertex))
                continue;

            graph.removeVertex(vertex);
        }

        return graph;
    }
}
