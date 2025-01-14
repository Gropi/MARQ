package test.Helper;

import Helper.VectorClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VectorClockTest {
    private final String m_TestKey = "doawhdohaw";
    private VectorClock m_InstanceUnderTest;

    @BeforeEach
    public void init() {
        m_InstanceUnderTest = new VectorClock(m_TestKey);
    }

    @Test
    public void VectorClock_StringConstructor_OK() {
        // Test
        var clock = m_InstanceUnderTest.getClock();

        assertEquals(1, clock.size());
        assertEquals(1, clock.get(m_TestKey));
    }

    @Test
    public void VectorClock_StringAndMapConstructor_OK() {
        var testKey = "DUMMY";
        var localInstanceUnderTest = new VectorClock(testKey, m_InstanceUnderTest.getClock());

        // Test
        var clock = localInstanceUnderTest.getClock();

        assertEquals(2, clock.size());
        assertEquals(1, clock.get(m_TestKey));
        assertEquals(1, clock.get(testKey));
    }

    @Test
    public void VectorClock_increase_OK() {
        var index = 2;
        for (int i = 0; i < 10; i++) {
            m_InstanceUnderTest.increase();
            assertEquals(index, m_InstanceUnderTest.getValueForProcessID(m_TestKey));
            index++;
        }
    }

    @Test
    public void VectorClock_getProcessKey_OK() {
        assertEquals(m_TestKey, m_InstanceUnderTest.getProcessKey());
    }

    @Test
    public void VectorClock_getValueForProcessID_NotFound() {
        assertEquals(-1, m_InstanceUnderTest.getValueForProcessID("uhdawiuhdiuaw"));
    }

    @Test
    public void VectorClock_getValueForProcessID_Found() {
        assertEquals(1, m_InstanceUnderTest.getValueForProcessID(m_TestKey));
    }
}
