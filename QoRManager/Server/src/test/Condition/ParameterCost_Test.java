package test.Condition;

import Condition.ParameterCost;
import Monitoring.Enums.MeasurableValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParameterCost_Test {
    @Test
    public void valueTest() {
        int value = 10;
        int value2 = 20;
        var instanceUnderTest = new ParameterCost(value, "LATENCY");
        assertEquals(value, instanceUnderTest.getValue());

        instanceUnderTest.setValue(value2);
        assertEquals(value2, instanceUnderTest.getValue());
    }

    @Test
    public void labelTest() {
        var testWeight1 = "RAM";
        var testWeight2 = "CPU";
        var instanceUnderTest = new ParameterCost(0, testWeight1);
        assertEquals(testWeight1, instanceUnderTest.getParameterName());

        instanceUnderTest.setParameterName(testWeight2);
        assertEquals(testWeight2, instanceUnderTest.getParameterName());
    }
}
