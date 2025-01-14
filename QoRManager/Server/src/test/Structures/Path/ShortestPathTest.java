package test.Structures.Path;

import Condition.ParameterCost;
import Structures.Graph.Edge;
import Structures.Path.ShortestPath;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ShortestPathTest {
    @Test
    public void getEdge() {
        var mockedEdge = mock(Edge.class);
        var instanceUnderTest = new ShortestPath(mockedEdge, null);

        assertEquals(mockedEdge, instanceUnderTest.getEdge());
    }

    @Test
    public void getCost() {
        var mockedEdge = mock(Edge.class);
        var mockedParameterCostOne = mock(ParameterCost.class);
        var mockedParameterCostTwo = mock(ParameterCost.class);
        var list = new ArrayList<ParameterCost>();
        list.add(mockedParameterCostOne);
        list.add(mockedParameterCostTwo);
        var instanceUnderTest = new ShortestPath(mockedEdge, list);

        var receivedList = instanceUnderTest.getCosts();
        assertEquals(2, receivedList.size());
        assertTrue(receivedList.contains(mockedParameterCostOne));
        assertTrue(receivedList.contains(mockedParameterCostTwo));
    }
}