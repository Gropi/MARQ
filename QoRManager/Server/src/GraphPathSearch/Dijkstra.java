package GraphPathSearch;

import Comparator.DecisionAid.BoundryFilter;
import Comparator.DecisionAid.DataModel.NormalizationMode;
import Comparator.DecisionAid.SingleCriteriaOptimizer;
import Comparator.DecisionAid.TOPSIS.Topsis;
import Comparator.LinearComparator;
import Comparator.CostComparator;
import Comparator.ParetoComparator;
import Condition.CostHelper;
import Condition.IWeightCostAggregator;
import Helper.NumberHelper;
import Measurement.MicroserviceChainMeasurement;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Edge;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IVertex;
import Structures.Graph.interfaces.IWeight;
import Structures.IGraph;
import Structures.Path.ShortestPath;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

public class Dijkstra implements IShortestPathAlgorithm{
    private final Logger _Logger;
    private final String _ComparatorToUse;
    private final IWeightCostAggregator _Aggregator;
    private Comparator<IWeight> _Comparator;
    private List<IVertex> _ListOfAlreadyCheckedVertices;
    private Map<String, ShortestPath> _ShortestPath;
    private List<IVertex> _EndVertex;
    private PriorityQueue<ShortestPath> _Queue;

    private List<ShortestPath> _TopsisQueue;
    // -- OLD STUFF --
    private List<IVertex> _ListToCheck;
    private final CostHelper _CostHelper;

    // -- PARETO EXPANSION --
    private Map<String, ArrayList<ShortestPath>> _ShortestPaths;
    private ArrayList<ShortestPath> _FinishedPaths;

    // -- PARALLEL EXPANSION --
    private Map<Integer, Map<Integer, ShortestPath>> _ShortestPathApplication;


    public Dijkstra(Logger logger) {
        this("", logger);
    }

    public Dijkstra(String comparatorToUse, Logger logger){
        this(comparatorToUse, new HashMap<>(), logger);
    }

    public Dijkstra(String comparatorToUse, Map<String, BiFunction<Number, Number, Number>> functions, Logger logger){
        _CostHelper = new CostHelper();
        _ComparatorToUse = comparatorToUse;
        _Aggregator = new IWeightCostAggregator();
        _Logger = logger;

        for(var key : functions.keySet()){
            _Aggregator.addFunction(key, functions.get(key));
        }
    }

    private void initializeSearchRun(List<Pair<String, Boolean>> conditions) {
        initializeComparators(conditions);

        _Queue = new PriorityQueue<>(_Comparator);

        _ShortestPath = new HashMap<>();
        _EndVertex = new LinkedList<>();

        _TopsisQueue = new ArrayList<>();

        // -- OLD --
        _ListToCheck = new LinkedList<>();
        _ListOfAlreadyCheckedVertices = new LinkedList<>();

        // -- EXPANSION --
        _ShortestPathApplication = new HashMap<>();

        _ShortestPaths = new HashMap<>();
        _FinishedPaths = new ArrayList<>();
    }

    @Override
    public List<ShortestPath> run(MicroserviceChainMeasurement measurement, IVertex startPoint, IGraph graph, List<Pair<String, Boolean>> conditions) {
        if(graph.isEndVertex(startPoint))
            return new ArrayList<>();

        initializeSearchRun(conditions);

        _Queue.add(new ShortestPath(new Edge(new Vertex("dummy", null, ""), startPoint, null), startPoint.getWeights()));

        while (!_Queue.isEmpty()) {
            var shortestPath = _Queue.poll();
            var vertex = shortestPath.getEndVertex();

            _ShortestPath.put(vertex.getLabel(), shortestPath);
            checkAllEdgesForVertex(graph, vertex);
            _ListOfAlreadyCheckedVertices.add(vertex);
        }

        return getShortestPathToEnd(measurement);
    }

    @Override
    public List<ShortestPath> run(MicroserviceChainMeasurement measurement, IVertex startPoint, IGraph graph, List<Pair<String, Boolean>> conditions, Map<String, Number> constraints, boolean mustFindSolutionAtAnyCost) {
        if(graph.isEndVertex(startPoint))
            return new ArrayList<>();

        var resultPath = run(measurement, startPoint, graph, conditions);
        return findShortestPath(resultPath, mustFindSolutionAtAnyCost, conditions, constraints);
    }

    //-------------------------- (Helper) Methods for TOPSIS    -----------------

