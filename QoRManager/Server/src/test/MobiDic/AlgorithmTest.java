package test.MobiDic;

import DecisionMaking.MobiDic.algorithms.MobiDiC;

import DecisionMaking.MobiDic.structures.MobiDicSubgraph;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Edge;
import Structures.Graph.Generator;
import Structures.Graph.Graph;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlgorithmTest {

    private Graph loadGraph() throws IOException {
        var generator = new Generator("../TestData/MobiDicTestApplication.xlsx");
        return generator.generate("TestSubgraphs", true);
    }

    @Test
    public void testCompleteGeneration() throws IOException {
        var generator = new Generator("../TestData/TestApplication.xlsx");
        var graph = generator.generateGraph();
        var vertices = graph.getAllVertices();
        var expected = new HashMap<Integer, List<String>>();

        expected.computeIfAbsent(0, k -> new ArrayList<>()).addAll(Arrays.asList("0", "1", "1", "2", "2", "3", "4"));
        expected.computeIfAbsent(1, k -> new ArrayList<>()).addAll(Arrays.asList("start", "yolomod", "rcnnmod", "hog", "cnn", "deploy", "blur"));
        expected.computeIfAbsent(2, k -> new ArrayList<>()).addAll(Arrays.asList("0", "30", "50", "100", "125", "15", "20"));

        var index = 0;
        for(var v : vertices) {
            var timeCosts = v.getWeight(MeasurableValues.TIME.name());
            var weight = timeCosts == null ? 0 : timeCosts.getValue();

            assertEquals(expected.get(0).get(index), String.valueOf(v.getStage()));
            assertEquals(expected.get(1).get(index), v.getServiceName());
            assertEquals(expected.get(2).get(index), String.valueOf(weight));
            index++;
            System.out.println(v.getStage() +" - "+ v.getLabel() + "   " + v.getId() + "   " + v.getServiceName() + "   " + weight);
        }
    }

    @Test
    public void testSubGraphs() throws IOException {
        var graph = loadGraph();
        createNodes(graph);

        var mobiDic = new MobiDiC();

        var mobiSubgraphs = mobiDic.constructSubgraphsWithCleanup(graph);
        var mobiSubgraph1 = mobiSubgraphs.get(0);
        var mobiSubgraph2 = mobiSubgraphs.get(1);
        var verticesMobiSubgraph1 = mobiSubgraph1.getVertices();
        var verticesMobiSubgraph2 = mobiSubgraph2.getVertices();

        //TEST CONSTRUCTION OF MOBIDIC-SUBGRAPHS

        assertEquals(9, verticesMobiSubgraph1.size());
        assertEquals("k011", verticesMobiSubgraph1.get(0).getLabel());
        assertEquals("k111", verticesMobiSubgraph1.get(1).getLabel());
        assertEquals("k112", verticesMobiSubgraph1.get(2).getLabel());
        assertEquals("k211", verticesMobiSubgraph1.get(3).getLabel());
        assertEquals("k212", verticesMobiSubgraph1.get(4).getLabel());
        assertEquals("k311", verticesMobiSubgraph1.get(5).getLabel());
        assertEquals("k312", verticesMobiSubgraph1.get(6).getLabel());
        assertEquals("k411", verticesMobiSubgraph1.get(7).getLabel());
        assertEquals("k511", verticesMobiSubgraph1.get(8).getLabel());

        assertEquals(8, verticesMobiSubgraph2.size());
        assertEquals("k011", verticesMobiSubgraph2.get(0).getLabel());
        assertEquals("k121", verticesMobiSubgraph2.get(1).getLabel());
        assertEquals("k122", verticesMobiSubgraph2.get(2).getLabel());
        assertEquals("k222", verticesMobiSubgraph2.get(3).getLabel());
        assertEquals("k311", verticesMobiSubgraph2.get(4).getLabel());
        assertEquals("k312", verticesMobiSubgraph2.get(5).getLabel());
        assertEquals("k411", verticesMobiSubgraph2.get(6).getLabel());
        assertEquals("k511", verticesMobiSubgraph2.get(7).getLabel());

        // TEST CONSTRUCTION OF SUBGRAPHS WITH MOBIDIC-SUBGRAPHS

        var subgraph1 = mobiSubgraph1.getGraphFromSubgraph();
        var subgraph2 = mobiSubgraph2.getGraphFromSubgraph();
        var verticesSubgraph1 = subgraph1.getAllVertices();
        var verticesSubgraph2 = subgraph2.getAllVertices();

        //FIRST SUBGRAPH

        var vertexUnderTest = verticesSubgraph1.get(0);
        var edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertEquals("k011", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k011", "k111"),
                new Pair<>("k011", "k112")
        ));

        vertexUnderTest = verticesSubgraph1.get(1);
        edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertEquals("k111", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k111", "k211"),
                new Pair<>("k111", "k212"),
                new Pair<>("k011", "k111")
        ));

        vertexUnderTest = subgraph1.getVertexByIdentifier("k211");
        edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k211", "k311"),
                new Pair<>("k211", "k312"),
                new Pair<>("k111", "k211"),
                new Pair<>("k112", "k211")
        ));

        vertexUnderTest = subgraph1.getVertexByIdentifier("k311");
        edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k311", "k411"),
                new Pair<>("k211", "k311"),
                new Pair<>("k212", "k311")
        ));

        vertexUnderTest = subgraph1.getVertexByIdentifier("k411");
        edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511"),
                new Pair<>("k311", "k411"),
                new Pair<>("k312", "k411")
        ));

        vertexUnderTest = subgraph1.getVertexByIdentifier("k511");
        edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511")
        ));

        vertexUnderTest = verticesSubgraph1.get(6);
        edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertEquals("k312", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k312", "k411"),
                new Pair<>("k211", "k312"),
                new Pair<>("k212", "k312")
        ));

        vertexUnderTest = subgraph1.getVertexByIdentifier("k212");
        edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k212", "k311"),
                new Pair<>("k212", "k312"),
                new Pair<>("k111", "k212"),
                new Pair<>("k112", "k212")
        ));

        vertexUnderTest = subgraph1.getVertexByIdentifier("k112");
        edgesOfVertex = subgraph1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k112", "k211"),
                new Pair<>("k112", "k212"),
                new Pair<>("k011", "k112")
        ));

        //___________________ SECOND SUBGRAPH __________________________________

        vertexUnderTest = verticesSubgraph2.get(0);
        edgesOfVertex = subgraph2.getAllEdges(vertexUnderTest);
        assertEquals("k011", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k011", "k121"),
                new Pair<>("k011", "k122")
        ));

        vertexUnderTest = subgraph2.getVertexByIdentifier("k311");
        edgesOfVertex = subgraph2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k311", "k411"),
                new Pair<>("k222", "k311")
        ));

        vertexUnderTest = subgraph2.getVertexByIdentifier("k411");
        edgesOfVertex = subgraph2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511"),
                new Pair<>("k311", "k411"),
                new Pair<>("k312", "k411")
        ));

        vertexUnderTest = subgraph2.getVertexByIdentifier("k511");
        edgesOfVertex = subgraph2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511")
        ));

        vertexUnderTest = subgraph2.getVertexByIdentifier("k312");
        edgesOfVertex = subgraph2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k312", "k411"),
                new Pair<>("k222", "k312")
        ));

        vertexUnderTest = subgraph2.getVertexByIdentifier("k121");
        edgesOfVertex = subgraph2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k121", "k222"),
                new Pair<>("k011", "k121")
        ));

        vertexUnderTest = subgraph2.getVertexByIdentifier("k222");
        edgesOfVertex = subgraph2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k222", "k311"),
                new Pair<>("k222", "k312"),
                new Pair<>("k121", "k222"),
                new Pair<>("k122", "k222")
        ));

        vertexUnderTest = subgraph2.getVertexByIdentifier("k122");
        edgesOfVertex = subgraph2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k122", "k222"),
                new Pair<>("k011", "k122")
        ));
    }

    @Test
    public void testApproximation() throws IOException {
        var graph = loadGraph();
        var nodes = createNodes(graph);

        var mobiDic = new MobiDiC();

        var verticesMobiSubgraph1 = new ArrayList<IVertex>();
        verticesMobiSubgraph1.add(nodes.get(0));
        verticesMobiSubgraph1.add(nodes.get(1));
        verticesMobiSubgraph1.add(nodes.get(2));
        verticesMobiSubgraph1.add(nodes.get(5));
        verticesMobiSubgraph1.add(nodes.get(6));
        verticesMobiSubgraph1.add(nodes.get(8));
        verticesMobiSubgraph1.add(nodes.get(9));
        verticesMobiSubgraph1.add(nodes.get(10));
        verticesMobiSubgraph1.add(nodes.get(11));
        var mobiSubgraph1 = new MobiDicSubgraph(graph, verticesMobiSubgraph1);

        var verticesMobiSubgraph2 = new ArrayList<IVertex>();
        verticesMobiSubgraph2.add(nodes.get(0));
        verticesMobiSubgraph2.add(nodes.get(3));
        verticesMobiSubgraph2.add(nodes.get(4));
        verticesMobiSubgraph2.add(nodes.get(7));
        verticesMobiSubgraph2.add(nodes.get(8));
        verticesMobiSubgraph2.add(nodes.get(9));
        verticesMobiSubgraph2.add(nodes.get(10));
        verticesMobiSubgraph2.add(nodes.get(11));
        var mobiSubgraph2 = new MobiDicSubgraph(graph, verticesMobiSubgraph2);

        var M = 67;

        //TEST CONSTRUCTION OF APPROXIMATED SUBWORKFLOWS

        var workflow1 = mobiDic.getApproximatedSubworkflow(null, mobiSubgraph1, M, "");
        var workflow2 = mobiDic.getApproximatedSubworkflow(null, mobiSubgraph2, M, "");

        var verticesWorkflow1 = workflow1.getAllVertices();
        var verticesWorkflow2 = workflow2.getAllVertices();

        //TEST FIRST SUBWORKFLOW

        var vertexUnderTest = verticesWorkflow1.get(0);
        var edgesOfVertex = workflow1.getAllEdges(vertexUnderTest);
        assertEquals("k011", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k011", "k112")
        ));

        vertexUnderTest = workflow1.getVertexByIdentifier("k411");
        edgesOfVertex = workflow1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511"),
                new Pair<>("k312", "k411")
        ));

        vertexUnderTest =  workflow1.getVertexByIdentifier("k511");
        edgesOfVertex = workflow1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511")
        ));

        vertexUnderTest = workflow1.getVertexByIdentifier("k312");
        edgesOfVertex = workflow1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k312", "k411"),
                new Pair<>("k212", "k312")
        ));

        vertexUnderTest = workflow1.getVertexByIdentifier("k212");
        edgesOfVertex = workflow1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k212", "k312"),
                new Pair<>("k112", "k212")
        ));

        vertexUnderTest =  workflow1.getVertexByIdentifier("k112");
        edgesOfVertex = workflow1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k112", "k212"),
                new Pair<>("k011", "k112")
        ));

        vertexUnderTest =  workflow1.getVertexByIdentifier("k112");
        edgesOfVertex = workflow1.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k112", "k212"),
                new Pair<>("k011", "k112")
        ));

        // ________________ TEST SECOND SUBWORKFLOW _____________________

        vertexUnderTest = verticesWorkflow2.get(0);
        edgesOfVertex = workflow2.getAllEdges(vertexUnderTest);
        assertEquals("k011", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k011", "k121")
        ));

        vertexUnderTest =  workflow1.getVertexByIdentifier("k411");
        edgesOfVertex = workflow2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511"),
                new Pair<>("k312", "k411")
        ));

        vertexUnderTest =  workflow2.getVertexByIdentifier("k511");
        edgesOfVertex = workflow2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511")
        ));

        vertexUnderTest = verticesWorkflow2.get(3);
        edgesOfVertex = workflow2.getAllEdges(vertexUnderTest);
        assertEquals("k312", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k312", "k411"),
                new Pair<>("k222", "k312")
        ));

        vertexUnderTest = workflow2.getVertexByIdentifier("k121");
        edgesOfVertex = workflow2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k121", "k222"),
                new Pair<>("k011", "k121")
        ));

        vertexUnderTest = workflow2.getVertexByIdentifier("k222");
        edgesOfVertex = workflow2.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k222", "k312"),
                new Pair<>("k121", "k222")
        ));

        // ______________ TEST APPROXIMATE WORKFLOW __________________

        var mobiSubgraphs = new ArrayList<MobiDicSubgraph>();
        mobiSubgraphs.add(mobiSubgraph1);
        mobiSubgraphs.add(mobiSubgraph2);

        var approximateWorkflow = mobiDic.getApproximateWorkflow(null, mobiSubgraphs, M, "", false, graph.getEnd());
        var verticesApproximateWorkflow = approximateWorkflow.getAllVertices();

        vertexUnderTest = verticesApproximateWorkflow.get(0);
        edgesOfVertex = approximateWorkflow.getAllEdges(vertexUnderTest);
        assertEquals("k011", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k011", "k112"),
                new Pair<>("k011", "k121")
        ));

        vertexUnderTest = approximateWorkflow.getVertexByIdentifier("k411");
        edgesOfVertex = approximateWorkflow.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511"),
                new Pair<>("k312", "k411")
        ));

        vertexUnderTest = approximateWorkflow.getVertexByIdentifier("k511");
        edgesOfVertex = approximateWorkflow.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k411", "k511")
        ));

        vertexUnderTest = verticesApproximateWorkflow.get(3);
        edgesOfVertex = approximateWorkflow.getAllEdges(vertexUnderTest);
        assertEquals("k312", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k312", "k411"),
                new Pair<>("k212", "k312"),
                new Pair<>("k222", "k312")
        ));

        vertexUnderTest = approximateWorkflow.getVertexByIdentifier("k212");
        edgesOfVertex = approximateWorkflow.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k212", "k312"),
                new Pair<>("k112", "k212")
        ));

        vertexUnderTest = approximateWorkflow.getVertexByIdentifier("k112");
        edgesOfVertex = approximateWorkflow.getAllEdges(vertexUnderTest);
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k112", "k212"),
                new Pair<>("k011", "k112")
        ));

        vertexUnderTest = verticesApproximateWorkflow.get(6);
        edgesOfVertex = approximateWorkflow.getAllEdges(vertexUnderTest);
        assertEquals("k121", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k121", "k222"),
                new Pair<>("k011", "k121")
        ));

        vertexUnderTest = verticesApproximateWorkflow.get(7);
        edgesOfVertex = approximateWorkflow.getAllEdges(vertexUnderTest);
        assertEquals("k222", vertexUnderTest.getLabel());
        assertTrue(checkCorrectConnections(edgesOfVertex,
                new Pair<>("k222", "k312"),
                new Pair<>("k121", "k222")
        ));
    }

    @SafeVarargs
    private boolean checkCorrectConnections(List<Edge> edges, Pair<String, String>... toCheck) {
        assertEquals(toCheck.length, edges.size());
        var tmp = new ArrayList<>(edges);
        for (var pairToCheck : toCheck) {
            var iterator = tmp.iterator();
            while (iterator.hasNext()) {
                var edge = iterator.next();
                if (edge.getSource().getLabel().equals(pairToCheck.getFirst()) &&
                        edge.getDestination().getLabel().equals(pairToCheck.getSecond())) {
                    iterator.remove();
                    break;
                }
            }
        }
        return tmp.isEmpty();
    }

    private List<IVertex> createNodes(IGraph graph) {
        var nodes = graph.getAllVertices();
        var time = MeasurableValues.TIME.name();

        nodes.get(0).setStage(0);
        nodes.get(0).setApplicationIndex(0);
        nodes.get(0).setApproximationIndex(0);
        nodes.get(0).updateWeight(time, 1);

        nodes.get(1).setStage(1);
        nodes.get(1).setApplicationIndex(0);
        nodes.get(1).setApproximationIndex(0);
        nodes.get(1).updateWeight(time, 3);

        nodes.get(2).setStage(1);
        nodes.get(2).setApplicationIndex(0);
        nodes.get(2).setApproximationIndex(1);
        nodes.get(2).updateWeight(time, 2);

        nodes.get(3).setStage(1);
        nodes.get(3).setApplicationIndex(1);
        nodes.get(3).setApproximationIndex(0);
        nodes.get(3).updateWeight(time, 13);

        nodes.get(4).setStage(1);
        nodes.get(4).setApplicationIndex(1);
        nodes.get(4).setApproximationIndex(1);
        nodes.get(4).updateWeight(time, 24);

        nodes.get(5).setStage(2);
        nodes.get(5).setApplicationIndex(0);
        nodes.get(5).setApproximationIndex(0);
        nodes.get(5).updateWeight(time, 4);

        nodes.get(6).setStage(2);
        nodes.get(6).setApplicationIndex(0);
        nodes.get(6).setApproximationIndex(1);
        nodes.get(6).updateWeight(time, 3);

        nodes.get(7).setStage(2);
        nodes.get(7).setApplicationIndex(1);
        nodes.get(7).setApproximationIndex(1);
        nodes.get(7).updateWeight(time, 31);

        nodes.get(8).setStage(3);
        nodes.get(8).setApplicationIndex(0);
        nodes.get(8).setApproximationIndex(0);
        nodes.get(8).updateWeight(time, 7);

        nodes.get(9).setStage(3);
        nodes.get(9).setApplicationIndex(0);
        nodes.get(9).setApproximationIndex(1);
        nodes.get(9).updateWeight(time, 6);

        nodes.get(10).setStage(4);
        nodes.get(10).setApplicationIndex(0);
        nodes.get(10).setApproximationIndex(0);
        nodes.get(10).updateWeight(time, 2);

        nodes.get(11).setStage(5);
        nodes.get(11).setApplicationIndex(0);
        nodes.get(11).setApproximationIndex(0);
        nodes.get(11).updateWeight(time, 5);

        return nodes;
    }
}
