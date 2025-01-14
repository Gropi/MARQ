package test.GraphPathSearch;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import CreatorTestData.TestGraphCreator;
import DecisionMaking.EdgeDiC.EdgeDiCManager;
import IO.impl.AccessDrive;
import Monitoring.Enums.MeasurableValues;
import Parser.Graph.GraphOnlineParser;
import Structures.Graph.Edge;
import Structures.IGraph;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Dijkstra_Topsis_Alibaba_Test {

    private static final Logger _Logger = LogManager.getLogger("executionLog");

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

    public void setupTest() throws IOException {
        _TestGraphCreator = new TestGraphCreator(_Logger);
        _GraphParser = new GraphOnlineParser(_Logger);
        if (_Files == null) {

            var diskhandler = new AccessDrive();
            _Files = diskhandler.listFilesUsingFilesList("E:\\Alibaba\\graphml\\15");
        }
        if ((_Index == 0 || _Index > 3) && _Index < _Files.size()) {
            if (_Index == 0)
                setCurrentGraph(_Index);
            else
                setCurrentGraph(_Index - 3);
        }
        setAdditionalParameters();
        _Index++;
    }

    public void setCurrentGraph(String file) {
        _CurrentGraph = _TestGraphCreator.randomizeGraphCostWithAdvancedParameters(
                _GraphParser.loadBaseGraph(file, UUID.randomUUID()));
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

        _Weights = new Double[_Conditions.size()];
        Arrays.fill(_Weights, 1d);

        _NormalizingMode = NormalizationMode.LINEAR;

        _MostImportantCriteria = new Pair<>(MeasurableValues.TIME.name(), false);
    }


    public Map<Edge, List<Integer>> getEdgeDicTopsisSelection() {
        var constraints = new HashMap<String, Number>();
        constraints.put(MeasurableValues.TIME.name(), Integer.MAX_VALUE);

        var instanceUnderTest = new EdgeDiCManager(_CurrentGraph, _Conditions, constraints, _Weights, _NormalizingMode, _MostImportantCriteria, _Logger, "topsis");
        var initialSelection = instanceUnderTest.getInitialSelection(Integer.MAX_VALUE, null);

        return initialSelection;
    }

    @Test
    public void testIterations() throws IOException {
        for(int i = 0; i < 105000; i++) {
            setupTest();
            var initialSelection = getEdgeDicTopsisSelection();

            assertFalse(initialSelection.isEmpty());
        }
    }
}
