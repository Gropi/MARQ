package Services;

public enum ServiceState {
    FREE(0), PENDING(1), RUNNING(2), TERMINATED(3);
    final int _Value;

    ServiceState(int value) {
        _Value = value;
    }

    public int getValue(){
        return _Value;
    }
    public static ServiceState of(int value) {
        for (ServiceState serviceState : values()) {
            if (serviceState._Value == value) return serviceState;
        }
        throw new IllegalArgumentException("There is no state for the value  " + value);
    }
}