    private void createTOPSISSolution(IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions, Double[] weights, NormalizationMode normalizingMode){

        var topsis = new Topsis(_Logger);

        initializeSearchRun(conditions);

        _TopsisQueue.add(new ShortestPath(new Edge(new Vertex("dummy", null, ""), from, null), from.getWeights()));

        while (!_TopsisQueue.isEmpty()) {
            var shortestPath = (ShortestPath) topsis.getOptimum(_TopsisQueue, conditions, weights, normalizingMode);
            if(_TopsisQueue.size() < 2)
                shortestPath = _TopsisQueue.get(0);
            _TopsisQueue.remove(shortestPath);
            var vertex = shortestPath.getEndVertex();

            _ShortestPath.put(vertex.getLabel(), shortestPath);
            checkAllEdgesForVertexWithTopsis(graph, vertex, topsis);
            _ListOfAlreadyCheckedVertices.add(vertex);
        }
    }

    @Override
    public List<ShortestPath> runTopsisVariation(MicroserviceChainMeasurement measurement, IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions, Map<String, Number> constraints, boolean mustFindSolutionAtAnyCost) {
        var weights = new Double[conditions.size()];
        Arrays.fill(weights, 1d);

        return runTopsisVariation(measurement, from, graph, conditions, constraints, weights, NormalizationMode.SIMPLE, mustFindSolutionAtAnyCost);
    }

    @Override
    public List<ShortestPath> runTopsisVariation(MicroserviceChainMeasurement measurement, IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions,
                                                 Map<String, Number> constraints, Double[] weights, NormalizationMode normalizingMode, boolean mustFindSolutionAtAnyCost) {
        if(graph.isEndVertex(from))
            return new ArrayList<>();

        createTOPSISSolution(from, graph, conditions, weights, normalizingMode);

        var resultPath = getShortestPathToEnd(measurement);
        return findShortestPath(resultPath, mustFindSolutionAtAnyCost, conditions, constraints);
    }

    @Override
    public ArrayList<ShortestPath> runEConstraintVariation(MicroserviceChainMeasurement measurement, IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions, Map<String, Number> constraints, Pair<String, Boolean> criteria, boolean mustFindSolutionAtAnyCost) {
        if(graph.isEndVertex(from))
            return new ArrayList<>();

        var boundryFilter = new BoundryFilter<ShortestPath>();
        var singleCriteriaOptimizer = new SingleCriteriaOptimizer<ShortestPath>();

        var paretoPaths = getParetoHistories(from, graph, conditions);
        var pathsInBoundry = boundryFilter.filter(paretoPaths, conditions, constraints);
        var optimalPaths = singleCriteriaOptimizer.filter(pathsInBoundry, criteria);

        var optimalList = getShortestPathsFromHistories(optimalPaths);

        if(optimalList.size() <= 0) {
            if(mustFindSolutionAtAnyCost) {
                optimalPaths = singleCriteriaOptimizer.filter(paretoPaths, criteria);
                optimalList = getShortestPathsFromHistories(optimalPaths);

            } else {
                return new ArrayList<>();
            }
        }

        if(measurement != null) {
            var takenPathQoR = optimalPaths.get(0).getWeight(MeasurableValues.QoR.name());
            var takenPathTotalTime = optimalPaths.get(0).getWeight(MeasurableValues.TIME.name());

            var qor = takenPathQoR != null ? takenPathQoR.getValue() : 0;

            measurement.addPathQoR(qor.doubleValue());

            if(takenPathTotalTime != null) {
                var time = takenPathTotalTime.getValue();

                if(measurement.getTotalTime() < time.doubleValue())
                    measurement.setTotalTimeTaken(time.doubleValue());
            }
        }

        return optimalList.get(0);
    }

    private List<ShortestPath> findShortestPath(List<ShortestPath> resultPath, boolean mustFindSolutionAtAnyCost, List<Pair<String, Boolean>> conditions,
                                                Map<String, Number> constraints) {
        if(mustFindSolutionAtAnyCost)
            return resultPath;

        var resultingCosts = resultPath.get(resultPath.size()-1);
        var keySet = constraints.keySet();
        for(var criteria : conditions) {
            var nameOfWeight = criteria.getFirst();
            var cost = resultingCosts.getWeight(nameOfWeight);

            if(!keySet.contains(nameOfWeight) || cost == null)
                continue;

            var limit = constraints.get(criteria.getFirst());
            var value = cost.getValue();

            //Choosen path not valid
            if((criteria.getSecond() && NumberHelper.compareValues(limit, value) > 0) || (!criteria.getSecond() && NumberHelper.compareValues(limit, value) < 0)) {
                return new ArrayList<>();
            }
        }

        return resultPath;
    }

