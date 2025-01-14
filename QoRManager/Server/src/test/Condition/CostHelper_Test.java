package test.Condition;

import Condition.CostHelper;
import Condition.ParameterCost;
import Monitoring.Enums.MeasurableValues;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CostHelper_Test {
    @Test
    public void CostHelper_mergeCosts() {
        var name1 = MeasurableValues.CPU.name();
        var name2 = MeasurableValues.RAM.name();
        var instanceUnderTest = new CostHelper();

        var parameter1 = new ParameterCost(20, name1);
        var parameter2 = new ParameterCost(10, name1);
        var parameter3 = new ParameterCost(10, name2);

        var listOne = new ArrayList<ParameterCost>();
        var listTwo = new ArrayList<ParameterCost>();

        listOne.add(parameter1);

        listTwo.add(parameter2);
        listTwo.add(parameter3);

        var result = instanceUnderTest.mergeCosts(listOne, listTwo);

        assertEquals(20, listOne.get(0).getValue());
        assertEquals(10, listTwo.get(0).getValue());
        assertEquals(10, listTwo.get(1).getValue());


        assertEquals(2, result.size());
        assertEquals(30, result.get(0).getValue());
        assertEquals(name1, result.get(0).getParameterName());
        assertEquals(10, result.get(1).getValue());
        assertEquals(name2, result.get(1).getParameterName());
    }
}
