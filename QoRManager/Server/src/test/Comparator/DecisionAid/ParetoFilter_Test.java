package test.Comparator.DecisionAid;

import Comparator.DecisionAid.ParetoFilter;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IVertex;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.atomfinger.touuid.UUIDs.toUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParetoFilter_Test {

    private ArrayList<IVertex> createOrderedTestVertices(){

        var list = new ArrayList<IVertex>();

        for(int i = 1; i <= 20; i++) {
            var id = toUUID(i);
            var vertex = new Vertex("Vertex "+i, id, "");
            vertex.updateWeight("parameter1", i);
            vertex.updateWeight("parameter2", 21 - i);

            if(i <= 10) {
                vertex.updateWeight("parameter3", 1000);
            } else {
                vertex.updateWeight("parameter3", 500);
            }

            list.add(vertex);
        }

        return list;
    }

    @Test
    public void CheckMinimaSearch() {
        var vertices = createOrderedTestVertices();
        var paretoFilter = new ParetoFilter<IVertex>();
        List<IVertex> result;

        var criteria = new ArrayList<String>();
        criteria.add("parameter1");

        result = paretoFilter.findMinima(vertices, criteria);
        assertEquals(1, result.size());
        assertTrue(result.contains(vertices.get(0)));

        criteria.add("parameter3");
        result = paretoFilter.findMinima(vertices, criteria);
        assertEquals(2, result.size());
        assertTrue(result.contains(vertices.get(0)));
        assertTrue(result.contains(vertices.get(10)));

        criteria.add("parameter2");
        result = paretoFilter.findMinima(vertices, criteria);
        assertEquals(20, result.size());
        for(var vertex : vertices){
            assertTrue(result.contains(vertex));
        }
    }

    @Test
    public void CheckMaximaSearch() {
        var vertices = createOrderedTestVertices();
        var paretoFilter = new ParetoFilter<IVertex>();
        List<IVertex> result;

        var criteria = new ArrayList<String>();
        criteria.add("parameter1");

        result = paretoFilter.findMaxima(vertices, criteria);
        assertEquals(1, result.size());
        assertTrue(result.contains(vertices.get(19)));

        criteria.add("parameter3");
        result = paretoFilter.findMaxima(vertices, criteria);
        assertEquals(2, result.size());
        assertTrue(result.contains(vertices.get(19)));
        assertTrue(result.contains(vertices.get(9)));

        criteria.add("parameter2");
        result = paretoFilter.findMaxima(vertices, criteria);
        assertEquals(20, result.size());
        for(var vertex : vertices){
            assertTrue(result.contains(vertex));
        }
    }

    @Test
    public void CheckOptimaSearch() {
        var vertices = createOrderedTestVertices();
        var paretoFilter = new ParetoFilter<IVertex>();
        List<IVertex> result;

        var criteria = new ArrayList<Pair<String, Boolean>>();
        criteria.add(new Pair("parameter1", true));

        result = paretoFilter.findOptima(vertices, criteria);
        assertEquals(1, result.size());
        assertTrue(result.contains(vertices.get(19)));

        criteria.add(new Pair("parameter2", false));

        result = paretoFilter.findOptima(vertices, criteria);
        assertEquals(1, result.size());
        assertTrue(result.contains(vertices.get(19)));

        criteria.add(new Pair("parameter3", true));
        result = paretoFilter.findOptima(vertices, criteria);
        assertEquals(2, result.size());
        assertTrue(result.contains(vertices.get(19)));
        assertTrue(result.contains(vertices.get(9)));

    }

}
