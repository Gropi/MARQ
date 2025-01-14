package test.Comparator.DecisionAid;

import Comparator.DecisionAid.BoundryFilter;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IVertex;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.github.atomfinger.touuid.UUIDs.toUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoundryFilter_Test {

    private ArrayList<IVertex> createOrderedTestVertices(){

        var list = new ArrayList<IVertex>();

        for(int i = 1; i <= 20; i++) {
            var id = toUUID(i);
            var vertex = new Vertex("Vertex "+i, id, "");

            vertex.updateWeight("parameter1", i);
            if(i%2==0) {
                vertex.updateWeight("parameter2", 500);
            } else {
                vertex.updateWeight("parameter2", 1000);
            }
            vertex.updateWeight("parameter3", 50);

            list.add(vertex);
        }

        return list;
    }


    @Test
    public void checkBoundryFiltering() {
        var vertices = createOrderedTestVertices();
        var boundryFilter = new BoundryFilter<IVertex>();
        List<IVertex> result;

        var criteria1 = new ArrayList<Pair<String, Boolean>>();
        criteria1.add(new Pair("parameter1", false));
        criteria1.add(new Pair("parameter2", false));
        criteria1.add(new Pair("parameter3", true));

        var criteria2 = new ArrayList<Pair<String, Boolean>>();
        criteria2.add(new Pair("parameter1", true));
        criteria2.add(new Pair("parameter2", false));
        criteria2.add(new Pair("parameter3", false));

        var limits = new HashMap<String, Number>();


        limits.put("parameter1", 10);
        var result1 = boundryFilter.filter(vertices, criteria1, limits);
        var result2 = boundryFilter.filter(vertices, criteria2, limits);

        assertEquals(10, result1.size());
        assertEquals(11, result2.size());

        for(int i = 1; i < 10; i++) {
            assertTrue(result1.contains(vertices.get(i-1)));
            assertTrue(!result2.contains(vertices.get(i-1)));
        }
        assertTrue(result1.contains(vertices.get(9)));
        assertTrue(result2.contains(vertices.get(9)));
        for(int i = 11; i <= 20; i++) {
            assertTrue(!result1.contains(vertices.get(i-1)));
            assertTrue(result2.contains(vertices.get(i-1)));
        }

        limits.put("parameter2", 501);
        result1 = boundryFilter.filter(vertices, criteria1, limits);
        result2 = boundryFilter.filter(vertices, criteria2, limits);

        assertEquals(5, result1.size());
        assertEquals(6, result2.size());

        for(int i = 1; i < 9; i++) {
            assertTrue(!result1.contains(vertices.get(i-1)));
            assertTrue(!result2.contains(vertices.get(i-1)));
            i++;
            assertTrue(result1.contains(vertices.get(i-1)));
            assertTrue(!result2.contains(vertices.get(i-1)));
        }
        assertTrue(result1.contains(vertices.get(9)));
        assertTrue(result2.contains(vertices.get(9)));
        for(int i = 11; i <= 19; i++) {
            assertTrue(!result1.contains(vertices.get(i - 1)));
            assertTrue(!result2.contains(vertices.get(i - 1)));
            i++;
            assertTrue(!result1.contains(vertices.get(i - 1)));
            assertTrue(result2.contains(vertices.get(i - 1)));
        }

        limits.put("parameter3", 51);
        result1 = boundryFilter.filter(result1, criteria1, limits);
        result2 = boundryFilter.filter(result2, criteria2, limits);

        assertTrue(result1.isEmpty());
        assertEquals(6, result2.size());

        for(int i = 11; i <= 19; i++) {
            assertTrue(!result2.contains(vertices.get(i - 1)));
            i++;
            assertTrue(result2.contains(vertices.get(i - 1)));
        }
    }

}
