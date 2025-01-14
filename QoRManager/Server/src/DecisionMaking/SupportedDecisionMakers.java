package DecisionMaking;

/**
 * This class allows to add new decision maker algorithms.
 */
public enum SupportedDecisionMakers {
    TOPSIS("topsis"),
    ECONSTRAINT("EConstraint"),
    MOBIDIC("mobidic");

    private final String _name;
    SupportedDecisionMakers(String nameOfDecisionMaker) {
        _name = nameOfDecisionMaker;
    }

    public boolean equalsName(String name) {
        return _name.equals(name);
    }

    public String toString() {
        return _name;
    }
}
