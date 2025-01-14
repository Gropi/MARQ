package CreatorTestData;

import org.apache.logging.log4j.Logger;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Edge;
import Structures.Graph.Graph;
import Structures.Graph.SimplifiedGraph.GraphSimplifier;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;

import java.util.*;

public class TestGraphCreator {
    private final Logger _Logger;
    private final Random m_Random = new Random();

    public TestGraphCreator(Logger logger) {
        _Logger = logger;
    }

    public IGraph randomizeGraphCost(IGraph graph) {
        for (var vertex : graph.getAllVertices()) {
            var qor = m_Random.nextInt(50, 100);
            if (graph.isEndVertex(vertex) || graph.getStart().equals(vertex))
                qor = 100;
            vertex.setQoR(qor);
            var factor = (double) qor / 100;
            vertex.updateWeight(MeasurableValues.TIME.name(), getNext(700, 800) * factor);
            vertex.updateWeight(MeasurableValues.ENERGY.name(), getNext(10, 500) * factor);
        }

        for (var edge : graph.getAllEdges()) {
            edge.updateWeight(MeasurableValues.LATENCY.name(), getNext(1, 50));
        }

        return graph;
    }

    public IGraph randomizeGraphCostWithAdvancedParameters(IGraph graph) {
        _Logger.debug("Creating random parameter costs for graph...");
        var optimum = new HashMap<Integer, Map<Integer, List<IVertex>>>();
        for (var vertex : graph.getAllVertices()) {
            var stage = vertex.getStage();
            if (!optimum.containsKey(stage)) {
                optimum.put(stage, new HashMap<>());
            }
            var stageEntries = optimum.get(stage);
            var applicationID = vertex.getApplicationIndex();
            if (!stageEntries.containsKey(applicationID))
                stageEntries.put(applicationID, new ArrayList<>());
            var vertices = stageEntries.get(applicationID);
            vertices.add(vertex);
        }

        for (var stage : optimum.keySet()) {
            var stageEntries = optimum.get(stage);
            for (var applicationID : stageEntries.keySet()) {
                randomizeAdvancedWithSameAppID(stageEntries.get(applicationID));
            }
        }

        for (var edge : graph.getAllEdges()) {
            edge.updateWeight(MeasurableValues.LATENCY.name(), getNext(1, 100));
        }

        return graph;
    }

    private double calculateRandomPrice(int time) {
        var basePricePerHour = 60;
        var millisecondsPerHour = 3600000;
        var logarithmicFactor = 0.5;
        var linearFactor = 0.5;
        var randomFactor = getNext(80, 100)/100d;

        var costInEuro = randomFactor *
                (logarithmicFactor * basePricePerHour * Math.log(time + 1) /Math.log(millisecondsPerHour)
                        + linearFactor * basePricePerHour * time /millisecondsPerHour);

        return costInEuro * 100;
    }

    private void randomizeAdvancedWithSameAppID(List<IVertex> vertices) {
        var optimumVertexId = vertices.size() == 1 ? 0 : new Random().nextInt(0, vertices.size() - 1);
        var optimumVertex = optimizeVertex(vertices.get(optimumVertexId));
        for (var vertex : vertices) {
            if (vertex != optimumVertex) {
                randomizeAdvancedWithRespectTo(vertex, optimumVertex);
            }
        }
    }

    private IVertex optimizeVertex(IVertex vertex) {
        var time = (int) (getNext(600, 800));
        var energy = (int) (getNext(10, 500));
        var costInCents = calculateRandomPrice(time);

        vertex.updateWeight(MeasurableValues.QoR.name(), 100);
        vertex.setQoR(100);

        vertex.updateWeight(MeasurableValues.PARAMETER_1.name(), 100d);
        vertex.updateWeight(MeasurableValues.PARAMETER_2.name(), 100d);
        vertex.updateWeight(MeasurableValues.PARAMETER_3.name(), 100d);
        vertex.updateWeight(MeasurableValues.PARAMETER_4.name(), 100d);
        vertex.updateWeight(MeasurableValues.PARAMETER_5.name(), 100d);
        vertex.updateWeight(MeasurableValues.TIME.name(), time);
        vertex.updateWeight(MeasurableValues.ENERGY.name(), energy);
        vertex.updateWeight(MeasurableValues.COST.name(), costInCents);

        return vertex;
    }

