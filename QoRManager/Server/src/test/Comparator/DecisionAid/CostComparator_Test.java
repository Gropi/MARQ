package test.Comparator.DecisionAid;

import Comparator.CostComparator;
import Condition.ParameterCost;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.exceptions.verification.ArgumentsAreDifferent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CostComparator_Test {
    private MeasurableValues _condition = MeasurableValues.CPU;

    @Test
    public void checkInteger() {
        var instanceUnderTest = new CostComparator( new Pair(_condition.name(), false));

        var condition1 = new ParameterCost(10, _condition.name());
        var condition2 = new ParameterCost(20, _condition.name());
        var condition3 = new ParameterCost(1, _condition.name());

        assertEquals(-1, instanceUnderTest.compare(condition1, condition2));
        assertEquals(1, instanceUnderTest.compare(condition1, condition3));
        assertEquals(0, instanceUnderTest.compare(condition1, condition1));
    }

    @Test
    public void checkIWeight() {
        var mockedWeight1 = mock(IWeight.class);
        var mockedWeight2 = mock(IWeight.class);
        var mockedWeight3 = mock(IWeight.class);

        var condition1 = new ParameterCost(10, _condition.name());
        var condition2 = new ParameterCost(20, _condition.name());
        var condition3 = new ParameterCost(1, _condition.name());

        when(mockedWeight1.getWeight(_condition.name())).thenReturn(condition1);
        when(mockedWeight2.getWeight(_condition.name())).thenReturn(condition2);
        when(mockedWeight3.getWeight(_condition.name())).thenReturn(condition3);

        var instanceUnderTest = new CostComparator(new Pair(_condition.name(), false));

        assertEquals(-1, instanceUnderTest.compare(mockedWeight1, mockedWeight2));
        assertEquals(1, instanceUnderTest.compare(mockedWeight1, mockedWeight3));
        assertEquals(0, instanceUnderTest.compare(mockedWeight1, mockedWeight1));
    }

    @Test
    public void notSupportedType() {
        var instanceUnderTest = new CostComparator(new Pair(_condition.name(), false));

        assertThrows(IllegalArgumentException.class, () -> { instanceUnderTest.compare(10, 20); });
    }
}
