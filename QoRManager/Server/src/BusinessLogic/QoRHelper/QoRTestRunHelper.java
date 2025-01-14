package BusinessLogic.QoRHelper;

import ApplicationSupport.IApplicationParameter;

import java.util.Random;

public class QoRTestRunHelper {
    /**
     * This method findes the next deadline within the bounds of the execution.
     * @param appParameter The execution parameters
     * @param currentDeadline The current deadline.
     * @return If there is no next deadline, the return value will be -1, otherwise the deadline.
     */
    public int nextDeadline(IApplicationParameter appParameter, int currentDeadline) {
        var nextDeadline = -1;
        if (appParameter.getFinalDeadline() > 0) {
            if (appParameter.getDeadlineStepSize() > 0) {
                var tempDeadline = currentDeadline + appParameter.getDeadlineStepSize();
                if (currentDeadline >= appParameter.getTestRepeats() - 1) {
                    if (tempDeadline <= appParameter.getFinalDeadline()) {
                        nextDeadline = tempDeadline;
                    }
                }
            } else {
                nextDeadline = new Random().nextInt(appParameter.getInitialDeadline(), appParameter.getFinalDeadline());
            }
        } else {
            nextDeadline = appParameter.getInitialDeadline();
        }
        return nextDeadline;
    }
}
