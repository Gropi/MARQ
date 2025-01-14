package Comparator;

import Comparator.DecisionAid.TOPSIS.Topsis;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LinearComparator implements Comparator  {
    private final List<Pair<String, Boolean>> _ConditionsToCompare;
    private final String _RatingFunction;

    private final Topsis _Topsis;

    public LinearComparator(String ratingFunction, List<Pair<String, Boolean>> conditions) {
        _RatingFunction = ratingFunction;
        _ConditionsToCompare = conditions;

        var m_Logger = LogManager.getRootLogger();
        _Topsis = new Topsis(m_Logger);
    }

    @Override
    public int compare(Object o1, Object o2) {
        if(!(o1 instanceof IWeight) || !(o2 instanceof IWeight)) {
            throw new RuntimeException("Can only compare instances of IWeight!");
        }
        var alternatives = new ArrayList<IWeight>();
        alternatives.add((IWeight) o1);
        alternatives.add((IWeight) o2);

        if(_RatingFunction.equalsIgnoreCase("topsis")) {
            var ratings = _Topsis.getCloseness(alternatives, _ConditionsToCompare);

            if(ratings[0] == ratings[1]){
                return 0;
            }

            return (ratings[1] - ratings[0]) < 0 ? -1 : 1;
        }

        return 0;
    }
}