    private void randomizeAdvancedWithRespectTo(IVertex vertex, IVertex optimumVertex) {
        var qor = m_Random.nextInt(50, 99);
        vertex.updateWeight(MeasurableValues.QoR.name(), qor);
        vertex.setQoR( qor);

        var parameter1 = m_Random.nextDouble(50, 100);
        var parameter2 = m_Random.nextDouble(50, 100);
        var parameter3 = m_Random.nextDouble(50, 100);
        var parameter4 = m_Random.nextDouble(50, 100);
        var parameter5 = m_Random.nextDouble(50, 100);

        var factor = qor/100d;
        var time = (int) (getNext((int)(600 * factor), optimumVertex.getWeight(MeasurableValues.TIME.name()).getValue().intValue()) * factor);
        var energy = (int) (getNext((int)(10 * factor), optimumVertex.getWeight(MeasurableValues.ENERGY.name()).getValue().intValue()) * factor);
        var cpu = (int) (getNext((int)(10 * factor), optimumVertex.getWeight(MeasurableValues.CPU.name()).getValue().intValue()) * factor);
        var ram = (int) (getNext((int)(50 * factor), optimumVertex.getWeight(MeasurableValues.RAM.name()).getValue().intValue()) * factor);
        var costInCents = calculateRandomPrice(time);

        vertex.updateWeight(MeasurableValues.PARAMETER_1.name(), parameter1);
        vertex.updateWeight(MeasurableValues.PARAMETER_2.name(), parameter2);
        vertex.updateWeight(MeasurableValues.PARAMETER_3.name(), parameter3);
        vertex.updateWeight(MeasurableValues.PARAMETER_4.name(), parameter4);
        vertex.updateWeight(MeasurableValues.PARAMETER_5.name(), parameter5);
        vertex.updateWeight(MeasurableValues.TIME.name(), time);
        vertex.updateWeight(MeasurableValues.ENERGY.name(), energy);
        vertex.updateWeight(MeasurableValues.CPU.name(), cpu);
        vertex.updateWeight(MeasurableValues.RAM.name(), ram);
        vertex.updateWeight(MeasurableValues.COST.name(), costInCents);

    }

    public void setPathWithPerfectQoRAcrossSubgraphs(IGraph graph) {
        var simplifier = new GraphSimplifier();
        var subgraphIndices = simplifier.getSubgraphIndicesIterative(graph);
        var subgraphs = simplifier.getSubgraphs(graph, subgraphIndices);

        for(var subgraph : subgraphs) {
            setPathWithPerfectQoR(subgraph);
        }
     }

    private void setPathWithPerfectQoR(IGraph graph) {
        var random = new Random();
        var currentVertex = graph.getStart();

        while(!graph.isEndVertex(currentVertex)) {
            var edges = graph.getOutgoingEdges(currentVertex);
            var optimalAvailable = edges.stream().filter(x -> x.getDestination().getQoR() == 100).findFirst().orElse(null);
            if (optimalAvailable == null) {
                // TODO: For the moment this works. However, if a graph is not fully connected, we need to handle the case in which
                // the path might come to a situation in which 100% is not possible.
                var chosenIndex = random.nextInt(edges.size());
                currentVertex = edges.get(chosenIndex).getDestination();

                var oldQoR = currentVertex.getQoR();
                var oldTime = currentVertex.getWeight(MeasurableValues.TIME.name()).getValue().intValue();
                var newTime = oldTime/oldQoR*100;

                currentVertex.setQoR(100);
                //Apart from updating the QoR we also need to relatively update the time to
                // keep a reasonable correlation between time and qor
                currentVertex.updateWeight(MeasurableValues.TIME.name(), newTime);
            } else {
                currentVertex = optimalAvailable.getDestination();
            }
        }
    }

    public IGraph createGraph(int amountNodes, int stages) {
        var startVertex = createVertex("Start", "Start", 1, 0, 1, 100);
        var endVertex = createVertex("End", "End", 1, stages - 1, 1, 100);
        var graph = new Graph(UUID.randomUUID(), startVertex, "");

        boolean isLastIterationMultiTask = false;

        var vertexesUsed = 2;
        var lastVertices = new ArrayList<IVertex>();
        var applicationCountLastStage = 1;
        for (int i = 1; i < stages - 1; i++) {
            if (stages - i > 0) {
                var isMultiStage = m_Random.nextBoolean();
                if (isMultiStage) {
                    isLastIterationMultiTask = true;
                } else {
                    if (isLastIterationMultiTask) {
                        var nextVertex = createVertex(String.valueOf(i), String.valueOf(i), 1, i, 1, 100);
                        graph.addVertex(nextVertex);
                        for (var vertex : lastVertices) {
                            graph.addEdge(createEdge(vertex, nextVertex));
                        }
                        lastVertices.clear();
                    } else {
                        var alternatives = getNext(0, amountNodes - vertexesUsed + stages - i);

                    }
                    isLastIterationMultiTask = false;
                }
            } else {
                // create rest of vertices
            }
        }
        // Add the edges to the last vertex.
        graph.addVertex(endVertex);
        for (var vertex : lastVertices) {
            graph.addEdge(createEdge(vertex, endVertex));
        }
        return graph;
    }

    private IVertex createVertex(String label, String serviceName, int applicationIndex, int stage, int approximation, int QoR) {
        var nextVertex = new Vertex(label, UUID.randomUUID(), serviceName);
        nextVertex.setApplicationIndex(applicationIndex);
        nextVertex.setStage(stage);
        nextVertex.setApproximationIndex(approximation);
        nextVertex.updateWeight(MeasurableValues.TIME.name(), getNext(10, 500));
        nextVertex.updateWeight(MeasurableValues.ENERGY.name(), getNext(1, 5000));
        nextVertex.setQoR(QoR);
        return nextVertex;
    }

    private IVertex createVertex(String label, String serviceName, int applicationIndex, int stage, int approximation) {
        return createVertex(label, serviceName, applicationIndex, stage, approximation, getNext(50, 100));
    }

    private Edge createEdge(IVertex start, IVertex end) {
        var nextEdge = new Edge(start, end, UUID.randomUUID());
        nextEdge.updateWeight(MeasurableValues.LATENCY.name(), getNext(0, 50));
        return nextEdge;
    }

    private Integer getNext(int start, int end) {
        return m_Random.nextInt(start, end);
    }
}
