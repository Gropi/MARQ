package Comparator;

import Structures.Graph.interfaces.IWeight;

import java.util.*;

public class PrioritizedComparator<T extends IWeight> implements Comparator<T> {
    private List<String> _Prioritization;
    private HashMap<String, CostComparator> _Comparators;

    public void add(CostComparator newComparator){
        _Comparators.put(newComparator.getConditionName(), newComparator);
    }

    public PrioritizedComparator(List<String> prioritization) {
        if (prioritization == null)
            throw new NullPointerException("you have to give a prio list.");
        _Prioritization = prioritization;
        _Comparators = new HashMap<>();
    }

    public void UpdatePrioritization(LinkedList<String> prioritization) {
        if (prioritization != null)
            _Prioritization = prioritization;
    }

    @Override
    public int compare(T objectToCompare1, T objectToCompare2) {
        for(var prio : _Prioritization){
            var resultForCriteria = (int) Math.signum(_Comparators.get(prio).compare(objectToCompare1, objectToCompare2));

            if(resultForCriteria != 0) {
                return resultForCriteria;
            }
        }
        return 0;
    }
}
