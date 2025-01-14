package Condition;

import Helper.NumberHelper;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Edge;
import Structures.Graph.interfaces.IVertex;
import Structures.Graph.interfaces.IWeight;
import Structures.Path.ShortestPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IWeightCostAggregator {
    Map<String, BiFunction<Number, Number, Number>> _DefaultParameterFunctions;
    Map<String, BiFunction<Number, Number, Number>> _ParameterAggregationFunctions;
    Map<String, Consumer<IWeight>> _DefaultDirectParameterFunctions;
    Map<String, Consumer<IWeight>> _DirectParameterFunctions;
    Map<String, BiFunction<IWeight, IWeight, IWeight>> _DefaultDirectAggregationFunctions;
    Map<String, BiFunction<IWeight, IWeight, IWeight>> _DirectAggregationFunctions;

    public IWeightCostAggregator() {
        _ParameterAggregationFunctions = new HashMap<>();
        _DefaultParameterFunctions = new HashMap<>();
        _DefaultParameterFunctions.put("+", (a, b) -> addNumbers(a, b));
        _DefaultParameterFunctions.put("*", (a, b) -> multiplyNumbers(a, b));

        _DirectAggregationFunctions = new HashMap<>();
        _DefaultDirectAggregationFunctions = new HashMap<>();
        _DefaultDirectAggregationFunctions.put("VxE", (a, b) -> stupidMergeVertexCostsWithEdge(a, b));
        _DefaultDirectAggregationFunctions.put("VxE+tl", (a, b) -> mergeVertexCostsWithEdgeAndTimeLatency(a, b));
        _DefaultDirectAggregationFunctions.put("PxP", (a, b) -> stupidMergePathCosts(a, b));

        _DirectParameterFunctions = new HashMap<>();
        _DefaultDirectParameterFunctions = new HashMap<>();
        _DefaultDirectParameterFunctions.put("txl", (a) -> aggregateTimeAndLatency(a));

    }

    public void addFunction(String name, BiFunction<Number, Number, Number> function) {
        _ParameterAggregationFunctions.put(name, function);
    }

    public void addDirectFunction(String name, BiFunction<IWeight, IWeight, IWeight> function) {
        _DirectAggregationFunctions.put(name, function);
    }

    public void aggregateParameters(List<IWeight> objects, String parameterAggregationFunction, List<String> parametersToAggregate, String targetParameter) {
        if(parametersToAggregate.size() <= 0)
            return;

        for(var object : objects) {
            BiFunction<Number, Number, Number> function;

            if(_ParameterAggregationFunctions.containsKey(parameterAggregationFunction)){
                function = _ParameterAggregationFunctions.get(parameterAggregationFunction);
            }else if(_DefaultParameterFunctions.containsKey(parameterAggregationFunction)){
                function = _DefaultParameterFunctions.get(parameterAggregationFunction);
            }else{
                throw new RuntimeException("Aggregation function unknown.");
            }

            var newParameter = object.getWeight(parametersToAggregate.get(0)).getValue();

            for(int i = 1; i < parametersToAggregate.size(); i++){
                newParameter = function.apply(newParameter, object.getWeight(parametersToAggregate.get(i)).getValue());
            }

            object.updateWeight(targetParameter, newParameter);
        }
    }

    public IWeight aggregateObjects(IWeight object1, IWeight object2, String parameterAggregationFunction, List<String> parametersToAggregate) {
        BiFunction<Number, Number, Number> function;

        if(_ParameterAggregationFunctions.containsKey(parameterAggregationFunction)){
            function = _ParameterAggregationFunctions.get(parameterAggregationFunction);
        }else if(_DefaultParameterFunctions.containsKey(parameterAggregationFunction)){
            function = _DefaultParameterFunctions.get(parameterAggregationFunction);
        }else{
            throw new RuntimeException("Aggregation function unknown.");
        }


        var result = object1;

        for(var parameter : parametersToAggregate) {
            var old = result.getWeight(parameter).getValue();
            var toAggregate = object2.getWeight(parameter).getValue();

            result.updateWeight(parameter, function.apply(old, toAggregate));
        }

        return result;
    }

    public IWeight aggregateObjects(List<IWeight> objects, String parameterAggregationFunction, List<String> parametersToAggregate) {
        if(objects.size() <= 0)
            return null;

        BiFunction<Number, Number, Number> function;

        if(_ParameterAggregationFunctions.containsKey(parameterAggregationFunction)){
            function = _ParameterAggregationFunctions.get(parameterAggregationFunction);
        }else if(_DefaultParameterFunctions.containsKey(parameterAggregationFunction)){
            function = _DefaultParameterFunctions.get(parameterAggregationFunction);
        }else{
            throw new RuntimeException("Aggregation function unknown.");
        }


        var result = objects.get(0);

        for(int i = 1; i < objects.size(); i++) {
            for(var parameter : parametersToAggregate) {
                var old = result.getWeight(parameter).getValue();
                var toAggregate = objects.get(i).getWeight(parameter).getValue();

                result.updateWeight(parameter, function.apply(old, toAggregate));
            }
        }

        return result;
    }

    public IWeight directAggregation(IWeight object1, IWeight object2, String directAggregationFunction){
        BiFunction<IWeight, IWeight, IWeight> function;

        if(_DirectAggregationFunctions.containsKey(directAggregationFunction)){
            function = _DirectAggregationFunctions.get(directAggregationFunction);
        }else if(_DefaultDirectAggregationFunctions.containsKey(directAggregationFunction)){
            function = _DefaultDirectAggregationFunctions.get(directAggregationFunction);
        }else{
            throw new RuntimeException("Aggregation function unknown.");
        }

        return function.apply(object1, object2);
    }

    public IWeight directAggregation(List<IWeight> objects, String directAggregationFunction){
        if(objects.size() <= 0)
            return null;

        BiFunction<IWeight, IWeight, IWeight> function;

        if(_DirectAggregationFunctions.containsKey(directAggregationFunction)){
            function = _DirectAggregationFunctions.get(directAggregationFunction);
        }else if(_DefaultDirectAggregationFunctions.containsKey(directAggregationFunction)){
            function = _DefaultDirectAggregationFunctions.get(directAggregationFunction);
        }else{
            throw new RuntimeException("Aggregation function unknown.");
        }


        var result = objects.get(0);

        for(int i = 1; i < objects.size(); i++) {
            result = function.apply(result, objects.get(i));
        }

        return result;
    }


    //DEFAULT FUNCTIONS...

    //...OVER PARAMETERS:
    private Number addNumbers(Number a, Number b) {
        if(a instanceof Integer && b instanceof Integer)
            return a.intValue() + b.intValue();
        else if (a instanceof Long && b instanceof Long)
            return a.longValue() + b.longValue();
        else if (a instanceof Short && b instanceof Short)
            return a.shortValue() + b.shortValue();
        else if (a instanceof Double && b instanceof Double)
            return a.doubleValue() + b.doubleValue();
        else
            throw new RuntimeException("Parameters invalid. Must be an instace of Number and have the same type.");
    }

    private Number multiplyNumbers(Number a, Number b) {
        if(a instanceof Integer && b instanceof Integer)
            return a.intValue() * b.intValue();
        else if (a instanceof Long && b instanceof Long)
            return a.longValue() * b.longValue();
        else if (a instanceof Short && b instanceof Short)
            return a.shortValue() * b.shortValue();
        else if (a instanceof Double && b instanceof Double)
            return a.doubleValue() * b.doubleValue();
        else
            throw new RuntimeException("Parameters invalid. Must be an instace of Number and have the same type.");
    }

    private void aggregateTimeAndLatency(IWeight instance) {
        var timeWeight = instance.getWeight(MeasurableValues.TIME.name());
        var latencyWeight = instance.getWeight(MeasurableValues.LATENCY.name());

        if(latencyWeight == null) {
            latencyWeight = new ParameterCost(0, MeasurableValues.LATENCY.name());
        }
        if(timeWeight == null) {
            timeWeight = new ParameterCost(0, MeasurableValues.TIME.name());
        }

        var resultingTime = NumberHelper.addValues(timeWeight.getValue(), latencyWeight.getValue());

        instance.updateWeight(MeasurableValues.TIME.name(), resultingTime);
    }

    private IWeight mergeVertexCostsWithEdgeAndTimeLatency(IWeight vertex, IWeight edge) {
        var path = stupidMergeVertexCostsWithEdge(vertex, edge);

        aggregateTimeAndLatency(path);

        return path;
    }

    //...OVER VECTORS
    private IWeight stupidMergeVertexCostsWithEdge(IWeight vertex, IWeight edge) {
        if(!(vertex instanceof IVertex) || !(edge instanceof Edge))
            return null;
        var parameterCosts = new ArrayList<ParameterCost>();

        for (var cost : ((IVertex) vertex).getWeights()) {
            parameterCosts.add(cost.Copy());
        }
        for (var cost : ((Edge) edge).getWeights()) {
            var foundEntry = parameterCosts.stream().filter(x -> x.getParameterName().equals(cost.getParameterName())).toList();
            if (foundEntry.size() == 0) {
                parameterCosts.add(cost.Copy());
            }
            else {
                var foundCost = foundEntry.get(0);
                var index = parameterCosts.indexOf(foundCost);
                foundCost.addCost(cost);
                parameterCosts.set(index, foundCost);
            }
        }

        var path = new ShortestPath((Edge) edge, parameterCosts);

        return path;
    }

    private IWeight stupidMergePathCosts(IWeight path1, IWeight path2) {
        if(!(path1 instanceof ShortestPath) || !(path2 instanceof ShortestPath))
            return null;
        var parameterCosts = new ArrayList<ParameterCost>();

        var costs1 = ((ShortestPath) path1).getCosts();
        var costs2 = ((ShortestPath) path2).getCosts();
        for (var cost : costs1) {
            var newCost = cost.Copy();
            var costParameterName = cost.getParameterName();
            //HAESSLICHER HARDCODE DES TODES ABER 2 TAGE VOR INFOCOM
            if(
                       costParameterName.equalsIgnoreCase(MeasurableValues.QoR.name())
                    || costParameterName.equalsIgnoreCase(MeasurableValues.PARAMETER_1.name())
                    || costParameterName.equalsIgnoreCase(MeasurableValues.PARAMETER_2.name())
                    || costParameterName.equalsIgnoreCase(MeasurableValues.PARAMETER_3.name())
                    || costParameterName.equalsIgnoreCase(MeasurableValues.PARAMETER_4.name())
                    || costParameterName.equalsIgnoreCase(MeasurableValues.PARAMETER_5.name())
            ) {
                var costMultiplier = costs2.stream().filter(x -> x.getParameterName().equalsIgnoreCase(cost.getParameterName())).findFirst().orElse(new ParameterCost(100d, cost.getParameterName()));
                newCost.multiplyPercentageCost(costMultiplier);
                parameterCosts.add(newCost);
                continue;
            }
            newCost.addCost(costs2.stream().filter(x -> x.getParameterName().equalsIgnoreCase(cost.getParameterName())).findFirst().orElse(new ParameterCost(0, cost.getParameterName())));
            parameterCosts.add(newCost);
        }
        for (var cost : ((ShortestPath) path2).getCosts()) {
            if(!parameterCosts.stream().map(x -> x.getParameterName()).collect(Collectors.toList()).contains(cost.getParameterName())){
                var newCost = cost.Copy();
                parameterCosts.add(newCost);
            }
        }
        var path = new ShortestPath(((ShortestPath) path2).getEdge(), parameterCosts);
        return path;
    }

}