    //--------------------------         Helper Methods         -----------------

    private List<ShortestPath> getShortestPathToEnd(MicroserviceChainMeasurement measurement) {
        if(_EndVertex.isEmpty()) {
            _Logger.debug("There was no Endvertex present.");
            return null;
        }
        var endVertex = _EndVertex.get(0);
        var path = new LinkedList<ShortestPath>();

        if(measurement != null) {
            var lastPath = _ShortestPath.get(endVertex.getLabel());

            var takenPathQoR = lastPath.getWeight(MeasurableValues.QoR.name());
            var takenPathTotalTime = lastPath.getWeight(MeasurableValues.TIME.name());

            var qor = takenPathQoR != null ? takenPathQoR.getValue() : 0;

            measurement.addPathQoR(qor.doubleValue());

            if(takenPathTotalTime != null) {
                var time = takenPathTotalTime.getValue();

                if(measurement.getTotalTime() < time.doubleValue())
                    measurement.setTotalTimeTaken(time.doubleValue());
            }
        }

        while(endVertex != null) {
            var shortestPath = _ShortestPath.get(endVertex.getLabel());
            // where at the end if this is true
            var edge = shortestPath.getEdge();
            if (edge == null || edge.id() == null) {
                endVertex = null;
            } else {
                path.add(shortestPath);
                endVertex = shortestPath.getStartVertex();
            }
        }
        // because we iterate backwards (from the end to the start) we need to reverse the list.
        Collections.reverse(path);
        return path;
    }

    private void checkAllEdgesForVertex(IGraph graph, IVertex predecessor) {
        for (var edge : graph.getOutgoingEdges(predecessor)) {
            var nextVertex = edge.getDestination();
            if (graph.isEndVertex(nextVertex) && !_EndVertex.contains(nextVertex)) {
                _EndVertex.add(nextVertex);
            }

            if (!_ShortestPath.containsKey(nextVertex.getLabel())) {
                var mergedPath = getShortestPathFromStartWithLatency(predecessor, edge);
                var pathsToVertex = _Queue.stream().filter(x -> x.getEndVertex().equals(nextVertex)).toList();


                if(pathsToVertex.isEmpty()){
                    _Queue.add(mergedPath);
                } else {
                    var best = pathsToVertex.get(0);
                    if(_Comparator.compare(best, mergedPath) > 0) {
                        _Queue.remove(best);
                        _Queue.add(mergedPath);
                    }

                }
            }
        }
    }

