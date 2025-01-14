package test.BusinessLogic.QoRHelper;

import ApplicationSupport.IApplicationParameter;
import BusinessLogic.QoRHelper.QoRTestRunHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QoRTestRunHelper_Test {
    @Test
    public void nextDeadline_withFinalAndSteps_ok() {
        // setup test
        var mockedAppParameter = mock(IApplicationParameter.class);
        when(mockedAppParameter.getFinalDeadline()).thenReturn(3500);
        when(mockedAppParameter.getDeadlineStepSize()).thenReturn(500);

        // setup test instance
        var instanceUnderTest = new QoRTestRunHelper();

        //evaluation
        var nextTime = instanceUnderTest.nextDeadline(mockedAppParameter, 3000);
        assertEquals(3500, nextTime);

        nextTime = instanceUnderTest.nextDeadline(mockedAppParameter, 3500);
        assertEquals(-1, nextTime);
    }

    @Test
    public void nextDeadline_random_ok() {
        // setup test
        var mockedAppParameter = mock(IApplicationParameter.class);
        var maxValue = 3500;
        when(mockedAppParameter.getFinalDeadline()).thenReturn(maxValue);
        when(mockedAppParameter.getInitialDeadline()).thenReturn(3000);

        // setup test instance
        var instanceUnderTest = new QoRTestRunHelper();

        //evaluation
        var nextTime = instanceUnderTest.nextDeadline(mockedAppParameter, 0);
        assertTrue(nextTime > 3000 && nextTime <= maxValue);
    }

    @Test
    public void nextDeadline_noFinalDeadline_ok() {
        // setup test
        var mockedAppParameter = mock(IApplicationParameter.class);
        var initial = 3500;
        when(mockedAppParameter.getInitialDeadline()).thenReturn(initial);

        // setup test instance
        var instanceUnderTest = new QoRTestRunHelper();

        //evaluation
        var nextTime = instanceUnderTest.nextDeadline(mockedAppParameter, 0);
        assertEquals(initial, nextTime);
    }
}
