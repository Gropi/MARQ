package test.Comparator.DecisionAid;

import Comparator.DecisionAid.SingleCriteriaOptimizer;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IVertex;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.github.atomfinger.touuid.UUIDs.toUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SingleCriteriaOptimizer_Test {
    private ArrayList<IVertex> createOrderedTestVertices(){

        var list = new ArrayList<IVertex>();

        for(int i = 1; i <= 20; i++) {
            var id = toUUID(i);
            var vertex = new Vertex("Vertex "+i, id, "");

            vertex.updateWeight("parameter1", 50);
            if(i <= 10) {
                vertex.updateWeight("parameter2", 500);
            } else {
                vertex.updateWeight("parameter2", 1000);
            }
            vertex.updateWeight("parameter3", i);

            list.add(vertex);
        }

        return list;
    }


    @Test
    public void checkSingleCriteriaFiltering() {
        var vertices = createOrderedTestVertices();
        var singleCriteriaOptimizer = new SingleCriteriaOptimizer<IVertex>();

        var result = singleCriteriaOptimizer.filter(vertices, new Pair<>("parameter1", false));
        assertEquals(20, result.size());
        for(var vertex : vertices) {
            result.contains(vertex);
        }

        result = singleCriteriaOptimizer.filter(vertices, new Pair<>("parameter2", false));
        assertEquals(10, result.size());
        for (int i = 1; i <= 10; i++) {
            assertTrue(result.contains(vertices.get(i - 1)));
        }
        for (int i = 11; i <= 20; i++) {
            assertTrue(!result.contains(vertices.get(i - 1)));
        }

        result = singleCriteriaOptimizer.filter(vertices, new Pair<>("parameter3", false));
        assertEquals(1, result.size());
        assertTrue(result.contains(vertices.get(0)));
        for (int i = 2; i <= 20; i++) {
            assertTrue(!result.contains(vertices.get(i - 1)));
        }

        var extraVertex = new Vertex("Vertex 21", toUUID(21), "");
        extraVertex.updateWeight("parameter1", 50);
        extraVertex.updateWeight("parameter2", 1000);
        extraVertex.updateWeight("parameter3", 1);
        vertices.add(extraVertex);

        result = singleCriteriaOptimizer.filter(vertices, new Pair<>("parameter3", false));
        assertEquals(2, result.size());
        assertTrue(result.contains(vertices.get(0)));
        assertTrue(result.contains(vertices.get(20)));
        for (int i = 2; i <= 20; i++) {
            assertTrue(!result.contains(vertices.get(i - 1)));
        }

    }
}
