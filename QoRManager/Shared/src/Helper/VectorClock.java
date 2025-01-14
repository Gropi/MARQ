package Helper;

import Network.DataModel.CommunicationMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VectorClock {
    private final Map<String, Integer> _Clock;
    private final String _ProcessKey;

    public VectorClock(String currentProcessKey) {
        this(currentProcessKey, new HashMap<>());
    }

    public VectorClock(String currentProcessKey, VectorClock other) {
        this(currentProcessKey, other.getClock());
    }

    public VectorClock(String currentProcessKey, Map<String, Integer> clock) {
        _ProcessKey = currentProcessKey;
        _Clock = new ConcurrentHashMap<>(clock);
        _Clock.putIfAbsent(currentProcessKey, 1);
    }

    public Map<String, Integer> getClock() {
        return new HashMap<>(_Clock);
    }

    public int getValueForProcessID(String id) {
        return _Clock.getOrDefault(id, -1);
    }

    public String getProcessKey(){
        return _ProcessKey;
    }

    public void addRange(VectorClock clock) {
        if (clock == null)
            throw new NullPointerException("The given vector clock is null.");
        for(var key : clock.getClock().keySet()) {
            _Clock.putIfAbsent(key, clock.getClock().get(key));
        }
    }

    public synchronized void increase() {
        _Clock.computeIfPresent(_ProcessKey, (k, v) -> v + 1);
    }

    public boolean isNewer(VectorClock other) {
        var key = other.getProcessKey();

        if (!_Clock.containsKey(key)) {
            return true;
        }
        else {
            return _Clock.get(key) < other.getClock().get(key);
        }
    }

    public List<CommunicationMessages.ProcessState> toProcessState() {
        synchronized (_Clock) {
            var processStates = new ArrayList<CommunicationMessages.ProcessState>();
            for(var key : _Clock.keySet())
            {
                // #SucheFreunde...
                var processState = CommunicationMessages.ProcessState.newBuilder()
                        // #LadeFreunde...
                        .setKey(key)
                        .setValue(_Clock.get(key))
                        .build();
                processStates.add(processState);
                // #KeinenGefunden---Sorry...
            }
            return processStates;
        }
    }

    public void addSingleProcessState(VectorClock other) {
        var key = other.getProcessKey();
        var otherProcessState = other.getClock().get(key);

        if(!_Clock.containsKey(key) || otherProcessState > _Clock.get(key)) {
            _Clock.put(key, otherProcessState);
        }
    }


    public void addProcessStates(List<CommunicationMessages.ProcessState> processStates) {
        for(var entry : processStates) {
            var key = entry.getKey();
            var value = entry.getValue();

            if (_Clock.containsKey(key)) {
                var oldValue = _Clock.get(key);
                if (oldValue > value) {
                    continue;
                }
                _Clock.put(key, value);
            }
            else {
                _Clock.put(key, value);
            }
        }
    }

    public void addVectorClock(VectorClock other) {
        var otherClock = other.getClock();
        for(var key : otherClock.keySet()) {
            var value = otherClock.get(key);

            if (_Clock.containsKey(key)) {
                var oldValue = _Clock.get(key);
                if (oldValue > value) {
                    continue;
                }
                _Clock.put(key, value);
            }
            else {
                _Clock.put(key, value);
            }
        }
    }
}
