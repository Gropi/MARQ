package test.Comparator.DataModel;

import Comparator.DecisionAid.DataModel.Criteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Criteria_Test {
    @Test
    public void testValidCriteria() {
        String name = "Efficiency";
        boolean maximizeCriteria = true;
        Criteria criteria = new Criteria(name, maximizeCriteria);

        assertEquals(name, criteria.getName());
        assertEquals(maximizeCriteria, criteria.maximizeCriteria());
    }

    @Test
    public void testNameCannotBeNull() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Criteria(null, true);
        });

        assertTrue(thrown.getMessage().contains("name must not be null or empty"));
    }

    @Test
    public void testNameCannotBeEmpty() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Criteria("", true);
        });

        assertTrue(thrown.getMessage().contains("name must not be null or empty"));
    }
}