    private void checkAllEdgesForVertexWithTopsis(IGraph graph, IVertex predecessor, Topsis topsis) {
        try {
            for (var edge : graph.getOutgoingEdges(predecessor)) {
                var nextVertex = edge.getDestination();
                if (graph.isEndVertex(nextVertex) && !_EndVertex.contains(nextVertex)) {
                    _EndVertex.add(nextVertex);
                }

                if (!_ShortestPath.containsKey(nextVertex.getLabel())) {
                    var mergedPath = getShortestPathFromStartWithLatency(predecessor, edge);
                    var pathsToVertex = _TopsisQueue.stream().filter(x -> x.getEndVertex().equals(nextVertex)).toList();


                    if(pathsToVertex.isEmpty()){
                        _TopsisQueue.add(mergedPath);
                    } else {
                        var best = pathsToVertex.get(0);
                        var betterChoice = topsis.findBetterChoice(best, mergedPath);
                        if (betterChoice == null) {
                            _Logger.warn("TOPSIS: Warning - An unwanted assumption about the path had to be made. Graph file: " + graph.getGraphLocation());
                        } else if(betterChoice.equals(mergedPath)) {
                            _TopsisQueue.remove(best);
                            _TopsisQueue.add(mergedPath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            _Logger.fatal("There was an exception in check all edges for vertex with TOPSIS. Exception: ", e);
        }
    }

    /**
     * This method has to create the shortest path to an end vertex. This is done by using the cost from the start vertex
     * with the cost of the edge and the end vertex.
     * @param startVertex The vertex you want to start from.
     * @param edgeUnderObservation The edge you want to add.
     * @return The shortest path.
     */
    private ShortestPath getShortestPathFromStart(IVertex startVertex, Edge edgeUnderObservation) {
        var shortestPathToStartVertex = _ShortestPath.get(startVertex.getLabel());
        var mergedNewSection = _Aggregator.directAggregation(edgeUnderObservation.getDestination(), edgeUnderObservation, "VxE");
        var mergedPath =_Aggregator.directAggregation(shortestPathToStartVertex, mergedNewSection, "PxP");

        return (ShortestPath) mergedPath;
    }

    private ShortestPath getShortestPathFromStartWithLatency(IVertex startVertex, Edge edgeUnderObservation) {
        var shortestPathToStartVertex = _ShortestPath.get(startVertex.getLabel());
        var mergedNewSection = _Aggregator.directAggregation(edgeUnderObservation.getDestination(), edgeUnderObservation, "VxE+tl");
        var mergedPath =_Aggregator.directAggregation(shortestPathToStartVertex, mergedNewSection, "PxP");

        return (ShortestPath) mergedPath;
    }

    private ShortestPath getShortestPathContinuation(ShortestPath path, Edge edgeUnderObservation) {
        var mergedNewSection = _Aggregator.directAggregation(edgeUnderObservation.getDestination(), edgeUnderObservation, "VxE+tl");
        var mergedPath =_Aggregator.directAggregation(path, mergedNewSection, "PxP");

        return (ShortestPath) mergedPath;
    }

    private void initializeComparators(List<Pair<String, Boolean>> conditions) {
        if (_ComparatorToUse.equalsIgnoreCase("paretoComparator")) {
            var paretoComparator = new ParetoComparator<IWeight>();

            for (var condition : conditions) {
                paretoComparator.add(new CostComparator(condition));
            }

            _Comparator = paretoComparator;
        } else if(_ComparatorToUse.equalsIgnoreCase("topsisComparator")){
            _Comparator = new LinearComparator("topsis", conditions);
        } else {
            _Comparator = new CostComparator(conditions.get(0));
        }
    }

    private ShortestPath getMinimum(ShortestPath best, ShortestPath candidate) {
        var minimum = _Comparator.compare(best, candidate);
        // IF BOTH ARE OPTIMAL - KEEP THE OLD ONE
        return minimum == 1 ? candidate : best;
    }

    //-------------------------- Methods for pareto optimality  -----------------

    @Override
    public List<ArrayList<ShortestPath>> runParetoPaths(IVertex startPoint, IGraph graph, List<Pair<String, Boolean>> conditions) {
        var pathHistories = getParetoHistories(startPoint, graph, conditions);
        return getShortestPathsFromHistories(pathHistories);
    }

    private ArrayList<ShortestPath> getParetoHistories(IVertex startPoint, IGraph graph, List<Pair<String, Boolean>> conditions) {
        initializeSearchRun(conditions);

        var paretoComparator = new ParetoComparator<ShortestPath>();

        for(var con : conditions) {
            paretoComparator.add(new CostComparator(con));
        }
        _Queue = new PriorityQueue<ShortestPath>(paretoComparator);

        _Queue.add(new ShortestPath(new Edge(new Vertex("dummy", null, ""), startPoint, null), startPoint.getWeights()));

        while (!_Queue.isEmpty()) {
            var shortestPath = _Queue.poll();
            var vertex = shortestPath.getEndVertex();
            var viableConsideration = true;

            var pathsToDelete = new ArrayList<ShortestPath>();

            for(var finishedPath : _FinishedPaths) {
                var comparisionResult = paretoComparator.compare(shortestPath, finishedPath);
                if(comparisionResult < 0) {
                    pathsToDelete.add(finishedPath);
                } else if(comparisionResult > 0) {
                    viableConsideration = false;
                    break;
                }
            }

            if(!viableConsideration) {
                continue;
            }

            if(graph.isEndVertex(vertex)) {
                for(var oldPath : pathsToDelete) {
                    _FinishedPaths.remove(oldPath);
                }
                _FinishedPaths.add(shortestPath);
                continue;
            }

            checkAllParetoEdgesForVertex(paretoComparator, graph, shortestPath);
        }

        return _FinishedPaths;
    }

    private List<ArrayList<ShortestPath>> getShortestPathsFromHistories(List<ShortestPath> pathHistories) {
        var result = new ArrayList<ArrayList<ShortestPath>>();

        for(var finishedPath : pathHistories){
            var list = getPathFromHistory(finishedPath);
            result.add(list);
        }

        return result;
    }

    private ArrayList<ShortestPath> getPathFromHistory(ShortestPath finishedPath) {
        var list = new ArrayList<>(finishedPath.getHistory());
        list.add(new ShortestPath(finishedPath.getEdge(), finishedPath.getCosts()));

        return list;
    }

    private void checkAllParetoEdgesForVertex(ParetoComparator comparator, IGraph graph, ShortestPath path) {
        for (var edge : graph.getOutgoingEdges(path.getEndVertex())) {
            var nextVertex = edge.getDestination();

            var pathsSoFar = _ShortestPaths.get(nextVertex.getLabel());
            if(pathsSoFar == null) {
                pathsSoFar = new ArrayList<>();
                _ShortestPaths.put(nextVertex.getLabel(), pathsSoFar);
            }

            var mergedPath = path.clone();
            if(mergedPath.getEdge().getSource().getLabel().equalsIgnoreCase("dummy")) {
                mergedPath = new ShortestPath(edge, getShortestPathContinuation(path, edge).getCosts());
            } else {
                mergedPath.appendEdge(edge, getShortestPathContinuation(path, edge).getCosts());
            }

            var viableChoice = true;
            var pathsToRemove = new ArrayList<ShortestPath>();

            for(var oldPath : pathsSoFar) {
                var comparisonResult = comparator.compare(mergedPath, oldPath);
                if(comparisonResult == 0) {
                    continue;
                }
                if(comparisonResult > 0) {
                    viableChoice = false;
                    break;
                } else {
                    pathsToRemove.add(oldPath);
                }
            }

            for(var oldPath : pathsToRemove) {
                pathsSoFar.remove(oldPath);
                _Queue.remove(oldPath);
            }
            if(viableChoice) {
                pathsSoFar.add(mergedPath);
                _Queue.add(mergedPath);
            }
        }
    }

    //-------------------------- Methods for parallel execution -----------------

    @Override
    public List<List<ShortestPath>> runAllPaths(IGraph graph, List<Pair<String, Boolean>> conditions, int deadline) {
        initializeSearchRun(conditions);

        var startPoint = graph.getStart();

        _ListToCheck.add(startPoint);
        ShortestPath startShortestPath = new ShortestPath(null, startPoint.getWeights());

        _ShortestPath.put(startPoint.getLabel(), startShortestPath);

        HashMap<Integer, ShortestPath> startVertexMap = new HashMap<>();
        _ShortestPathApplication.put(startPoint.getApplicationIndex(), startVertexMap);

        while (!_ListToCheck.isEmpty()) {
            var copiedList = new ArrayList<>(_ListToCheck);
            var iterator = copiedList.iterator();
            while (iterator.hasNext()) {
                // check all edges of a certain vertex, with the goal to find a shorter path to an end vertex of an edge.
                var vertex = iterator.next();
                checkAllEdgesForVertexExpanded(graph, vertex);
                _ListOfAlreadyCheckedVertices.add(vertex);
                iterator.remove();
                _ListToCheck.remove(vertex);
            }
        }

        var endApplicationIndex = _EndVertex.get(0).getApplicationIndex();
        var shortestPaths = getShortestPaths(endApplicationIndex);
        for (var path : shortestPaths) {
            Collections.reverse(path);
        }
        shortestPaths = adjustPathCosts(shortestPaths);
        if (shortestPaths.stream().map(x -> x.get(x.size() -1)
                        .getWeight(MeasurableValues.TIME.name()).getValue().intValue())
                .mapToInt(x -> x).max().orElseThrow() > deadline) {
            return null;
        }
        return shortestPaths;
    }

    private void checkAllEdgesForVertexExpanded(IGraph graph, IVertex currentVertex) {
        for (var edge : graph.getOutgoingEdges(currentVertex)) {

            ShortestPath shortestPathVertex;
            ShortestPath shortestPathApplication;
            Map<Integer, ShortestPath> shortestPathApplicationMap;

            var nextVertex = edge.getDestination();
            if (graph.isEndVertex(nextVertex) && !_EndVertex.contains(nextVertex)) {
                _EndVertex.add(nextVertex);
            }
            var mergedShortestPath = getShortestPathFromStart(currentVertex, edge);

            // first we check whether the vertex is already checked. If not, we need to check the vertex later.
            if (!_ListOfAlreadyCheckedVertices.contains(nextVertex) && !_ListToCheck.contains(nextVertex)) {
                // if the vertex was not checked and not found yet, we can be sure that the current edge is the shortest!
                _ListToCheck.add(nextVertex);
                // store the shortest path to it, because there is no other until now.
                shortestPathVertex = mergedShortestPath;

                shortestPathApplicationMap = _ShortestPathApplication.get(nextVertex.getApplicationIndex());

                if (shortestPathApplicationMap != null) {
                    shortestPathApplication = shortestPathApplicationMap.get(currentVertex.getApplicationIndex());
                    if (shortestPathApplication != null) {
                        shortestPathApplication = getMinimum(shortestPathApplication, shortestPathVertex);
                    } else {
                        shortestPathApplication = shortestPathVertex;
                    }
                } else {
                    shortestPathApplicationMap = new HashMap<>();
                    _ShortestPathApplication.put(nextVertex.getApplicationIndex(), shortestPathApplicationMap);
                    shortestPathApplication = shortestPathVertex;
                }
            } else {
                var shortestPathToEndVertex = _ShortestPath.get(nextVertex.getLabel());
                shortestPathApplicationMap = _ShortestPathApplication.get(nextVertex.getApplicationIndex());
                // if the vertex was checked, we need to check whether the current path is shorter.
                shortestPathVertex = getMinimum(shortestPathToEndVertex, mergedShortestPath);

                shortestPathApplication = shortestPathApplicationMap.get(currentVertex.getApplicationIndex());
                if (shortestPathApplication != null) {
                    shortestPathApplication = getMinimum(shortestPathApplication, mergedShortestPath);
                } else {
                    shortestPathApplication = mergedShortestPath;
                }
            }
            _ShortestPath.put(edge.getDestination().getLabel(), shortestPathVertex);
            shortestPathApplicationMap.put(currentVertex.getApplicationIndex(), shortestPathApplication);
        }
    }

    private List<List<ShortestPath>> getShortestPaths(Integer endIndex) {
        var incomingPaths = _ShortestPathApplication.get(endIndex).values();
        var result = new ArrayList<List<ShortestPath>>();
        for (var shortestPath : incomingPaths) {
            if (shortestPath.getEdge() != null) {
                List<ShortestPath> newPath = new ArrayList<>();
                newPath.add(shortestPath);
                result.addAll(getShortestPathToEndApplications(newPath, shortestPath.getStartVertex()));
            }
        }
        return result;
    }

    private List<List<ShortestPath>> getShortestPathToEndApplications(List<ShortestPath> path, IVertex vertex) {
        Collection<ShortestPath> incomingPaths = _ShortestPathApplication.get(vertex.getApplicationIndex()).values();
        List<List<ShortestPath>> result = new ArrayList<>();
        if (incomingPaths.size() > 1) {
            for (var shortestPath : incomingPaths) {
                if (shortestPath.getEdge() != null) {
                    List<ShortestPath> newPath = new ArrayList<>(path);
                    newPath.add(shortestPath);
                    result.addAll(getShortestPathToEndApplications(newPath, shortestPath.getStartVertex()));
                } else {
                    result.add(path);
                }
            }
        } else {
            var shortestPath = _ShortestPath.get(vertex.getLabel());
            if (shortestPath.getEdge() != null) {
                List<ShortestPath> newPath = new ArrayList<>(path);
                newPath.add(shortestPath);
                result.addAll(getShortestPathToEndApplications(newPath, shortestPath.getStartVertex()));
            } else {
                result.add(path);
            }
        }
        return result;
    }

    private List<List<ShortestPath>> adjustPathCosts(List<List<ShortestPath>> paths) {
        var adjustedPaths = new ArrayList<List<ShortestPath>>();
        for (var path : paths) {
            var newPath = new ArrayList<ShortestPath>();
            var previousPath = path.get(0);
            newPath.add(path.get(0));
            for (int i = 1; i < path.size(); i++) {
                var edge = path.get(i).getEdge();
                var mergedShortestPath = _CostHelper.mergeCosts(edge.getDestination().getWeights(), edge.getWeights());
                mergedShortestPath = _CostHelper.mergeCosts(previousPath.getCosts(), mergedShortestPath);
                var newShortestPath = new ShortestPath(edge, mergedShortestPath);
                newPath.add(newShortestPath);
                previousPath = newShortestPath;
            }
            adjustedPaths.add(newPath);
        }
        return adjustedPaths;
    }
}
