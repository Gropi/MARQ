package Comparator;

import Condition.ParameterCost;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;

import java.util.Comparator;

public class CostComparator implements Comparator {
    private final Pair<String, Boolean> _ConditionToCompare;

    public CostComparator(Pair<String, Boolean> condition) {
        _ConditionToCompare = condition;
    }

    public String getConditionName() {
        return _ConditionToCompare.getFirst();
    }

    /**
     * Compares to costs
     * @param o1 the value you want to check
     * @param o2 the second value you want to check
     * @return the value 0 if cost1 is equal to cost2; a value less than 0 if cost1 is numerically less than cost2;
     * and a value greater than 0 if cost1 is numerically greater than the cost2 (signed comparison).
     */
    @Override
    public int compare(Object o1, Object o2) {
        if (o1.getClass().equals(o2.getClass())) {
            Number o1Value;
            Number o2Value;

            if (o1 instanceof IWeight) {
                var o1Weight = (((IWeight) o1).getWeight(_ConditionToCompare.getFirst()));
                var o2Weight = ((IWeight) o2).getWeight(_ConditionToCompare.getFirst());
                o1Value = o1Weight != null ? o1Weight.getValue() : 0;
                o2Value = o2Weight != null ? o2Weight.getValue() : 0;

            } else if (o1 instanceof ParameterCost) {
                if(!_ConditionToCompare.getFirst().equalsIgnoreCase(((ParameterCost) o1).getParameterName()) || !((ParameterCost) o1).getParameterName().equals(((ParameterCost) o2).getParameterName())) {
                    throw new IllegalArgumentException("Invalid argumenttypes");
                }
                o1Value = o1 != null ? ((ParameterCost) o1).getValue() : 0;
                o2Value = o2 != null ? ((ParameterCost) o2).getValue() : 0;

            } else {
                throw new IllegalArgumentException("The type you use is not supported by the comparer.");
            }

            if(_ConditionToCompare.getSecond()){
                return compareCosts(o2Value, o1Value);
            }
            return compareCosts(o1Value, o2Value);
        }
        throw new IllegalArgumentException("The type you use is not supported by the comparer.");
    }

    /**
     * Compares to costs
     * @param cost1 the value you want to check
     * @param cost2 the second value you want to check
     * @return the value 0 if cost1 is equal to cost2; a value less than 0 if cost1 is numerically less than cost2;
     * and a value greater than 0 if cost1 is numerically greater than the cost2 (signed comparison).
     */
    private int compareCosts(Number cost1, Number cost2) {
        if(cost1 instanceof Integer && cost2 instanceof Integer){
            return Integer.compare(cost1.intValue(), cost2.intValue());
        } else if(cost1 instanceof Short && cost2 instanceof Short){
            return Short.compare(cost1.shortValue(), cost2.shortValue());
        } else if(cost1 instanceof Double && cost2 instanceof Double){
            return Double.compare(cost1.doubleValue(), cost2.doubleValue());
        } else if(cost1 instanceof Long && cost2 instanceof Long) {
            return Long.compare(cost1.longValue(), cost2.longValue());
        }
        throw new IllegalArgumentException("Invalid argumenttypes");
    }
}
