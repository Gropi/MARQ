package testrun.MARQ.DecisionMaker;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import DecisionMaking.EdgeDiC.EdgeDiCManager;
import DecisionMaking.MobiDic.MobiDiCManager;
import Monitoring.Enums.MeasurableValues;
import Parser.Graph.GraphOnlineParser;
import Structures.IGraph;
import CreatorTestData.TestGraphCreator;
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
public class DecisionMakerBenchmarksSmall {
    private static final Logger _Logger = LogManager.getLogger("executionLog");

    @State(Scope.Benchmark)
    public static class GraphState {
        private final IGraph m_RandomizedGraphSmall;

        public GraphState() {
            var testGraphCreator = new TestGraphCreator(_Logger);
            var parser = new GraphOnlineParser(_Logger);
            m_RandomizedGraphSmall = testGraphCreator.randomizeGraphCostWithAdvancedParameters(parser.loadBaseGraph("../TestData/Graph/Paper/small.graphml", UUID.randomUUID()));

            parser.saveGraphToXML(m_RandomizedGraphSmall, "logs/JMH/savedGraphs/graph_" + m_RandomizedGraphSmall.getGraphID() + ".graphml");
        }
    }

    private final Logger m_Logger = LogManager.getLogger("measurementLog");
    private List<Pair<String, Boolean>> m_Conditions;
    private Double[] m_Weights;
    private NormalizationMode m_NormalizingMode;
    private Pair<String, Boolean> m_MostImportantCriteria;

    @Setup
    public void initTests() {
        Configurator.setAllLevels("executionLog", Level.ERROR);
        Configurator.setAllLevels("measurementLog", Level.ERROR);
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);

        m_Conditions = new ArrayList<>();
        m_Conditions.add(new Pair<>(MeasurableValues.TIME.name(), false));
        m_Conditions.add(new Pair<>(MeasurableValues.ENERGY.name(), false));

        m_Weights = new Double[m_Conditions.size()];
        Arrays.fill(m_Weights, 1d);

        m_NormalizingMode = NormalizationMode.LINEAR;

        m_MostImportantCriteria = new Pair<>(MeasurableValues.TIME.name(), false);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 40)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void MobiDicAverage(GraphState state, Blackhole blackhole) {
        var instanceUnderTest = new MobiDiCManager(state.m_RandomizedGraphSmall);
        blackhole.consume(instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3)
    @Measurement(iterations = 40)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void MobiDicThroughput(GraphState state, Blackhole blackhole) {
        var instanceUnderTest = new MobiDiCManager(state.m_RandomizedGraphSmall);
        blackhole.consume(instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3)
    @Measurement(iterations = 40)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void EdgeDicEConstraintThroughput(GraphState state, Blackhole blackhole) {
        var constraints = new HashMap<String, Number>();
        constraints.put(MeasurableValues.TIME.name(), Integer.MAX_VALUE);

        var instanceUnderTest = new EdgeDiCManager(state.m_RandomizedGraphSmall, m_Conditions, constraints, null, m_NormalizingMode, m_MostImportantCriteria, _Logger, "EConstraint");
        blackhole.consume(instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 40)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EdgeDicEConstraintAverage(GraphState state, Blackhole blackhole) {
        var constraints = new HashMap<String, Number>();
        constraints.put(MeasurableValues.TIME.name(), Integer.MAX_VALUE);

        var instanceUnderTest = new EdgeDiCManager(state.m_RandomizedGraphSmall, m_Conditions, constraints, null, m_NormalizingMode, m_MostImportantCriteria, _Logger, "EConstraint");
        blackhole.consume(instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3)
    @Measurement(iterations = 40)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void EdgeDicTopsisThroughput(GraphState state, Blackhole blackhole) {

        var constraints = new HashMap<String, Number>();
        constraints.put(MeasurableValues.TIME.name(), Integer.MAX_VALUE);

        var instanceUnderTest = new EdgeDiCManager(state.m_RandomizedGraphSmall, m_Conditions, constraints, m_Weights, m_NormalizingMode, m_MostImportantCriteria, _Logger, "topsis");
        blackhole.consume(instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 40)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EdgeDicTopsisAverage(GraphState state, Blackhole blackhole) {
        var constraints = new HashMap<String, Number>();
        constraints.put(MeasurableValues.TIME.name(), Integer.MAX_VALUE);

        var instanceUnderTest = new EdgeDiCManager(state.m_RandomizedGraphSmall, m_Conditions, constraints, m_Weights, m_NormalizingMode, m_MostImportantCriteria, _Logger, "topsis");
        blackhole.consume(instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null));
    }
}
