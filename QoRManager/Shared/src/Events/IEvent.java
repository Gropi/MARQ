package Events;

import java.util.ArrayList;

public interface IEvent {
    public ArrayList<Integer> getTerminationConditions();

    public String getEventType();
}
