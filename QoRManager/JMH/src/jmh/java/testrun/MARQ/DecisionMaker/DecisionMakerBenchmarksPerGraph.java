package testrun.MARQ.DecisionMaker;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import CreatorTestData.TestGraphCreator;
import DecisionMaking.EdgeDiC.EdgeDiCManager;
import DecisionMaking.MobiDic.MobiDiCManager;
import GraphPathSearch.IShortestPathAlgorithm;
import IO.impl.AccessDrive;
import Monitoring.Enums.MeasurableValues;
import Parser.Graph.GraphOnlineParser;
import Structures.IGraph;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.annotations.Level.Trial;

@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 10, time = 2)
@Fork(10)
@State(Scope.Benchmark)
public class DecisionMakerBenchmarksPerGraph {
    private static final Logger _Logger = LogManager.getLogger("executionLog");

    private List<Pair<String, Boolean>> _Conditions;
    private Double[] _Weights;
    private NormalizationMode _NormalizingMode;
    private Pair<String, Boolean> _MostImportantCriteria;
    private Map<String, Number> _Constraints;

    @State(Scope.Group)
    public static class StateMyLove {
        private List<String> _Files;
        private IGraph _CurrentGraph;
        private int _Index = 0;

        @Setup(Trial)
        public void SetupTest() throws IOException {
            if (_Files == null) {
                var diskhandler = new AccessDrive();
                _Files = diskhandler.listFilesUsingFilesList("E:\\Alibaba\\graphml\\36");
            }
            if ((_Index == 0 || _Index > 3) && _Index < _Files.size()) {
                if (_Index == 0)
                    setCurrentGraph(_Index);
                else
                    setCurrentGraph(_Index - 3);
            }
            _Index++;
        }

        private void setCurrentGraph(int index) {
            var testGraphCreator = new TestGraphCreator(_Logger);
            var graphParser = new GraphOnlineParser(_Logger);
            var file = _Files.get(index);
            _CurrentGraph = testGraphCreator.randomizeGraphCostWithAdvancedParameters(graphParser.loadBaseGraph(file, UUID.randomUUID()));
        }
    }

    @Setup
    public void initTests() {
        Configurator.setAllLevels("executionLog", org.apache.logging.log4j.Level.ERROR);
        Configurator.setAllLevels("measurementLog", org.apache.logging.log4j.Level.ERROR);
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), org.apache.logging.log4j.Level.ERROR);

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
    @Group("averageTimePerGraph")
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void MobiDicAveragePerGraph(StateMyLove state, Blackhole blackhole) {
        var instanceUnderTest = new MobiDiCManager(state._CurrentGraph);
        blackhole.consume(instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null));
    }

    @Benchmark
    @Group("averageTimePerGraph")
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EConstraintAveragePerGraph(StateMyLove state, Blackhole blackhole) {
        var instanceUnderTest = new EdgeDiCManager(state._CurrentGraph, _Conditions, _Constraints, _Weights, _NormalizingMode,
                _MostImportantCriteria, _Logger, IShortestPathAlgorithm.MODE_ECONSTRAINT);
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }

    @Benchmark
    @Group("averageTimePerGraph")
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisAveragePerGraph(StateMyLove state, Blackhole blackhole) {
        var instanceUnderTest = new EdgeDiCManager(state._CurrentGraph, _Conditions, _Constraints, _Weights,
                _NormalizingMode, _MostImportantCriteria,
                _Logger, IShortestPathAlgorithm.MODE_TOPSIS);
        instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);
        blackhole.consume(instanceUnderTest);
    }
}
