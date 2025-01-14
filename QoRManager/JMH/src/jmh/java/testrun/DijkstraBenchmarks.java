package testrun;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import CreatorTestData.TestGraphCreator;
import DecisionMaking.EdgeDiC.EdgeDiCManager;
import Monitoring.Enums.MeasurableValues;
import Parser.Graph.GraphOnlineParser;
import Structures.IGraph;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Fork(value = 4)
public class DijkstraBenchmarks {
    private static final Logger _Logger = LogManager.getLogger("executionLog");

    @State(Scope.Benchmark)
    public static class GraphState {
        private final IGraph _RandomizedGraphSmall;
        private final IGraph _RandomizedGraphMedium;
        private final IGraph _RandomizedGraphHuge;

        public GraphState() {
            var graphParser = new GraphOnlineParser(_Logger);
            var testGraphCreator = new TestGraphCreator(_Logger);
            _RandomizedGraphSmall = testGraphCreator.randomizeGraphCostWithAdvancedParameters(graphParser.loadBaseGraph("../TestData/Graph/BachelorarbeitGraphen/graph_for_simplifier_flattened.graphml", null));
            _RandomizedGraphMedium = testGraphCreator.randomizeGraphCostWithAdvancedParameters(graphParser.loadBaseGraph("../TestData/Graph/BachelorarbeitGraphen/fourtyNodesTestgraph_flattened.graphml", null));
            _RandomizedGraphHuge = testGraphCreator.randomizeGraphCostWithAdvancedParameters(graphParser.loadBaseGraph("../TestData/Graph/BachelorarbeitGraphen/hugeTestGraph_flattened.graphml", null));
        }
    }

    private List<Pair<String, Boolean>> _Conditions;
    private Double[] _Weights;
    private NormalizationMode _NormalizingMode;
    private Pair<String, Boolean> _MostImportantCriteria;
    private Map<String, Number> _Constraints;

    @Param({"small", "medium", "huge"})
    public String graphSize;

    @Setup
    public void initTests() {
        Configurator.setAllLevels("executionLog", Level.ERROR);
        Configurator.setAllLevels("measurementLog", Level.ERROR);
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);

        _Conditions = new ArrayList<>();
        _Conditions.add(new Pair<>(MeasurableValues.TIME.name(), false));

        _Conditions.add(new Pair<>(MeasurableValues.PARAMETER_1.name(), true));
        _Conditions.add(new Pair<>(MeasurableValues.PARAMETER_2.name(), true));
        _Conditions.add(new Pair<>(MeasurableValues.PARAMETER_3.name(), true));
        _Conditions.add(new Pair<>(MeasurableValues.PARAMETER_4.name(), true));
        _Conditions.add(new Pair<>(MeasurableValues.PARAMETER_5.name(), true));

        _Constraints = new HashMap<>();
        _Constraints.put(MeasurableValues.TIME.name(), 100);

        _Weights = new Double[_Conditions.size()];
        Arrays.fill(_Weights, 1d);

        _NormalizingMode = NormalizationMode.LINEAR;

        _MostImportantCriteria = new Pair<>(MeasurableValues.PARAMETER_1.name(), true);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void DijkstraTopsisBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        IGraph chosenGraph;
        if(graphSize.equalsIgnoreCase("small")) {
            chosenGraph = state._RandomizedGraphSmall;
        } else if(graphSize.equalsIgnoreCase("medium")) {
            chosenGraph = state._RandomizedGraphMedium;
        } else {
            chosenGraph = state._RandomizedGraphHuge;
        }

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights,
                _NormalizingMode, _MostImportantCriteria,
                _Logger, "topsis");
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void DijkstraTopsisMergedEConstraintBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        IGraph chosenGraph;
        if(graphSize.equalsIgnoreCase("small")) {
            chosenGraph = state._RandomizedGraphSmall;
        } else if(graphSize.equalsIgnoreCase("medium")) {
            chosenGraph = state._RandomizedGraphMedium;
        } else {
            chosenGraph = state._RandomizedGraphHuge;
        }

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights, _NormalizingMode, _MostImportantCriteria, _Logger, "topsisMergedEConstraint");
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void DijkstraEConstraintBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        IGraph chosenGraph;
        if(graphSize.equalsIgnoreCase("small")) {
            chosenGraph = state._RandomizedGraphSmall;
        } else if(graphSize.equalsIgnoreCase("medium")) {
            chosenGraph = state._RandomizedGraphMedium;
        } else {
            chosenGraph = state._RandomizedGraphHuge;
        }

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights, _NormalizingMode, _MostImportantCriteria, _Logger, "EConstraint");
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }
}
