package test.Structures.Graph;

import Parser.Graph.GraphOnlineParser;
import Structures.Graph.Edge;
import Structures.Graph.Generator;
import Structures.Graph.Graph;
import Structures.Graph.Vertex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static io.github.atomfinger.touuid.UUIDs.toUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Graph_Test {
    private Logger _Logger = LogManager.getLogger("measurementLog");
    private final String _TestString1 = "hwuidha";
    private final String _TestString2 = "98123guzag";
    private final String _TestString3 = "dj893z18023";

    @Test
    public void getOutgoingEdge_FullTest() {
        var graph = new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/graph.graphml", null);

        var edges = graph.getOutgoingEdges(graph.getStart());
        assertEquals(2, edges.size());
    }

    @Test
    public void getAllEdge_FullTest() {
        var graph = new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/graph.graphml", null);

        var edges = graph.getAllEdges(graph.getStart());
        assertEquals(2, edges.size());
    }

    @Test
    public void getVertexByIdentifierTest() {
        var mockedVertex = mock(Vertex.class);
        when(mockedVertex.getLabel()).thenReturn(_TestString1);
        var mockedVertex2 = mock(Vertex.class);
        when(mockedVertex2.getLabel()).thenReturn(_TestString2);
        var mockedVertex3 = mock(Vertex.class);
        when(mockedVertex3.getLabel()).thenReturn(_TestString3);

        var graph = new Graph(UUID.randomUUID(), mockedVertex, false, "");
        graph.addVertex(mockedVertex2);
        graph.addVertex(mockedVertex3);

        var vertex = graph.getVertexByIdentifier(_TestString1);
        assertEquals(mockedVertex, vertex);
        vertex = graph.getVertexByIdentifier(_TestString2);
        assertEquals(mockedVertex2, vertex);
        vertex = graph.getVertexByIdentifier(_TestString3);
        assertEquals(mockedVertex3, vertex);
        vertex = graph.getVertexByIdentifier("_TestString3");
        assertNull(vertex);

    }

    @Test
    public void getVertexByIdTest() {
        var mockedVertex = mock(Vertex.class);
        when(mockedVertex.getId()).thenReturn(UUID.randomUUID());
        var mockedVertex2 = mock(Vertex.class);
        when(mockedVertex2.getId()).thenReturn(UUID.randomUUID());
        var mockedVertex3 = mock(Vertex.class);
        when(mockedVertex3.getId()).thenReturn(UUID.randomUUID());

        var graph = new Graph(UUID.randomUUID(), mockedVertex, false, "");
        graph.addVertex(mockedVertex2);
        graph.addVertex(mockedVertex3);

        var vertex = graph.getVertexById(mockedVertex.getId());
        assertEquals(mockedVertex, vertex);
        vertex = graph.getVertexById(mockedVertex2.getId());
        assertEquals(mockedVertex2, vertex);
        vertex = graph.getVertexById(mockedVertex3.getId());
        assertEquals(mockedVertex3, vertex);
        vertex = graph.getVertexById(UUID.randomUUID());
        assertNull(vertex);
    }

    @Test
    public void getStartVertexTest() {
        var mockedVertex = mock(Vertex.class);
        when(mockedVertex.getId()).thenReturn(UUID.randomUUID());

        var graph = new Graph(UUID.randomUUID(), mockedVertex, false, "");

        var vertex = graph.getStart();
        assertEquals(mockedVertex, vertex);
    }

    // build up a new graph that looks like (from left to right):
    //
    //      x2
    // x1         x4
    //      x3
    // Links: x1 - x2 - x4
    //        x1 - x3 - x4
    // we are going to delete x3
    @Test
    public void removeVertexTest() {
        Generator generator = null;

        try {
            generator = new Generator("../TestData/ExampleApplication.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }

        var graph = generator.generate("RemoveVertexTest", false);

        var x2 = graph.getVertexById(toUUID(1));
        var x3 = graph.getVertexById(toUUID(2));

        var vertex = graph.getAllEdges(x3);
        assertEquals(2, vertex.size());

        graph.removeVertex(x3);
        vertex = graph.getAllEdges(graph.getVertexById(toUUID(3)));
        assertEquals(1, vertex.size());

        var expectedEdgeFromVertex = vertex.get(0);
        assertEquals(x2, expectedEdgeFromVertex.getSource());
    }

    @Test
    public void getAllNextVerticesTest(){
        var vertex0 = new Vertex("", UUID.randomUUID(), "");
        var vertex1 = new Vertex("", UUID.randomUUID(), "");
        var vertex2 = new Vertex("", UUID.randomUUID(), "");
        var vertex3 = new Vertex("", UUID.randomUUID(), "");


        var graph = new Graph(UUID.randomUUID(), vertex0, false, "");

        graph.addVertex(vertex1);
        graph.addVertex(vertex2);
        graph.addVertex(vertex3);

        graph.addEdge(vertex0.getId(), vertex1.getId(), UUID.randomUUID());
        graph.addEdge(vertex0.getId(), vertex2.getId(), UUID.randomUUID());
        graph.addEdge(vertex0.getId(), vertex3.getId(), UUID.randomUUID());

        var list = graph.getAllNextVertices(vertex0);

        var test = new ArrayList<>();
        test.add(vertex1);
        test.add(vertex2);
        test.add(vertex3);
        assertEquals(list,test);
    }

    @Test
    public void addVertexText() {
        var vertex0 = new Vertex("", UUID.randomUUID(), "");
        vertex0.setStage(0);
        vertex0.setApplicationIndex(0);
        var vertex1 = new Vertex("", UUID.randomUUID(), "");
        vertex1.setStage(1);
        vertex1.setApplicationIndex(1);
        var vertex2 = new Vertex("", UUID.randomUUID(), "");
        vertex2.setStage(1);
        vertex2.setApplicationIndex(0);
        var vertex3 = new Vertex("", UUID.randomUUID(), "");
        vertex3.setStage(2);
        vertex3.setApplicationIndex(0);

        var graph = new Graph(UUID.randomUUID(), vertex0, false, "");
        graph.addVertex(vertex1);
        graph.addVertex(vertex2);
        graph.addVertex(vertex3);

        var parts = graph.getSerialParallelParts();

        assertEquals(3, parts.size());
        assertEquals(vertex0, parts.get(0).getSerialVertices().get(0));
        assertEquals(vertex1, parts.get(1).getSerialVertices().get(0));
        assertEquals(vertex2, parts.get(1).getParallelVertices().get(0));
        assertEquals(vertex3, parts.get(2).getSerialVertices().get(0));
    }

    @Test
    public void addEdgeFindCorrectDecisionMaker() {
        var instanceUnderTest = new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/graph_with_multiple_parallel.graphml", null);
        var objectsToCheck = instanceUnderTest.getAllVertices();
        var labelsToCheck = Arrays.asList(18, 10, 11, 12, 13);

        for (var vertex : objectsToCheck) {
            if (labelsToCheck.contains(vertex.getApplicationIndex()))
                assertTrue(vertex.isDecisionMakingVertex());
            else
                assertFalse(vertex.isDecisionMakingVertex());
        }
    }

    @Test
    public void findAllPathsTest() {
        var instanceUnderTest = new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/graph_with_multiple_parallel.graphml", null);

        var listOfPaths = instanceUnderTest.findAllPaths(instanceUnderTest.getStart(), instanceUnderTest.getEnd());

        for (var path : listOfPaths) {
            System.out.println("Start new path:");
            for (var edge : path) {
                System.out.println(edge.toString());
            }
        }
    }

    @Test
    public void testUniteWithGraph() {
        // Erstellen von zwei unabhängigen Graphen
        var vertexA = new Vertex("A", UUID.randomUUID(), "");
        var vertexB = new Vertex("B", UUID.randomUUID(), "");
        var vertexC = new Vertex("C", UUID.randomUUID(), "");
        var edgeA = new Edge(vertexA, vertexB, UUID.randomUUID());
        var edgeB = new Edge(vertexB, vertexC, UUID.randomUUID());

        var graph1 = new Graph(UUID.randomUUID(), vertexA, "");
        graph1.addVertex(vertexB);
        graph1.addEdge(edgeA);

        Graph graph2 = new Graph(UUID.randomUUID(), vertexB, "");
        graph2.addVertex(vertexC);
        graph2.addEdge(edgeB);

        // Vereinigung der beiden Graphen
        graph1.uniteWithGraph(graph2);

        // Überprüfen, ob alle Knoten korrekt vorhanden sind
        assertNotNull(graph1.getVertexByIdentifier("A"));
        assertNotNull(graph1.getVertexByIdentifier("B"));
        assertNotNull(graph1.getVertexByIdentifier("C"));

        // Überprüfen, ob alle Kanten korrekt vorhanden sind
        var edges = graph1.getAllEdges();
        assertEquals(2, edges.size());
        assertTrue(edges.contains(edgeA));
        assertTrue(edges.contains(edgeB));

        // Überprüfen, ob keine doppelten Kanten hinzugefügt wurden
        var edgesFromA = graph1.getOutgoingEdges(graph1.getVertexByIdentifier("A"));
        assertEquals(1, edgesFromA.size());

        var edgesFromB = graph1.getOutgoingEdges(graph1.getVertexByIdentifier("B"));
        assertEquals(1, edgesFromB.size());  // Die Kante B -> C
    }

    @Test
    public void testUniteWithGraphNoOverlap() {
        // Erstellen von zwei unabhängigen Graphen
        var vertexA = new Vertex("A", UUID.randomUUID(), "");
        var vertexB = new Vertex("B", UUID.randomUUID(), "");
        var vertexC = new Vertex("C", UUID.randomUUID(), "");
        var vertexD = new Vertex("D", UUID.randomUUID(), "");
        var edgeA = new Edge(vertexA, vertexB, UUID.randomUUID());
        var edgeB = new Edge(vertexC, vertexD, UUID.randomUUID());

        var graph1 = new Graph(UUID.randomUUID(), vertexA, "");
        graph1.addVertex(vertexB);
        graph1.addEdge(edgeA);

        Graph graph2 = new Graph(UUID.randomUUID(), vertexC, "");
        graph2.addVertex(vertexD);
        graph2.addEdge(edgeB);

        // Vereinigung der beiden Graphen
        graph1.uniteWithGraph(graph2);

        // Überprüfen, ob alle Knoten korrekt hinzugefügt wurden
        assertNotNull(graph1.getVertexByIdentifier("A"));
        assertNotNull(graph1.getVertexByIdentifier("B"));
        assertNotNull(graph1.getVertexByIdentifier("C"));
        assertNotNull(graph1.getVertexByIdentifier("D"));

        // Überprüfen, ob alle Kanten korrekt vorhanden sind
        var edges = graph1.getAllEdges();
        assertEquals(2, edges.size());
        assertTrue(edges.contains(edgeA));
        assertTrue(edges.contains(edgeB));
    }
}
