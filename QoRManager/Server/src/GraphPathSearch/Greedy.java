package GraphPathSearch;

import Comparator.CostComparator;
import Comparator.ParetoComparator;
import Condition.CostHelper;
import Measurement.MicroserviceChainMeasurement;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Edge;
import Structures.Graph.interfaces.IVertex;
import Structures.Graph.interfaces.IWeight;
import Structures.IGraph;
import Structures.Path.ShortestPath;
import org.apache.commons.math3.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Greedy implements IShortestPathAlgorithm{

    private final CostHelper _CostHelper;

    private ParetoComparator<Edge> _Comparators;

    public Greedy(){
        _CostHelper = new CostHelper();
    }

    @Override
    public List<ShortestPath> run(IVertex startPoint, IGraph graph, List<Pair<String, Boolean>> conditions, List<Function<IWeight, Boolean>> constraints) {
        throw new RuntimeException("Greedy not yet able to handle constraints");
    }

    @Override
    public List<ShortestPath> run(MicroserviceChainMeasurement measurement, IVertex startPoint, IGraph graph, List<Pair<String, Boolean>> conditions) {
        initializeComparators(conditions);
        var path = new LinkedList<ShortestPath>();
        var selectedVertex = startPoint;

        var currentCosts = selectedVertex.getWeights();

        Edge edge = null;
        do {
            edge = getNextEdge(graph, selectedVertex);
            if (edge != null) {
                selectedVertex = edge.getDestination();

                var mergedCosts = _CostHelper.mergeCosts(edge.getDestination().getWeights(), edge.getWeights());
                currentCosts = _CostHelper.mergeCosts(mergedCosts, currentCosts);

                path.add(new ShortestPath(edge, currentCosts));
            }
        } while (edge != null);

        return path;
    }

    private void initializeComparators(List<Pair<String, Boolean>> conditions) {
        _Comparators = new ParetoComparator<Edge>();

        for (var condition : conditions) {
            _Comparators.add(new CostComparator(condition));
        }
    }

    private Edge getNextEdge(IGraph graph, IVertex currentVertex) {
        Edge bestFit = null;
        var edges = graph.getOutgoingEdges(currentVertex);
        for (var entry : edges) {
            if (bestFit == null)
                bestFit = entry;
            else
                bestFit = getMinimum(bestFit, entry);
        }
        return bestFit;
    }

    private Edge getMinimum(Edge currentlyBest, Edge candidate) {
        var minimum = _Comparators.compare(currentlyBest, candidate);
        return minimum == 1 ? candidate : currentlyBest;
    }
}
