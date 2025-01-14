package test.GraphPathSearch;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import GraphPathSearch.Dijkstra;
import Monitoring.Enums.MeasurableValues;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import test.Helper.TestDataCreator;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DijkstraTest {
    private final Logger m_Logger = LogManager.getLogger("measurementLog");

    @Test
    public void testToExploitOldDijkstra() {
        var testData = new TestDataCreator();
        var graph = testData.generateGraphToExploitOldDijkstra();
        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair<>(MeasurableValues.TIME.name(), false));

        var instanceUnderTest = new Dijkstra(m_Logger);
        var result = instanceUnderTest.run(null, graph.getStart(), graph, prioritized);

        String[] expectedVerticesInPath = {"Vertex0", "Vertex2", "Vertex1", "Vertex3", "Vertex5"};

        for(int i = 0; i < result.size(); i++) {
            assertEquals(expectedVerticesInPath[i], result.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath[4], result.get(3).getEndVertex().getLabel());
    }

    @Test
    public void testParetoDijkstraWithOnePath() {
        var testData = new TestDataCreator();
        var graph = testData.generateGraphToExploitOldDijkstra();
        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair<>(MeasurableValues.TIME.name(), false));

        var instanceUnderTest = new Dijkstra(m_Logger);

        var result = instanceUnderTest.runParetoPaths(graph.getStart(), graph, prioritized);

        assertEquals(1, result.size());

        var shortestPath = result.get(0);

        String[] expectedVerticesInPath = {"Vertex0", "Vertex2", "Vertex1", "Vertex3", "Vertex5"};

        for(int i = 0; i < shortestPath.size(); i++) {
            assertEquals(expectedVerticesInPath[i], shortestPath.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath[4], shortestPath.get(3).getEndVertex().getLabel());
    }

    @Test
    public void testParetoDijkstraWithMultiplePaths() {
        var testData = new TestDataCreator();
        var graph = testData.generateMultiParetoGraph();
        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair<>(MeasurableValues.TIME.name(), false));
        prioritized.add(new Pair<>(MeasurableValues.RAM.name(), false));

        var instanceUnderTest = new Dijkstra(m_Logger);

        var result = instanceUnderTest.runParetoPaths(graph.getStart(), graph, prioritized);

        assertEquals(3, result.size());

        var shortestPath = result.get(0);
        String[] expectedVerticesInPath1 = {"Vertex0", "Vertex1", "Vertex3", "Vertex5"};
        for(int i = 0; i < shortestPath.size(); i++) {
            assertEquals(expectedVerticesInPath1[i], shortestPath.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath1[3], shortestPath.get(2).getEndVertex().getLabel());

        shortestPath = result.get(1);
        String[] expectedVerticesInPath2 = {"Vertex0", "Vertex2", "Vertex1", "Vertex3", "Vertex5"};
        for(int i = 0; i < shortestPath.size(); i++) {
            assertEquals(expectedVerticesInPath2[i], shortestPath.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath2[4], shortestPath.get(3).getEndVertex().getLabel());

        shortestPath = result.get(2);
        String[] expectedVerticesInPath3 = {"Vertex0", "Vertex2", "Vertex4", "Vertex5"};
        for(int i = 0; i < shortestPath.size(); i++) {
            assertEquals(expectedVerticesInPath3[i], shortestPath.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath3[3], shortestPath.get(2).getEndVertex().getLabel());
    }

    @Test
    public void testEConstraintDijkstra() {
        var testData = new TestDataCreator();
        var graph = testData.generateMultiParetoGraph();
        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair<>(MeasurableValues.TIME.name(), false));
        prioritized.add(new Pair<>(MeasurableValues.RAM.name(), false));
        var limits = new HashMap<String, Number>();
        limits.put(MeasurableValues.RAM.name(), 5);
        var criteria = new Pair<>(MeasurableValues.TIME.name(), false);

        var instanceUnderTest = new Dijkstra(m_Logger);

        var result = instanceUnderTest.runEConstraintVariation(null, graph.getStart(), graph, prioritized, limits, criteria, false);

        assertEquals(3, result.size());

        String[] expectedVerticesInPath = {"Vertex0", "Vertex2", "Vertex4", "Vertex5"};

        for(int i = 0; i < result.size(); i++) {
            assertEquals(expectedVerticesInPath[i], result.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath[3], result.get(2).getEndVertex().getLabel());
    }

    @Test
    public void testTopsisDijkstra() {
        var testData = new TestDataCreator();
        var graph = testData.generateMultiParetoGraph();
        var conditions = new ArrayList<Pair<String, Boolean>>();
        conditions.add(new Pair<>(MeasurableValues.TIME.name(), false));
        conditions.add(new Pair<>(MeasurableValues.RAM.name(), false));
        var limits = new HashMap<String, Number>();
        limits.put(MeasurableValues.RAM.name(), 15);

        var instanceUnderTest = new Dijkstra(m_Logger);

        var result = instanceUnderTest.runTopsisVariation(null, graph.getStart(), graph, conditions, limits, false);

        assertEquals(3, result.size());

        String[] expectedVerticesInPath = {"Vertex0", "Vertex2", "Vertex4", "Vertex5"};

        for(int i = 0; i < result.size(); i++) {
            assertEquals(expectedVerticesInPath[i], result.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath[3], result.get(2).getEndVertex().getLabel());

        // ----------------------------------------------------------------------------------------

        var weights1 = new Double[conditions.size()];
        weights1[0] = 1d;
        weights1[1] = 100d;

        result = instanceUnderTest.runTopsisVariation(null, graph.getStart(), graph, conditions, limits, weights1, NormalizationMode.SIMPLE,false);

        assertEquals(3, result.size());

        String[] expectedVerticesInPath2 = {"Vertex0", "Vertex1", "Vertex3", "Vertex5"};

        for(int i = 0; i < result.size(); i++) {
            assertEquals(expectedVerticesInPath2[i], result.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath2[3], result.get(2).getEndVertex().getLabel());

        // ----------------------------------------------------------------------------------------

        var weights2 = new Double[conditions.size()];
        weights2[0] = 100d;
        weights2[1] = 1d;

        result = instanceUnderTest.runTopsisVariation(null, graph.getStart(), graph, conditions, limits, weights2, NormalizationMode.SIMPLE, false);

        assertEquals(3, result.size());

        String[] expectedVerticesInPath3 = {"Vertex0", "Vertex2", "Vertex4", "Vertex5"};

        for(int i = 0; i < result.size(); i++) {
            assertEquals(expectedVerticesInPath3[i], result.get(i).getStartVertex().getLabel());
        }
        assertEquals(expectedVerticesInPath3[3], result.get(2).getEndVertex().getLabel());
    }

    @Test
    public void dijkstraWithOneConditionOnVertex() {
        var testData = new TestDataCreator();
        var graph = testData.generateWithOnlyOneCost();
        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair<>(MeasurableValues.TIME.name(), false));

        var instanceUnderTest = new Dijkstra(m_Logger);
        var result = instanceUnderTest.run(null, graph.getStart(), graph, prioritized);


        assertEquals(2, result.size());
        var path = result.get(0);
        assertEquals("start", path.getEdge().getSource().getLabel());
        assertEquals("A3", path.getEdge().getDestination().getLabel());

        path = result.get(1);
        assertEquals("A3", path.getEdge().getSource().getLabel());
        assertEquals("end", path.getEdge().getDestination().getLabel());

        assertEquals(1, path.getCosts().size());
        assertEquals(5, path.getWeight(MeasurableValues.TIME.name()).getValue());
    }

    @Test
    public void dijkstraWithOneConditionOnNode() {
        var testData = new TestDataCreator();
        var graph = testData.generateGraph();
        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair<>(MeasurableValues.LATENCY.name(), false));

        var instanceUnderTest = new Dijkstra(m_Logger);
        var result = instanceUnderTest.run(null, graph.getStart(), graph, prioritized);

        assertEquals(2, result.size());
        var path = result.get(0);
        assertEquals("start", path.getEdge().getSource().getLabel());
        assertEquals("A3", path.getEdge().getDestination().getLabel());

        path = result.get(1);
        assertEquals("A3", path.getEdge().getSource().getLabel());
        assertEquals("end", path.getEdge().getDestination().getLabel());

        assertEquals(4, path.getCosts().size());
        assertEquals(21, path.getWeight(MeasurableValues.LATENCY.name()).getValue());
        assertEquals(32, path.getWeight(MeasurableValues.TIME.name()).getValue());
        assertEquals(11, path.getWeight(MeasurableValues.CPU.name()).getValue());
        assertEquals(11, path.getWeight(MeasurableValues.RAM.name()).getValue());
    }

    @Test
    public void dijkstraWithTwoConditionOnNode() {
        var testData = new TestDataCreator();
        var graph = testData.generateGraph();
        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair<>(MeasurableValues.CPU.name(), false));
        prioritized.add(new Pair<>(MeasurableValues.TIME.name(), false));

        var instanceUnderTest = new Dijkstra("paretoComparator", m_Logger);
        var result = instanceUnderTest.run(null, graph.getStart(), graph, prioritized);

        //assertEquals(2, result.size());
        var path = result.get(0);
        assertEquals("start", path.getEdge().getSource().getLabel());
        assertEquals("A3", path.getEdge().getDestination().getLabel());

        path = result.get(1);
        assertEquals("A3", path.getEdge().getSource().getLabel());
        assertEquals("end", path.getEdge().getDestination().getLabel());

        assertEquals(4, path.getCosts().size());
        assertEquals(21, path.getWeight(MeasurableValues.LATENCY.name()).getValue());
        assertEquals(32, path.getWeight(MeasurableValues.TIME.name()).getValue());
        assertEquals(11, path.getWeight(MeasurableValues.CPU.name()).getValue());
        assertEquals(11, path.getWeight(MeasurableValues.RAM.name()).getValue());
    }
}
