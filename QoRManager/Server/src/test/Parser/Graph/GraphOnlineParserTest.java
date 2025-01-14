package test.Parser.Graph;

import Parser.Graph.GraphOnlineParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GraphOnlineParserTest {
    private Logger _Logger = LogManager.getLogger("measurementLog");

    @Test
    public void testLoad() {
        var instanceUnderTest = new GraphOnlineParser(_Logger);
        var graph = instanceUnderTest.loadBaseGraph("../TestData/Graph/graph.graphml", null);
        var vertices = graph.getAllVertices();

        assertEquals(14, vertices.size());

        var endVertex = graph.getVertexById(UUID.randomUUID());
        assertTrue(graph.isEndVertex(endVertex));
    }

    @Test
    public void testEnhancedGraph_Load() {
        var instanceUnderTest = new GraphOnlineParser(_Logger);
        var graph = instanceUnderTest.loadBaseGraph("../TestData/Graph/graph_for_simplifier.graphml", null);
        var vertices = graph.getAllVertices();

        assertEquals(19, vertices.size());

        var endVertex = graph.getVertexById(UUID.randomUUID());
        assertTrue(graph.isEndVertex(endVertex));
    }
}
