package Comparator.DecisionAid;

import Helper.NumberHelper;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SingleCriteriaOptimizer<T extends IWeight> {

    /** This method selects the optimum out of a list of candidates considering only a single criterion
     *
     * @param candidates a list of candidates
     * @param criteria the criterion with additional information of whether it is to be maximized or minimized
     *                 -> boolean true = is to be maximized
     * @return the optimum out of the candidates considering the criterion
     */
    public List<T> filter(List<T> candidates, Pair<String, Boolean> criteria) {
        var result = new ArrayList<T>();
        if(candidates.size() == 0)
            return result;

        var currentOptimum = candidates.get(0);

        for(var candidate : candidates) {
            var currentValue = currentOptimum.getWeight(criteria.getFirst()).getValue();
            var candidateValue = candidate.getWeight(criteria.getFirst()).getValue();

            var r = NumberHelper.compareValues(candidateValue, currentValue);

            if(!criteria.getSecond() && r < 0 || criteria.getSecond() && r > 0) {
                currentOptimum = candidate;
            }
        }

        for(var candidate : candidates) {
            var currentValue = currentOptimum.getWeight(criteria.getFirst()).getValue();
            var candidateValue = candidate.getWeight(criteria.getFirst()).getValue();

            var r = NumberHelper.compareValues(candidateValue, currentValue);

            if(r == 0) {
                result.add(candidate);
            }
        }

        return result;
    }
}
