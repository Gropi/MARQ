package test.EdgeDiC;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import DecisionMaking.EdgeDiC.EdgeDiCManager;
import Monitoring.Enums.MeasurableValues;
import Parser.Graph.GraphOnlineParser;
import CreatorTestData.TestGraphCreator;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EdgeDiCManagerTest {
    private final Logger _Logger = LogManager.getLogger("measurementLog");
    private List<Pair<String, Boolean>> m_Conditions;
    private Pair<String, Boolean> m_MostImportantCriteria;
    private final TestGraphCreator m_GraphRandomizer = new TestGraphCreator(_Logger);

    @BeforeEach
    public void init() {
        Configurator.setAllLevels("executionLog", Level.ERROR);
        Configurator.setAllLevels("measurementLog", Level.ERROR);
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);

        m_Conditions = new ArrayList<>();
        m_Conditions.add(new Pair<>(MeasurableValues.TIME.name(), false));
        m_Conditions.add(new Pair<>(MeasurableValues.LATENCY.name(), false));

        m_MostImportantCriteria = new Pair<>(MeasurableValues.TIME.name(), false);
    }

    @Test
    public void testHugeGraph() {
        var constraints = new HashMap<String, Number>();
        constraints.put(MeasurableValues.TIME.name(), 12221000);

        var randomizedGraph = m_GraphRandomizer.randomizeGraphCost(new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/hugeTestGraph.graphml", null));

        var weights = new Double[m_Conditions.size()];
        Arrays.fill(weights, 1d);

        var instanceUnderTest = new EdgeDiCManager(randomizedGraph, m_Conditions, constraints,weights, NormalizationMode.LINEAR, m_MostImportantCriteria, _Logger, "topsis");
        var edges = instanceUnderTest.getInitialSelection(12000, null);
        assertNotNull(edges);
    }

    @Test
    public void testSmallGraph() {
        var constraints = new HashMap<String, Number>();
        constraints.put(MeasurableValues.TIME.name(), 12000);

        var randomizedGraph = m_GraphRandomizer.randomizeGraphCost(new GraphOnlineParser(_Logger).loadBaseGraph("../TestData/Graph/graph_for_simplifier.graphml", null));

        var weights = new Double[m_Conditions.size()];
        Arrays.fill(weights, 1d);

        var instanceUnderTest = new EdgeDiCManager(randomizedGraph, m_Conditions, constraints, weights, NormalizationMode.LINEAR, m_MostImportantCriteria, _Logger, "topsis");
        var edges = instanceUnderTest.getInitialSelection(12000, null);
        assertNotNull(edges);
    }
}
