package Comparator.DecisionAid;

import Helper.NumberHelper;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ParetoFilter<T extends IWeight> {

    /** This method gets a list of candidates and a list of relevant criteria and filters the Pareto-optimal candidates
     *  according to the criteria
     *
     *  The corresponding boolean to each criterion implies whether the criterion is to be maximized or minimized
     *  -> boolean true = is to be maximized
     *
     * @param candidates the list of candidates that must extend the IWeight interface
     * @param criteria the list of criteria
     * @return a list of Pareto-optima
     */
    public ArrayList<T> findOptima(List<T> candidates, List<Pair<String, Boolean>> criteria) {
        var result = new ArrayList<T>();

        for(var candidate : candidates) {
            var isNewOptimum = true;

            var newResult = new ArrayList<T>();

            for(var priorOptimum : result) {
                var isOptimumCoparedTo = false;
                var oldIsOptimum = false;

                for(var criterium : criteria) {
                    var candidateValue = candidate.getWeight(criterium.getFirst()).getValue();
                    var priorOptimumValue = priorOptimum.getWeight(criterium.getFirst()).getValue();

                    var r = NumberHelper.compareValues(candidateValue, priorOptimumValue);

                    //Es gilt den Parameter zu minimieren
                    if(!criterium.getSecond()) {
                        if(r < 0) {
                            isOptimumCoparedTo = true;
                        } else if (r > 0) {
                            if(!newResult.contains(priorOptimum))
                                newResult.add(priorOptimum);
                        }

                    //Es gilt den Parameter zu maximieren
                    } else {
                        if(r < 0) {
                            if(!newResult.contains(priorOptimum))
                                newResult.add(priorOptimum);
                        } else if (r > 0) {
                            isOptimumCoparedTo = true;
                        }
                    }
                }
                if(!isOptimumCoparedTo)
                    isNewOptimum = false;
            }
            result = newResult;
            if(isNewOptimum)
                result.add(candidate);
        }
        return result;
    }

    public List<T> findMinima(List<T> candidates, List<String> criteria) {
        return  findMaxOrMinimum(candidates, criteria, false);
    }

    public List<T> findMaxima(List<T> candidates, List<String> criteria) {
        return  findMaxOrMinimum(candidates, criteria, true);
    }

    private List<T> findMaxOrMinimum(List<T> candidates, List<String> criteria, boolean findMinima) {
        var newCriteria = new ArrayList<Pair<String, Boolean>>();

        for(var c : criteria) {
            newCriteria.add(new Pair(c, findMinima));
        }

        return findOptima(candidates, newCriteria);
    }
}
