package Structures;

import Structures.Graph.Edge;
import Structures.Graph.interfaces.IVertex;

import java.util.List;
import java.util.UUID;

public interface IGraph {
    UUID getGraphID();

    IVertex getStart();

    IVertex getEnd();

    IVertex getVertexByIdentifier(String identifier);

    IVertex getVertexById(UUID id);

    String getGraphLocation();

    List<IVertex> getAllVertices();

    void addVertex(IVertex vertex);

    void addEdge(Edge edge);

    /**
     * Allows you to add a new edge between to vertices. The edge is allways directed.
     * @param startID The id of the starting vertex.
     * @param endID The id of the ending vertex.
     * @param edgeCount The index of the edge, for easier finding
     */
    void addEdge(UUID startID, UUID endID, UUID edgeCount);

    List<Edge> getEdgesBetweenVertices(IVertex outgoing, IVertex ingoing);

    List<Edge> getEdgesBetweenVertices(UUID outgoingVertexID, UUID ingoingVertexID);

    /**
     * Returns all edges of one to and from one vertex.
     * @param currentVertex The vertex you are looking for
     * @return The list of all edges connected to the vertex.
     */
    List<Edge> getAllEdges(IVertex currentVertex);

    /**
     * Get all edges in total.
     * @return A list of all edges.
     */
    List<Edge> getAllEdges();

    /**
     * Returns all outgoing edges between to vertices, where the current vertex is the starting vertex.
     * @param currentVertex The vertex you are looking for outgoing edges.
     * @return The list of edges.
     */
    List<Edge> getOutgoingEdges(IVertex currentVertex);

    /**
     * Checks all edges for the given vertex. If the vertex has an outgoing edge, this method returns false, otherwise true.
     * @param vertex The vertex we are looking for.
     * @return If the vertex has an outgoing edge, this method returns false, otherwise true.
     */
    boolean isEndVertex(IVertex vertex);

    /**
     * Returns the information, whether the graph is directed (true) or not (false).
     * @return True if directed, false if not.
     */
    boolean isDirected();

    List<List<Edge>> findAllPaths();

    List<List<Edge>> findAllPaths(IVertex startVertex, IVertex endVertex);

    /**
     * This method calculates the new stages for every vertex within the current graph.
     */
    void recalculateGraphStages();
}
