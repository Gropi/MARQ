package test.Structures.Graph;

import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Edge;
import Structures.Graph.Vertex;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Edge_Test {
    @Test
    public void idTest() {
        var id = UUID.randomUUID();
        var mockedVertex = mock(Vertex.class);
        var mockedVertex2 = mock(Vertex.class);
        when(mockedVertex.getId()).thenReturn(UUID.randomUUID());
        when(mockedVertex2.getId()).thenReturn(UUID.randomUUID());
        var edge = new Edge(mockedVertex, mockedVertex2, id);

        assertEquals(id, edge.id());
    }

    @Test
    public void vertexInitializer() {
        var mockedVertex = mock(Vertex.class);
        var mockedVertex2 = mock(Vertex.class);
        when(mockedVertex.getId()).thenReturn(UUID.randomUUID());
        when(mockedVertex2.getId()).thenReturn(UUID.randomUUID());
        var edge = new Edge(mockedVertex, mockedVertex2, UUID.randomUUID());

        assertEquals(mockedVertex, edge.getSource());
        assertEquals(mockedVertex2, edge.getDestination());
    }

    @Test
    public void equalsTest() {
        var mockedVertex = mock(Vertex.class);
        var mockedVertex2 = mock(Vertex.class);
        when(mockedVertex.getId()).thenReturn(UUID.randomUUID());
        when(mockedVertex2.getId()).thenReturn(UUID.randomUUID());
        var id = UUID.randomUUID();
        var edge = new Edge(mockedVertex, mockedVertex2, id);
        var edge2 = new Edge(mockedVertex, mockedVertex2, id);
        var edge3 = new Edge(mockedVertex, mockedVertex2, UUID.randomUUID());

        assertEquals(edge, edge);
        assertEquals(edge, edge2);
        assertNotEquals(edge, edge3);
    }

    @Test
    public void weightTest() {
        var mockedVertex = mock(Vertex.class);
        var mockedVertex2 = mock(Vertex.class);
        when(mockedVertex.getId()).thenReturn(UUID.randomUUID());
        when(mockedVertex2.getId()).thenReturn(UUID.randomUUID());
        var edge = new Edge(mockedVertex, mockedVertex2, UUID.randomUUID());

        var testWeight1 = MeasurableValues.LATENCY.name();
        var testWeight2 = MeasurableValues.CPU.name();
        int value1 = 1;
        int value2 = 23;
        edge.updateWeight(testWeight1, value1);
        edge.updateWeight(testWeight2, value2);

        assertNull(edge.getWeight(MeasurableValues.RAM.name()));
        assertEquals(value1, edge.getWeight(testWeight1).getValue());
        assertEquals(value2, edge.getWeight(testWeight2).getValue());

        value1 = 21;
        edge.updateWeight(testWeight1, value1);
        assertEquals(value1, edge.getWeight(testWeight1).getValue());
    }

    @Test
    public void equalsAndCloneTest() {
        var mockedVertex = mock(Vertex.class);
        var mockedVertex2 = mock(Vertex.class);
        when(mockedVertex.getId()).thenReturn(UUID.randomUUID());
        when(mockedVertex2.getId()).thenReturn(UUID.randomUUID());
        var edge = new Edge(mockedVertex, mockedVertex2, UUID.randomUUID());

        var clone = edge.clone();

        assertEquals(clone, edge);
        assertNotSame(clone, edge);
    }
}
