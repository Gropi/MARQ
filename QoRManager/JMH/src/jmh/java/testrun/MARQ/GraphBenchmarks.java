package testrun.MARQ;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import CreatorTestData.TestGraphCreator;
import DecisionMaking.EdgeDiC.EdgeDiCManager;
import GraphPathSearch.IShortestPathAlgorithm;
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
public class GraphBenchmarks {
    private static final Logger _Logger = LogManager.getLogger("executionLog");
    @Param({"small", "normal", "big", "huge"})
    public String _GraphSize;

    @State(Scope.Benchmark)
    public static class GraphState {
        private final IGraph m_RandomizedGraphSmall;

        private final IGraph m_RandomizedGraphHuge;

        private final IGraph m_RandomizedGraphNormal;

        private final IGraph m_RandomizedGraphBig;

        public GraphState() {
            var testGraphCreator = new TestGraphCreator(_Logger);
            var parser = new GraphOnlineParser(_Logger);
            m_RandomizedGraphSmall = testGraphCreator.randomizeGraphCostWithAdvancedParameters(parser.loadBaseGraph("../TestData/Graph/Paper/small.graphml", UUID.randomUUID()));
            m_RandomizedGraphNormal = testGraphCreator.randomizeGraphCostWithAdvancedParameters(parser.loadBaseGraph("../TestData/Graph/Paper/normal.graphml", UUID.randomUUID()));
            m_RandomizedGraphBig = testGraphCreator.randomizeGraphCostWithAdvancedParameters(parser.loadBaseGraph("../TestData/Graph/Paper/big.graphml", UUID.randomUUID()));
            m_RandomizedGraphHuge = testGraphCreator.randomizeGraphCostWithAdvancedParameters(parser.loadBaseGraph("../TestData/Graph/Paper/huge.graphml", UUID.randomUUID()));

            parser.saveGraphToXML(m_RandomizedGraphSmall, "logs/JMH/savedGraphs/graph_" + m_RandomizedGraphSmall.getGraphID() + ".graphml");
            parser.saveGraphToXML(m_RandomizedGraphNormal, "logs/JMH/savedGraphs/graph_" + m_RandomizedGraphNormal.getGraphID() + ".graphml");
            parser.saveGraphToXML(m_RandomizedGraphBig, "logs/JMH/savedGraphs/graph_" + m_RandomizedGraphBig.getGraphID() + ".graphml");
            parser.saveGraphToXML(m_RandomizedGraphHuge, "logs/JMH/savedGraphs/graph_" + m_RandomizedGraphHuge.getGraphID() + ".graphml");
        }
    }

    private List<Pair<String, Boolean>> _Conditions;
    private Double[] _Weights;
    private NormalizationMode _NormalizingMode;
    private Pair<String, Boolean> _MostImportantCriteria;
    private Map<String, Number> _Constraints;

    @Setup
    public void initTests() {
        Configurator.setAllLevels("executionLog", Level.ERROR);
        Configurator.setAllLevels("measurementLog", Level.ERROR);
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);

        _Conditions = new ArrayList<>();
        _Conditions.add(new Pair<>(MeasurableValues.TIME.name(), false));
        _Conditions.add(new Pair<>(MeasurableValues.ENERGY.name(), false));
        _Conditions.add(new Pair<>(MeasurableValues.QoR.name(), false));

        _Conditions.add(new Pair<>(MeasurableValues.PARAMETER_1.name(), true));
        _Conditions.add(new Pair<>(MeasurableValues.PARAMETER_2.name(), true));
        _Conditions.add(new Pair<>(MeasurableValues.PARAMETER_3.name(), true));

        _Constraints = new HashMap<>();
        _Constraints.put(MeasurableValues.TIME.name(), 100);

        _Weights = new Double[_Conditions.size()];
        Arrays.fill(_Weights, 1d);

        _NormalizingMode = NormalizationMode.LINEAR;

        _MostImportantCriteria = new Pair<>(MeasurableValues.TIME.name(), true);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void DijkstraTopsisBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        var chosenGraph = getCurrentGraph(state);

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights,
                _NormalizingMode, _MostImportantCriteria,
                _Logger, IShortestPathAlgorithm.MODE_TOPSIS);
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void DijkstraTopsisMergedEConstraintBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        var chosenGraph = getCurrentGraph(state);

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights, _NormalizingMode, _MostImportantCriteria, _Logger, "topsisMergedEConstraint");
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void DijkstraTopsisWithBackupBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        var chosenGraph = getCurrentGraph(state);

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights, _NormalizingMode, _MostImportantCriteria, _Logger, "topsisWithBackup");
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TOPSISIterativeBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        var chosenGraph = getCurrentGraph(state);

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights,
                _NormalizingMode, _MostImportantCriteria,
                _Logger, "topsisIterative");
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void DijkstraEConstraintBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        var chosenGraph = getCurrentGraph(state);

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights, _NormalizingMode,
                _MostImportantCriteria, _Logger, IShortestPathAlgorithm.MODE_ECONSTRAINT);
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void DijkstraLexicographicBenchmarkAverageTime(GraphState state, Blackhole blackhole) {
        var chosenGraph = getCurrentGraph(state);

        var instanceUnderTest = new EdgeDiCManager(chosenGraph, _Conditions, _Constraints, _Weights, _NormalizingMode, _MostImportantCriteria, _Logger, "lexicographic");
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    private IGraph getCurrentGraph(GraphState state) {
        IGraph chosenGraph;
        if(_GraphSize.equalsIgnoreCase("small")) {
            chosenGraph = state.m_RandomizedGraphSmall;
        } else if(_GraphSize.equalsIgnoreCase("normal")) {
            chosenGraph = state.m_RandomizedGraphNormal;
        } else if(_GraphSize.equalsIgnoreCase("big")) {
            chosenGraph = state.m_RandomizedGraphBig;
        } else {
            chosenGraph = state.m_RandomizedGraphHuge;
        }
        return  chosenGraph;
    }
}
