package GraphPathSearch;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import Structures.Graph.interfaces.IVertex;
import Structures.Graph.interfaces.IWeight;
import Measurement.MicroserviceChainMeasurement;
import Structures.IGraph;
import Structures.Path.ShortestPath;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IShortestPathAlgorithm {
     String MODE_TOPSIS = "topsis";
     String MODE_ECONSTRAINT = "EConstraint";

     default List<List<ShortestPath>> runAllPaths(IGraph graph, List<Pair<String, Boolean>> conditions, int deadline) {
          return null;
     }

     default List<ShortestPath> run(MicroserviceChainMeasurement measurement, IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions) {
          return null;
     }

     default List<ShortestPath> run(MicroserviceChainMeasurement measurement, IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions, Map<String, Number> constraints, boolean mustFindSolutionAtAnyCost) {
          return null;
     }

     default List<ShortestPath> run(IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions, List<Function<IWeight, Boolean>> constraints) {
          return null;
     }

     default List<ShortestPath> runTopsisVariation(MicroserviceChainMeasurement measurement,IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions, Map<String, Number> limits, boolean mustFindSolutionAtAnyCost) {
          return null;
     }

     default List<ShortestPath> runTopsisVariation(MicroserviceChainMeasurement measurement, IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions, Map<String, Number> limits, Double[] weights, NormalizationMode normalizingMode, boolean mustFindSolutionAtAnyCost) {
          return null;
     }

     default void resetIterativeWeights(){}

     default ArrayList<ShortestPath> runEConstraintVariation(MicroserviceChainMeasurement measurement, IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions, Map<String, Number> limits, Pair<String, Boolean> criteria, boolean mustFindSolutionAtAnyCost) {
          return null;
     }

     default List<ArrayList<ShortestPath>> runParetoPaths(IVertex from, IGraph graph, List<Pair<String, Boolean>> conditions) {
          return null;
     }
}
