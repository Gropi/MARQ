package Comparator.DecisionAid.TOPSIS;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import Helper.NumberHelper;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Topsis {
    private final Logger m_Logger;
    private List<Pair<String, Boolean>> m_LastCriteria;
    private Double[] m_LastWeights;
    private Number[] m_LastIdealSolution;
    private Number[] m_LastWorstSolution;
    private NormalizationMode m_LastNormalizingMode;

    public Topsis(Logger logger) {
        m_Logger = logger;
    }

    /** Reduced execution of topsis to calculate the better choice out of two given alternatives with using information
     *  of prior executions of topsis
     *
     * @param a one alternative that could be chosen
     * @param b one alternative that could be chosen
     * @return the chosen alternative
     */
    public IWeight findBetterChoice(IWeight a, IWeight b) {
        if(m_LastCriteria == null || m_LastWeights == null || m_LastIdealSolution == null || m_LastWorstSolution == null)
            return null;

        var closeness = new Double[2];
        var weightedParameters = new Double[2][m_LastCriteria.size()];

        var vectornormalizationFactors = new Number[m_LastCriteria.size()];

        for(int c = 0; c < m_LastCriteria.size(); c++) {
            var criteriaName = m_LastCriteria.get(c).getFirst();
            var toMaximize = m_LastCriteria.get(c).getSecond();
            var aWeight = a.getWeight(criteriaName);
            var bWeight = b.getWeight(criteriaName);
            var aValue = aWeight == null ? 0 : aWeight.getValue();
            var bValue = bWeight == null ? 0 : bWeight.getValue();

            //Checks whether one of the alternatives exceeds the stored ideal/anti-ideal solutions and updates them accordingly
            if(toMaximize) {
                if(NumberHelper.compareValues(m_LastIdealSolution[c], aValue) < 0)
                    m_LastIdealSolution[c] = aValue;
                if(NumberHelper.compareValues(m_LastIdealSolution[c], bValue) < 0)
                    m_LastIdealSolution[c] = bValue;
                if(NumberHelper.compareValues(aValue, m_LastWorstSolution[c]) < 0)
                    m_LastWorstSolution[c] = aValue;
                if(NumberHelper.compareValues(bValue, m_LastWorstSolution[c]) < 0)
                    m_LastWorstSolution[c] = bValue;
            } else {
                if(NumberHelper.compareValues(aValue, m_LastIdealSolution[c]) < 0)
                    m_LastIdealSolution[c] = aValue;
                if(NumberHelper.compareValues(bValue, m_LastIdealSolution[c]) < 0)
                    m_LastIdealSolution[c] = bValue;
                if(NumberHelper.compareValues(m_LastWorstSolution[c], aValue) < 0)
                    m_LastWorstSolution[c] = aValue;
                if(NumberHelper.compareValues(m_LastWorstSolution[c], bValue) < 0)
                    m_LastWorstSolution[c] = bValue;
            }

            if(m_LastNormalizingMode == NormalizationMode.VECTOR) {
                var listOfActions = new ArrayList<Number>();

                listOfActions.add(aValue);
                listOfActions.add(bValue);
                if(a != m_LastIdealSolution[c] && b !=  m_LastIdealSolution[c]) {
                    listOfActions.add(m_LastIdealSolution[c]);
                }
                if(a != m_LastWorstSolution[c] && b !=  m_LastWorstSolution[c]) {
                    listOfActions.add(m_LastWorstSolution[c]);
                }

                vectornormalizationFactors[c] = calculateEuclideanFromValues(listOfActions);
            } else {
                vectornormalizationFactors[c] = 1;
            }
        }

        Double normalizedIdealValue = 0d;
        Double normalizedWorstValue = 0d;

        for(int j = 0; j < 2; j++){
            var action = b;
            if(j == 0) {
                action = a;
            }

            var positiveDistance = 0d;
            var negativeDistance = 0d;

            //Iterate through the criteria to do some calculations for each criterion individually before bringing them together
            for(int i = 0; i < m_LastCriteria.size(); i++) {
                var actionWeight = action.getWeight(m_LastCriteria.get(i).getFirst());
                Number actionValue = 0;
                if(actionWeight != null) {
                    actionValue = actionWeight.getValue();
                }

                var normalizedValue = normalizeValue(actionValue, m_LastIdealSolution[i], m_LastWorstSolution[i], vectornormalizationFactors[i],
                        m_LastCriteria.get(i));
                weightedParameters[j][i] = NumberHelper.multiplyNumberWithDouble(normalizedValue, m_LastWeights[i]);

                //Add distance of the given criterion to the overall positive and negative distances
                positiveDistance += Math.pow(weightedParameters[j][i] - normalizedIdealValue, 2);
                negativeDistance += Math.pow(weightedParameters[j][i] - normalizedWorstValue, 2);
            }

            //Now that the squared distance for every criterion has been added to the positive/negative distances all
            // there is left to do is calculating the squareroot
            positiveDistance = Math.sqrt(positiveDistance);
            negativeDistance = Math.sqrt(negativeDistance);

            // Given the positive/negative distance calculate the closeness of the alternative
            closeness[j] = negativeDistance/(negativeDistance + positiveDistance);
        }

        //Compare the closeness of both alternatives and return the better choice
        return (closeness[0] > closeness[1]) ? a : b;
    }

    private Double normalizeValue(Number valueToNormalize, Number idealSolution, Number worstSolution, Number vectornormalizationFactors, Pair<String, Boolean> criteria) {
        var normalizedValue = 0d;
        if(m_LastNormalizingMode == NormalizationMode.VECTOR) {
            normalizedValue = NumberHelper.divideValuesAsDoubles(valueToNormalize, vectornormalizationFactors);

        } else if(m_LastNormalizingMode == NormalizationMode.LINEAR) {
            if(criteria.getSecond()) {
                normalizedValue = NumberHelper.divideValuesAsDoubles(NumberHelper.subtractValues(valueToNormalize, worstSolution),
                        NumberHelper.subtractValues(idealSolution, worstSolution));
            } else {
                normalizedValue = NumberHelper.divideValuesAsDoubles(NumberHelper.subtractValues(worstSolution, valueToNormalize),
                        NumberHelper.subtractValues(worstSolution, idealSolution));
            }

        } else if(m_LastNormalizingMode == NormalizationMode.SIMPLE) {
            if(criteria.getSecond()) {
                normalizedValue = NumberHelper.divideValuesAsDoubles(valueToNormalize, idealSolution);
            } else {
                normalizedValue = NumberHelper.divideValuesAsDoubles(idealSolution, valueToNormalize);
            }

        }  else {
            throw new RuntimeException("Choose valid normalizing mode! 'vector', 'linear' or 'simple'.");
        }

        return normalizedValue;
    }

    /** Method to choose the optimum out of a set of given alternatives considering certain weighted criteria
     *
     * @param actions the set of alternatives (called actions in the original publication)
     * @param criteria the set of criteria the alternatives should be compared by. The boolean gives additional
     *                information of whether the criterion is to be maximized or minimized
     *                -> boolean true = is to be maximized
     * @param weights list of weights to prioritize certain criteria over others - the size of the weight shows the
     *               impact of the criterion on the choice
     * @param normalizingMode chosen way of normalization in the topsis algorithm
     * @return the chosen optimum of the alternatives calculated given the criteria and the weights
     */
    public IWeight getOptimum(List<? extends IWeight> actions, List<Pair<String, Boolean>> criteria,  Double[] weights, NormalizationMode normalizingMode) {
        m_LastNormalizingMode = normalizingMode;

        if(actions.isEmpty())
            return null;
        if(actions.size() < 2)
            return actions.get(0);

        //First the closeness of every alternative must be calculated
        var closeness = getCloseness(actions, criteria, weights, normalizingMode);
        var chosenIndex = 0;
        var chosenValue = closeness[0];

        //Then simply choose the alternative with the highest closeness
        for(int i = 1; i < closeness.length; i++) {
            if(closeness[i] > chosenValue) {
                chosenValue = closeness[i];
                chosenIndex = i;
            }
        }

        return actions.get(chosenIndex);
    }

    /** Helper method to fill in missing information and call the actual getOptimum method
     *
     * @param actions the alternatives to be compared (named actions in original publication)
     * @param criteria the criteria the alternatives should be compared according to with additional information of whether
     *                the criterion is to be maximized or minimized -> boolean true = is to be maximized
     * @return the optimum of the alternatives calculated given the criteria
     */
    public IWeight getOptimum(List<? extends IWeight> actions, List<Pair<String, Boolean>> criteria) {
        var weights = new Double[criteria.size()];

        Arrays.fill(weights, 1d);

        return getOptimum(actions, criteria, weights, NormalizationMode.SIMPLE);
    }

    /** Helper method to fill in missing information and call the actual getOptimum method
     *
     * @param actions the alternatives to be compared (named actions in original publication)
     * @param criteria the criteria the alternatives should be compared according to with additional information of whether
     * @param weights weights to prioritize certian criteria over other
     * @return the optimum of the alternatives calculated given the criteria and the weights
     */
    public IWeight getOptimum(List<? extends IWeight> actions, List<Pair<String, Boolean>> criteria, Double[] weights) {
        return getOptimum(actions, criteria, weights, NormalizationMode.SIMPLE);
    }

    /** Helper method to fill in missing information and call the actual getCloseness method
     *
     * @param actions the set of alternatives (named actions in original publication) to get the closeness for
     * @param criteria the criteria the relative closeness should be calculated with
     * @return a list of the relative closeness for every given alternative
     */
    public Double[] getCloseness(List<? extends IWeight> actions, List<Pair<String, Boolean>> criteria) {
        var weights = new Double[criteria.size()];
        Arrays.fill(weights, 1d);

        return getCloseness(actions, criteria, weights, NormalizationMode.SIMPLE);
    }

    /** Method to calculate the relative closeness of a set of alternatives (named actions in original publication) to a
     *  constructed ideal and anti-ideal solution.
     *
     * @param actions the set of alternatives (named actions in original publication)
     * @param criteria a list of criteria the relative closeness is calculated with
     * @param weights a list of weights to regulate the impact of single criteria and prioritize one over the other
     * @param normalizingMode the chosen mode of normalization for topsis
     * @return a list of the relative closeness for every given alternative
     */
    public Double[] getCloseness(List<? extends IWeight> actions, List<Pair<String, Boolean>> criteria,  Double[] weights, NormalizationMode normalizingMode) {
        m_LastNormalizingMode = normalizingMode;

        // If there is less than 2 alternatives constructing an ideal/anti-ideal solution is pointless/impossible
        if(actions.size() < 2)
            return null;

        var weightedParameters = new Double[actions.size()][criteria.size()];
        var idealSolution = new Double[criteria.size()];
        var worstSolution = new Double[criteria.size()];
        m_LastIdealSolution = new Number[criteria.size()];
        m_LastWorstSolution = new Number[criteria.size()];
        var closeness = new Double[actions.size()];

        // handle one criterion after the other
        for(int i = 0; i < criteria.size(); i++) {
           var c = criteria.get(i);
           Number bestValue;
           Number worstValue;

           var cost = actions.get(0).getWeight(c.getFirst());
           if(cost != null) {
               bestValue = cost.getValue();
               worstValue = cost.getValue();
           } else {
               if(c.getSecond()) {
                   bestValue = Integer.MIN_VALUE;
                   worstValue = Integer.MAX_VALUE;
               } else {
                   bestValue = Integer.MAX_VALUE;
                   worstValue = Integer.MIN_VALUE;
               }
           }

           for (var candidate : actions) {
               var candidateWeight = candidate.getWeight(c.getFirst());
               var candidateValue = candidateWeight == null ? 0 : candidateWeight.getValue();

               if (c.getSecond() && NumberHelper.compareValues(candidateValue, bestValue) > 0 || !c.getSecond() && NumberHelper.compareValues(candidateValue, bestValue) < 0) {
                   bestValue = candidateValue;
               }

               if (c.getSecond() && NumberHelper.compareValues(candidateValue, worstValue) < 0 || !c.getSecond() && NumberHelper.compareValues(candidateValue, worstValue) > 0) {
                   worstValue = candidateValue;
               }
           }

           // store the best/worst value for the handled criterion
           m_LastIdealSolution[i] = bestValue;
           m_LastWorstSolution[i] = worstValue;

           Number vectornormalizationFactor = 1;
           if(normalizingMode == NormalizationMode.VECTOR) {
               vectornormalizationFactor = calculateEuclidean(actions, c);
           }

           //Normalize and weight the parameters for the given criterion across all alternatives
           for(int j = 0; j < actions.size(); j++){
               var actionsWeight = actions.get(j).getWeight(c.getFirst());
               var actionsValue = actionsWeight == null ? 0 : actionsWeight.getValue();

               //NORMALIZATION:
               var normalizedValue = normalizeValue(actionsValue, bestValue, worstValue, vectornormalizationFactor, c);
               //Weight and store the normalized value
               weightedParameters[j][i] = NumberHelper.multiplyNumberWithDouble(normalizedValue, weights[i]);
           }

           if(normalizingMode == NormalizationMode.VECTOR) {
               idealSolution[i] = NumberHelper.multiplyNumberWithDouble(NumberHelper.divideValuesAsDoubles(bestValue, vectornormalizationFactor), weights[i]);
               worstSolution[i] = NumberHelper.multiplyNumberWithDouble(NumberHelper.divideValuesAsDoubles(worstValue, vectornormalizationFactor), weights[i]);
           } else {
               idealSolution[i] = weights[i];

               if(normalizingMode == NormalizationMode.LINEAR) {
                   worstSolution[i] = 0d;
               } else if(normalizingMode == NormalizationMode.SIMPLE){
                   if(c.getSecond()) {
                       worstSolution[i] = NumberHelper.multiplyNumberWithDouble(NumberHelper.divideValuesAsDoubles(worstValue, bestValue), weights[i]);
                   } else {
                       worstSolution[i] = NumberHelper.multiplyNumberWithDouble(NumberHelper.divideValuesAsDoubles(bestValue, worstValue), weights[i]);
                   }
               }
           }
       }

       //Calculate the relative distance for each alternative using the euclidean distance to the weighted normalized
       // worst/best solution
       for(int j = 0; j < actions.size(); j++){
           var positiveDistance = 0d;
           var negativeDistance = 0d;

           for(int i = 0; i < criteria.size(); i++) {
               positiveDistance += Math.pow(weightedParameters[j][i] - idealSolution[i], 2);
               negativeDistance += Math.pow(weightedParameters[j][i] - worstSolution[i], 2);
           }

           positiveDistance = Math.sqrt(positiveDistance);
           negativeDistance = Math.sqrt(negativeDistance);

           closeness[j] = negativeDistance/(negativeDistance + positiveDistance);
       }

       m_LastCriteria = criteria;
       m_LastWeights = weights;

       return closeness;
    }

    /** Helper method to calculate the euclidean of a set of parameters that correspond a specific criterion and to a
     *  list of alternatives
     *
     * @param actions the list of alternatives (named actions in original publication)
     * @param criteria the specific criterion to determine the parameters by
     * @return the euclidean
     */
    private Number calculateEuclidean(List<? extends IWeight> actions, Pair<String, Boolean> criteria) {
        var f = Math.pow(actions.get(0).getWeight(criteria.getFirst()).getValue().doubleValue(), 2);
        for (int j = 1; j < actions.size(); j++){
            f += Math.pow(actions.get(j).getWeight(criteria.getFirst()).getValue().doubleValue(), 2);
        }
        return Math.sqrt(f);
    }

    /** Helper method to calculate the euclidean of a set of parameters that correspond a specific criterion and to a
     *  list of alternatives
     *
     * @param actions the list of alternatives (named actions in original publication)
     * @return the euclidean
     */
    private Number calculateEuclideanFromValues(List<Number> actions) {
        var f = Math.pow(actions.get(0).doubleValue(), 2);
        for (int j = 1; j < actions.size(); j++){
            f += Math.pow(actions.get(j).doubleValue(), 2);
        }
        return Math.sqrt(f);
    }
}
