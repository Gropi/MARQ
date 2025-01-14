package Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CostHelper {
    public List<ParameterCost> mergeCosts(final List<ParameterCost> costsOne, final List<ParameterCost> costTwo) {
        var mergedList = new ArrayList<ParameterCost>();
        for (var cost : costsOne) {
            mergedList.add(cost.Copy());
        }
        for (var cost : costTwo) {
            var foundEntry = mergedList.stream().filter(x -> x.getParameterName().equals(cost.getParameterName())).toList();
            if (foundEntry.size() == 0) {
                mergedList.add(cost.Copy());
            }
            else {
                var foundCost = foundEntry.get(0);
                var index = mergedList.indexOf(foundCost);
                foundCost.addCost(cost);
                mergedList.set(index, foundCost);
            }
        }
        return mergedList;
    }
}
