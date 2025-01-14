package Comparator.DecisionAid.DataModel;

public class Criteria {
    private final boolean m_MaximizeCriteria;
    private final String m_Name;

    public Criteria(String name, boolean maximizeCriteria) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("The parameter name must not be null or empty.");
        m_Name = name;
        m_MaximizeCriteria = maximizeCriteria;
    }


    public String getName() {
        return m_Name;
    }

    public boolean maximizeCriteria() {
        return m_MaximizeCriteria;
    }
}
