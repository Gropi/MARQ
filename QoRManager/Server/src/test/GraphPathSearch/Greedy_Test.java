package test.GraphPathSearch;

import GraphPathSearch.Greedy;
import Monitoring.Enums.MeasurableValues;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;
import test.Helper.TestDataCreator;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class Greedy_Test {

    @Test
    public void graphTest() {
        var testData = new TestDataCreator();
        var graph = testData.generateGraph();

        var instanceUnderTest = new Greedy();
        var prioritized = new ArrayList<Pair<String, Boolean>>();
        prioritized.add(new Pair(MeasurableValues.LATENCY.name(), false));
        prioritized.add(new Pair(MeasurableValues.TIME.name(), false));

        var shortestPaths = instanceUnderTest.run(null, graph.getStart(), graph, prioritized);

        assertEquals(3, shortestPaths.size());

        assertEquals("start", shortestPaths.get(0).getEdge().getSource().getLabel());
        assertEquals("A2",  shortestPaths.get(0).getEdge().getDestination().getLabel());
        assertEquals("A2",  shortestPaths.get(1).getEdge().getSource().getLabel());
        assertEquals("A2.2",  shortestPaths.get(1).getEdge().getDestination().getLabel());
        assertEquals("A2.2",  shortestPaths.get(2).getEdge().getSource().getLabel());
        assertEquals("end",  shortestPaths.get(2).getEdge().getDestination().getLabel());
    }
}
