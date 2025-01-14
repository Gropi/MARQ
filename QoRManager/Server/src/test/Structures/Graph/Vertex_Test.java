package test.Structures.Graph;

import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Vertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class Vertex_Test {
    private Vertex _InstanceUnderTest;

    @BeforeEach
    public void init() {
        _InstanceUnderTest = new Vertex("", UUID.randomUUID(), "");
    }

    @Test
    public void getWeightsTest() {
        _InstanceUnderTest.updateWeight(MeasurableValues.RAM.name(), 100);
        _InstanceUnderTest.updateWeight(MeasurableValues.CPU.name(), 32);

        var weights1 = _InstanceUnderTest.getWeights();
        var weights2 = _InstanceUnderTest.getWeights();

        assertNotSame(weights1, weights2);

        assertEquals(2, weights1.size());
        assertEquals(MeasurableValues.RAM.name(), weights1.get(0).getParameterName());
        assertEquals(MeasurableValues.CPU.name(), weights1.get(1).getParameterName());
        assertEquals(100, weights1.get(0).getValue());
        assertEquals(32, weights1.get(1).getValue());
    }

    @Test
    public void updateWeightLatencyException() {
        assertThrows(RuntimeException.class, () -> _InstanceUnderTest.updateWeight(MeasurableValues.LATENCY.name(), 100));
    }

    @Test
    public void idTest() {
        var id = UUID.randomUUID();
        var vertex = new Vertex("", id, "");

        assertEquals(id, vertex.getId());
    }

    @Test
    public void equalsTest() {
        var id = UUID.randomUUID();
        var vertex = new Vertex("", id, "");
        var vertex2 = new Vertex("", id, "");
        var vertex3 = new Vertex("", UUID.randomUUID(), "");

        assertEquals(vertex, vertex);
        assertEquals(vertex, vertex2);
        assertNotEquals(vertex, vertex3);
    }

    @Test
    public void getLabelTest() {
        var label = "hduiawd8687";
        var vertex = new Vertex(label, UUID.randomUUID(), "");

        assertEquals(label, vertex.getLabel());
    }
}
