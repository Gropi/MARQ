package Comparator.DecisionAid;

import Helper.NumberHelper;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoundryFilter<T extends IWeight>{

    /** Filters a list of candidates to only keep ones that correspond to criteria in a certain range
     *
     * @param candidates the candidates to be filtered
     * @param criteria the criteria names with additional information of whether the criterion is to be maximized -> boolean true = is to be maximized
     * @param limits the limits that must be kept
     * @return the filtered list of candidates
     */
    public List<T> filter(List<T> candidates, List<Pair<String, Boolean>> criteria, Map<String, Number> limits) {
        var result = new ArrayList<T>();

        for(var candidate : candidates){
            var viable = true;

            for(var criterium : criteria) {
                if(!limits.containsKey(criterium.getFirst())){
                    continue;
                }
                var parameterCost = candidate.getWeight(criterium.getFirst());
                if(parameterCost == null)
                    continue;
                var candidateValue = parameterCost.getValue();
                var r = NumberHelper.compareValues(candidateValue, limits.get(criterium.getFirst()));

                if(!criterium.getSecond() && r > 0 || criterium.getSecond() && r < 0) {
                    viable = false;
                    break;
                }
            }

            if(viable) {
                result.add(candidate);
            }
        }

        return result;
    }
}
