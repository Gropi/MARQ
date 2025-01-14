package test.Structures.Graph.SimplifiedGraph;

import Parser.Graph.GraphOnlineParser;
import Structures.Graph.SimplifiedGraph.GraphSimplifier;
import Structures.Graph.interfaces.IVertex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphSimplifierTest {
    private Logger _Logger = LogManager.getLogger("measurementLog");

    @Test
    public void testGetSubgraphIndices() {
        var instanceUnderTest = new GraphSimplifier();
        var graph = new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/graph_for_simplifier.graphml", null);
        //var graph = new GraphOnlineParser().loadBaseGraph("../TestData/Graph/graph_with_multiple_parallel.graphml");

        var graphSimplifier = new GraphSimplifier();

        var result = graphSimplifier.getSubgraphIndices(graph);

        Integer[] expR1 = {0, 0, 0, 0, 0, 0};
        Integer[] expR2 = {0, 1, 1, 0, 0, 0};
        Integer[] expR3 = {0, 2, 2, 1, 1, 0};
        Integer[] expR4 = {0, 2, 3, 1, 1, 0};
        Integer[] expR5 = {0, 3, 4, 2, 1, 0};
        Integer[][] expectedResult = {expR1, expR2, expR3, expR4, expR5};

        for(int i = 0; i < result.size(); i++) {
            for(int j = 0; j < result.get(i).size(); j++){
                assertEquals(expectedResult[i][j], result.get(i).get(j));
            }
        }
    }

    @Test
    public void testGetSubgraphIndicesIterative() {
        var instanceUnderTest = new GraphSimplifier();
        var graph = new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/graph_for_simplifier.graphml", null);
        //var graph = new GraphOnlineParser().loadBaseGraph("../TestData/Graph/graph_with_multiple_parallel.graphml");

        var graphSimplifier = new GraphSimplifier();

        var result = graphSimplifier.getSubgraphIndicesIterative(graph);

        Integer[] expR1 = {0, 3, 4, 2, 1, 0};
        Integer[] expR2 = {0, 2, 3, 1, 1, 0};
        Integer[] expR3 = {0, 2, 2, 1, 1, 0};
        Integer[] expR4 = {0, 1, 1, 0, 0, 0};
        Integer[] expR5 = {0, 0, 0, 0, 0, 0};
        Integer[][] expectedResult = {expR1, expR2, expR3, expR4, expR5};

        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 6; j++){
                assertEquals(expectedResult[i][j], result.get(i).get(j));
            }
        }
    }

    @Test
    public void testSubgraphGeneration(){
        var instanceUnderTest = new GraphSimplifier();
        var graph = new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/graph_for_simplifier.graphml", null);
        //var graph = new GraphOnlineParser().loadBaseGraph("../TestData/Graph/graph_with_multiple_parallel.graphml");

        var graphSimplifier = new GraphSimplifier();

        //GET NODES
        var dummy1 = graph.getVertexByIdentifier("dummy1");
        var dummy2 = graph.getVertexByIdentifier("dummy2");
        var dummy3 = graph.getVertexByIdentifier("dummy3");
        var dummy4 = graph.getVertexByIdentifier("dummy4");
        var dummy5 = graph.getVertexByIdentifier("dummy5");
        var dummy6 = graph.getVertexByIdentifier("dummy6");
        var dummy7 = graph.getVertexByIdentifier("dummy7");
        var dummy8 = graph.getVertexByIdentifier("dummy8");
        var dummy9 = graph.getVertexByIdentifier("dummy9");
        var dummy10 = graph.getVertexByIdentifier("dummy10");
        var dummy11 = graph.getVertexByIdentifier("dummy11");
        var dummy12 = graph.getVertexByIdentifier("dummy12");
        var dummy13 = graph.getVertexByIdentifier("dummy13");
        var dummy14 = graph.getVertexByIdentifier("dummy14");
        var dummy15 = graph.getVertexByIdentifier("dummy15");
        var dummy16 = graph.getVertexByIdentifier("dummy16");
        var dummy17 = graph.getVertexByIdentifier("dummy17");
        var dummy18 = graph.getVertexByIdentifier("dummy18");
        var dummy19 = graph.getVertexByIdentifier("dummy19");

        //INITIALIZE RESULT AND PARTIAL RESULTS
        var expectedResult = new ArrayList<ArrayList<IVertex>>();

        var result1 = new ArrayList<IVertex>();
        var result2 = new ArrayList<IVertex>();
        var result3 = new ArrayList<IVertex>();
        var result4 = new ArrayList<IVertex>();
        var result5 = new ArrayList<IVertex>();

        //FILL RESULTS WITH NODES
        result1.add(dummy1);
        result1.add(dummy2);
        result1.add(dummy13);
        result1.add(dummy16);
        result1.add(dummy17);
        result1.add(dummy18);
        result1.add(dummy19);

        result2.add(dummy1);
        result2.add(dummy3);
        result2.add(dummy13);
        result2.add(dummy14);
        result2.add(dummy15);
        result2.add(dummy18);
        result2.add(dummy19);

        result3.add(dummy1);
        result3.add(dummy4);
        result3.add(dummy6);
        result3.add(dummy10);
        result3.add(dummy12);
        result3.add(dummy13);

        result4.add(dummy1);
        result4.add(dummy4);
        result4.add(dummy7);
        result4.add(dummy10);
        result4.add(dummy12);
        result4.add(dummy13);

        result5.add(dummy1);
        result5.add(dummy5);
        result5.add(dummy8);
        result5.add(dummy9);
        result5.add(dummy11);
        result5.add(dummy12);
        result5.add(dummy13);

        expectedResult.add(result5);
        expectedResult.add(result4);
        expectedResult.add(result3);
        expectedResult.add(result2);
        expectedResult.add(result1);

        var result = graphSimplifier.getSubgraphs(graph);

        for(int i = 0; i < result.size(); i++) {
            var subgraph = result.get(i);
            var subgraphVertices = subgraph.getAllVertices();

            for(int j = 0; j < subgraphVertices.size(); j++) {
                var vertex = subgraphVertices.get(j);

                assertEquals(expectedResult.get(i).get(j), vertex);
            }
        }
    }
}
