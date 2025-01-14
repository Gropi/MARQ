package Condition;

/**
 * Defines a cost for a certain parameter. This is needed to be used in graphs for figuring out, how expensive
 * a given path is.
 */
public class ParameterCost {
    private Number _Value;
    private String _ParameterName;

    public Number getValue() {
        return _Value;
    }

    public void setValue(Number value) {
        _Value = value;
    }

    public String getParameterName() {
        return _ParameterName;
    }

    public void setParameterName(String name) {
        _ParameterName = name;
    }

    /**
     * This class allows you store costs for a given string (namely a parameter). The costs have to be
     * represented in int. For the moment you need to change the unit (e.g. nanosecond not second). This has to be
     * the same unit for the same parameters!
     * @param value The value of the cost
     * @param label The label to identify the cost
     */
    public ParameterCost(Number value, String label) {
        _Value = value;
        _ParameterName = label;
    }

    /**
     * Copy constructor. The values of each parameter cost will be summed up.
     * @param copyOne Object one to copy
     * @param copyTwo Object two to copy
     */
    public ParameterCost(ParameterCost copyOne, ParameterCost copyTwo) {
        _ParameterName = copyOne.getParameterName();
        if(copyOne.getValue() instanceof Integer && copyTwo.getValue() instanceof Integer)
        {
            _Value = copyOne.getValue().intValue() + copyTwo.getValue().intValue();
        }
        else if(copyOne.getValue() instanceof Long && copyTwo.getValue() instanceof Long) {
            _Value = copyOne.getValue().longValue() + copyTwo.getValue().longValue();
        }
    }

    public boolean canBeCompared(ParameterCost condition) {
        return condition.getParameterName().equals(_ParameterName);
    }

    public void addCost(ParameterCost costsToAdd) {
        if(_Value instanceof Integer) {
            if(!(costsToAdd.getValue() instanceof  Integer)){
                throw new RuntimeException("Trying to add Non-Integer-Value to Integer-Value.");
            }
            _Value = _Value.intValue() + costsToAdd.getValue().intValue();
        } else if(_Value instanceof Long) {
            if(!(costsToAdd.getValue() instanceof  Long)){
                throw new RuntimeException("Trying to add Non-Long-Value to Long-Value.");
            }
            _Value = _Value.longValue() + costsToAdd.getValue().longValue();
        }
    }

    public void multiplyPercentageCost(ParameterCost costToMultiply) {
        if(_Value instanceof Integer) {
            if(!(costToMultiply.getValue() instanceof Integer)){
                throw new RuntimeException("Trying to add Non-Integer-Value to Integer-Value.");
            }
            _Value = _Value.intValue() * costToMultiply.getValue().intValue()/100;
        } else if(_Value instanceof Long) {
            if(!(costToMultiply.getValue() instanceof Long)){
                throw new RuntimeException("Trying to add Non-Long-Value to Long-Value.");
            }
            _Value = _Value.longValue() * costToMultiply.getValue().longValue()/100;
        } else if(_Value instanceof Double) {
            if(!(costToMultiply.getValue() instanceof Double)){
                throw new RuntimeException("Trying to add Non-Double-Value to Long-Value.");
            }
            _Value = _Value.doubleValue() * costToMultiply.getValue().doubleValue()/100d;
        } else if(_Value instanceof Short ) {
            if(!(costToMultiply.getValue() instanceof Short)){
                throw new RuntimeException("Trying to add Non-Short-Value to Long-Value.");
            }
            _Value = _Value.shortValue() * costToMultiply.getValue().shortValue()/100;
        }

    }

    public ParameterCost Copy() {
        return new ParameterCost(_Value, _ParameterName);
    }
}
