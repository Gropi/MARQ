package testrun.MARQ.DecisionMaker;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import CreatorTestData.TestGraphCreator;
import DecisionMaking.EdgeDiC.EdgeDiCManager;
import DecisionMaking.MobiDic.MobiDiCManager;
import IO.impl.AccessDrive;
import Monitoring.Enums.MeasurableValues;
import Parser.Graph.GraphOnlineParser;
import Structures.IGraph;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.annotations.Level.Iteration;

@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 100, time = 2)
@Fork(1)
public class DecisionMakerBenchmarksAlibaba {
    private static final Logger _Logger = LogManager.getLogger("executionLog");

    @State(Scope.Group)
    public static class StateMyLove {
        private List<String> _Files;
        private IGraph _CurrentGraph;
        private TestGraphCreator _TestGraphCreator;
        private GraphOnlineParser _GraphParser;
        private int _Index = 0;

        //Additional information for Topsis and EConstraint
        private List<Pair<String, Boolean>> _Conditions;
        private Double[] _Weights;
        private NormalizationMode _NormalizingMode;
        private Pair<String, Boolean> _MostImportantCriteria;
        private HashMap<String, Number> _Constraints;

        private EdgeDiCManager _EConstraintManager;
        private EdgeDiCManager _TopsisManager;
        private MobiDiCManager _MobiManager;

        @Setup(Iteration)
        public void SetupTest() throws IOException {
            _TestGraphCreator = new TestGraphCreator(_Logger);
            _GraphParser = new GraphOnlineParser(_Logger);
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
            setAdditionalParameters();

            _EConstraintManager = new EdgeDiCManager(_CurrentGraph, _Conditions, _Constraints,
                    null, _NormalizingMode,
                    _MostImportantCriteria, _Logger, "EConstraint");

            _TopsisManager = new EdgeDiCManager(_CurrentGraph, _Conditions, _Constraints, _Weights, _NormalizingMode, _MostImportantCriteria, _Logger, "topsis");

            _MobiManager = new MobiDiCManager(_CurrentGraph);

            _Index++;
        }

        public void setCurrentGraph(int index) {
            System.out.println("New Graph set " + index);
            var file = _Files.get(index);
            _CurrentGraph = _TestGraphCreator.randomizeGraphCostWithAdvancedParameters(
                    _GraphParser.loadBaseGraph(file, UUID.randomUUID()));
        }

        public void setAdditionalParameters() {
            _Conditions = new ArrayList<>();
            _Conditions.add(new Pair<>(MeasurableValues.TIME.name(), false));
            _Conditions.add(new Pair<>(MeasurableValues.ENERGY.name(), false));
            _Conditions.add(new Pair<>(MeasurableValues.QoR.name(), true));

            _Weights = new Double[_Conditions.size()];
            Arrays.fill(_Weights, 1d);

            _NormalizingMode = NormalizationMode.LINEAR;

            _MostImportantCriteria = new Pair<>(MeasurableValues.TIME.name(), false);

            _Constraints = new HashMap<>();
            _Constraints.put(MeasurableValues.TIME.name(), Integer.MAX_VALUE);
        }
    }

    @Benchmark
    @Group("averageTime")
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void MobiDicAverage(StateMyLove state, Blackhole blackhole) {
        blackhole.consume(state._MobiManager.getInitialSelection(Integer.MAX_VALUE, null));
    }

    @Benchmark
    @Group("averageTime")
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EdgeDicEConstraintAverage(StateMyLove state, Blackhole blackhole) {
        blackhole.consume(state._EConstraintManager.getInitialSelection(Integer.MAX_VALUE, null));
    }

    @Benchmark
    @Group("averageTime")
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EdgeDicTopsisAverage(StateMyLove state, Blackhole blackhole) {
        blackhole.consume(state._TopsisManager.getInitialSelection(Integer.MAX_VALUE, null));
    }
}
