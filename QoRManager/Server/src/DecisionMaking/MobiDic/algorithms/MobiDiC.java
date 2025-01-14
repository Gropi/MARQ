package DecisionMaking.MobiDic.algorithms;

import GraphPathSearch.Dijkstra;
import DecisionMaking.MobiDic.structures.MobiDicSubgraph;
import Measurement.MicroserviceChainMeasurement;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Graph;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MobiDiC {
    private final Logger m_Logger = LogManager.getLogger("measurementLog");

    /** Get the depth/the maximum stage of the graph which
     * contains all nodes starting at the startVertex.
     *
     * @param startVertex Vertex to start at
     * @return The maximum depth of all reachable nodes from the startVertex
     */
    private int getMaxGraphStage(IGraph graph, IVertex startVertex){
        var maxStage = startVertex.getStage();

        var vertices = graph.getAllVertices();
        for(var vertex : vertices) {
            var stage = vertex.getStage();
            if(stage > maxStage)
                maxStage = stage;
        }

        return maxStage;
    }

    /** Get the depth/the maximum stage of the graph which
     * contains all reachable nodes starting at the startVertex.
     *
     * @param startVertex Vertex to start at
     * @return The maximum depth of all reachable nodes from the startVertex
     */
    private int getMaxReachableGraphStage(IGraph graph, IVertex startVertex){
        var outgoingEdges = graph.getOutgoingEdges(startVertex);
        var maxStage = startVertex.getStage();

        if(outgoingEdges.isEmpty())
            return maxStage;

        for(var edge : outgoingEdges){
            var rekMaxStage = getMaxReachableGraphStage(graph, edge.getDestination());
            if(rekMaxStage > maxStage)
                maxStage = rekMaxStage;
        }

        return maxStage;
    }

    /** Method to initialize all stages in a graph
     *
     * @param startVertex the Vertex to start the process from
     */
    public void setGraphStages(IGraph graph, Vertex startVertex){
        startVertex.setStage(1);
        setStages(graph, startVertex);
    }

    /** Helper method to initialize all stages in a graph
     *
     * @param currentVertex the vertex which is currently handled
     */
    private void setStages(IGraph graph, IVertex currentVertex) {
        var edges = graph.getOutgoingEdges(currentVertex);

        for(var edge : edges)
            edge.getDestination().setStage(determineVertexStage(graph, edge.getDestination()));

        for(var edge : edges)
            setStages(graph, edge.getDestination());
    }

    /** Get the current stage of a vertex which is determined by the maximum stage of all
     * nodes with an outgoing edge to the vertex at hand +1
     *
     * @param currentVertex the vertex at hand
     * @return the stage of the current vertex considering the current graph structure
     */
    private int determineVertexStage(IGraph graph, IVertex currentVertex) {
        int maxStage = 1;
        var edges = graph.getOutgoingEdges(currentVertex);

        for(var edge : edges){
            if(edge.getDestination() != currentVertex)
                continue;

            if(edge.getSource().getStage() > maxStage)
                maxStage = edge.getSource().getStage();
        }

        return maxStage + 1;
    }

    /** Get the maximum task index for a specific stage within all nodes
     * starting at a start vertex
     *
     * @param stage the stage to be looked for
     * @return the maximum task index for the given stage
     */
    private int getMaxTaskIndexForStage(IGraph graph, int stage){
        var maxTaskIndex = 0;
        var vertices = graph.getAllVertices();

        for(var vertex : vertices) {
            if(vertex.getStage() != stage)
                continue;
            var applicationIndex = vertex.getApplicationIndex();
            if(applicationIndex > maxTaskIndex)
                maxTaskIndex = applicationIndex;
        }

        return maxTaskIndex;
    }

    /** Get the maximum task index for a specific stage within all reachable nodes
     * starting at a start vertex
     *
     * @param currentVertex the vertex to start at (usually the graphs start vertex)
     * @param stage the stage to be looked for
     * @return the maximum task index for the given stage
     */
    private int getMaxReachableTaskIndexForStage(IGraph graph, IVertex currentVertex, int stage){
       var currentStage = currentVertex.getStage();
       var maxTaskIndex = 0;

       if(currentStage > stage)
           return maxTaskIndex;
       if(currentStage == stage)
           return currentVertex.getApplicationIndex();

       var edges = graph.getOutgoingEdges(currentVertex);
       for(var edge : edges){
           var maxEdgeTaskIndex = getMaxReachableTaskIndexForStage(graph, edge.getDestination(), stage);
           if(maxTaskIndex < maxEdgeTaskIndex)
               maxTaskIndex = maxEdgeTaskIndex;
       }

       return maxTaskIndex;
    }

    /**Construct subgraphs for given graph. The vertices of the graph must have given task and stage identifications.
     *
     * @param graph Graph to create subgraphs of
     * @return ArrayList containing all subgraphs.
     */
    public ArrayList<MobiDicSubgraph> constructSubgraphs(IGraph graph) {
        //MEASUREMENT
        var startTime = System.currentTimeMillis();

        //Prepare variables
        var startVertex = graph.getStart();

        var subgraphs = new ArrayList<MobiDicSubgraph>();
        subgraphs.add(new MobiDicSubgraph(graph, graph.getAllVertices()));

        var maxGraphStage = getMaxGraphStage(graph, startVertex);
        int[] maxTaskIndices = new int[maxGraphStage];

        for(int t = 1; t <= maxGraphStage; t++){
            maxTaskIndices[t-1] = getMaxTaskIndexForStage(graph, t);
        }

        //Go through stages
        for(int i = 1; i < maxGraphStage; i++){
            if(maxTaskIndices[i-1] < 1)
                continue;

            var newSubgraphs = new ArrayList<MobiDicSubgraph>();

            //Assume that while-loop in paper should be a for-loop
            for(int j = 0; j <= maxTaskIndices[i-1]; j++){
                for(var oldSubgraph : subgraphs){
                    var subgraph = oldSubgraph.clone();
                    subgraph.filterTaskStage(j, i);

                    if(maxTaskIndices[i] < 1){
                        subgraph.filterChildren(graph, i);
                    }

                    newSubgraphs.add(subgraph);
                }
            }

            subgraphs = newSubgraphs;
        }

        //MEASUREMENT
        var endTime = System.currentTimeMillis();
        m_Logger.debug("MobiDic construction of subgraphs - TIME CONSUMED: " + (endTime - startTime) + "ms");

        return subgraphs;
    }

    public ArrayList<MobiDicSubgraph> constructSubgraphsWithCleanup(IGraph graph) {
        //Prepare variables
        var startVertex = graph.getStart();

        var subgraphs = constructSubgraphs(graph);

        //MEASUREMENT
        var startTime = System.currentTimeMillis();

        subgraphs = new ArrayList<>(subgraphs.stream().filter(subgraph -> subgraph.reachableFrom(graph, startVertex)).toList());

        //MEASUREMENT
        var endTime = System.currentTimeMillis();
        m_Logger.debug("Subgraphs cleanup - TIME CONSUMED: " + (endTime - startTime) + "ms");

        return subgraphs;
    }

    public IGraph getApproximateWorkflow(MicroserviceChainMeasurement measurement, IGraph graph, int M, String coparatorToUse, boolean mustFindSolutionAtAnyCost) {
        var subgraphs = constructSubgraphsWithCleanup(graph);
        return getApproximateWorkflow(measurement, subgraphs, M, coparatorToUse, mustFindSolutionAtAnyCost, graph.getEnd());
    }

    public IGraph getApproximateWorkflow(MicroserviceChainMeasurement measurement, List<MobiDicSubgraph> subgraphs, int M, String coparatorToUse, boolean mustFindSolutionAtAnyCost, IVertex end) {

        //MEASUREMENT
        var startTime = System.currentTimeMillis();

        var approximatedSubgraphs = new ArrayList<Graph>();

        for(var subgraph : subgraphs) {
            var subWorkflow = getApproximatedSubworkflow(measurement, subgraph, M, coparatorToUse);

            if(subWorkflow == null) {
                if(mustFindSolutionAtAnyCost) {
                    continue;
                }
                return null;
            }

            var subWorkflowEnd = subWorkflow.getEnd();
            if(!subWorkflowEnd.equals(end))
                continue;

            approximatedSubgraphs.add(subWorkflow);
        }

        var approximatedWorkflow = approximatedSubgraphs.get(0);

        for(int i = 1; i < approximatedSubgraphs.size(); i++)
            approximatedWorkflow.uniteWithGraph(approximatedSubgraphs.get(i));

        //MEASUREMENT
        var endTime = System.currentTimeMillis();
        m_Logger.debug("MobiDic construction of approximate workflow - TIME CONSUMED: " + (endTime - startTime) + "ms");

        return approximatedWorkflow;
    }

    public Graph getApproximatedSubworkflow(MicroserviceChainMeasurement measurement, MobiDicSubgraph subgraph, int M, String coparatorToUse) {
        //MEASUREMENT
        var startTime = System.currentTimeMillis();

        //Initialize variables
        var graphToIterateUpon = subgraph.getGraphFromSubgraph();
        var result = subgraph.getGraphFromSubgraph();

        var resultVertices = new ArrayList<IVertex>();
        resultVertices.add(graphToIterateUpon.getStart());

        var count = 1;
        var vertices = graphToIterateUpon.getAllVertices();
        var K = vertices.size();

        Dijkstra dijkstra;

        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair<>(MeasurableValues.TIME.name(), false));
        //OWN EXPANSION
        if(coparatorToUse.equalsIgnoreCase("topsis")){
            prioritized.add(new Pair<>("EnergyConsumption", false));
        }

        dijkstra = new Dijkstra(coparatorToUse, m_Logger);

        var child_val = new HashMap<IVertex, Integer>();
        for(var t : vertices) {
            var edges = graphToIterateUpon.getOutgoingEdges(t);
            child_val.put(t, edges.size());
        }

        while(!vertices.isEmpty() && count < K){
            var startVertex = graphToIterateUpon.getStart();
            if(graphToIterateUpon.isEndVertex(startVertex))
                break;

            var P = dijkstra.run(null, startVertex, graphToIterateUpon, prioritized);
            var weight = startVertex.getWeight(MeasurableValues.TIME.name());
            var D = weight == null ? 0 : weight.getValue().longValue();
            for(var p : P) {
                var otherWeight = p.getEndVertex().getWeight(MeasurableValues.TIME.name());
                D += otherWeight == null ? 0 : otherWeight.getValue().longValue();
            }
            child_val.put(P.get(0).getStartVertex(), child_val.get(P.get(0).getStartVertex()) - 1);

            for(var v : P){
                child_val.put(v.getEndVertex(), child_val.get(v.getEndVertex()) - 1);
            }

            if(D > M) {
                return null;
            } else {

                for(var sp : P){
                    if(!resultVertices.contains(sp.getEndVertex()))
                        resultVertices.add(sp.getEndVertex());
                }

                var verticesToDelete = new ArrayList<IVertex>();
                for(var v : vertices){
                    if(child_val.get(v) <= 0) {
                        int stageToDelete = v.getStage();
                        for(var v2 : vertices){
                            if(v2.getStage() <= stageToDelete) {
                                graphToIterateUpon.removeVertex(v2);
                                verticesToDelete.add(v2);
                            }
                        }
                    }
                }

                for(var v : verticesToDelete)
                    vertices.remove(v);

                count++;
            }
        }

        for(var v : result.getAllVertices()){
            if(resultVertices.contains(v))
                continue;

            result.removeVertex(v);
        }

        //MEASUREMENT
        var endTime = System.currentTimeMillis();
        m_Logger.debug("Construction of single approximate subworkflow - TIME CONSUMED: " + (endTime - startTime) + "ms");

        //QoR Logging
        var pathTime = 0d;
        var pathQoR = 100d;
        var verticesForMeasurement = result.getAllVertices();
        for(var vertex : verticesForMeasurement) {
            pathTime += vertex.getWeight(MeasurableValues.TIME.name()) == null ? 0d : vertex.getWeight(MeasurableValues.TIME.name()).getValue().doubleValue();
            pathQoR *= vertex.getQoR()/100d;
        }
        if(!(measurement == null)) {
            measurement.addPathQoR(pathQoR);

            if(measurement.getTotalTime() < pathTime) {
                measurement.setTotalTimeTaken(pathTime);
            }
        }

        return result;
    }
}

