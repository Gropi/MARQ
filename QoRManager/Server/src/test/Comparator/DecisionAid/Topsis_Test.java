package test.Comparator.DecisionAid;

import Comparator.DecisionAid.DataModel.NormalizationMode;
import Comparator.DecisionAid.TOPSIS.Topsis;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static io.github.atomfinger.touuid.UUIDs.toUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Topsis_Test {
    private Logger m_Logger = LogManager.getLogger("measurementLog");

    @Test
    public void testTopsis(){
        var topsis = new Topsis(m_Logger);

        int[][]  alternatives = {
                {8, 7, 2, 1},
                {5, 3, 7, 5},
                {7, 5, 6, 4},
                {9, 9, 7, 3},
                {11, 10, 3, 7},
                {6, 9, 5, 4}
        };

        var criteria = new ArrayList<Pair<String, Boolean>>();
        for(int i = 1; i <= 4; i++) {
            criteria.add(new Pair<>("param" + i, true));
        }

        var actions = new ArrayList<IWeight>();
        for(int i = 0; i < alternatives.length; i++) {
            var id = toUUID(i);
            var vertex = new Vertex("Vertex" + (i + 1), id, "");
            for(int j = 0; j < alternatives[0].length; j++){
                vertex.updateWeight("param" + (j + 1), alternatives[i][j]);
            }
            actions.add(vertex);
        }

        Double[] weights = {0.4, 0.3, 0.1, 0.2};

        var closeness = topsis.getCloseness(actions, criteria, weights, NormalizationMode.VECTOR);

        Double[] expectedValues = {0.387, 0.327, 0.391, 0.615, 0.868, 0.493};

        var df = new DecimalFormat("##0.000");
        for (int i = 0; i < closeness.length; i++) {
            assertEquals(df.format(expectedValues[i]), df.format(closeness[i]));//*1000)/1000d);
        }
    }


}
