package Structures.Graph.SerialParallel;

import Structures.Graph.interfaces.IVertex;

import java.util.List;

public class SerialParallelPart {
    private final List<IVertex> _serialVertices;
    private final List<IVertex> _parallelVertices;

    public SerialParallelPart(List<IVertex> serialVertices, List<IVertex> parallelVertices) {
        _serialVertices = serialVertices;
        _parallelVertices = parallelVertices;
    }

    public List<IVertex> getSerialVertices() {
        return _serialVertices;
    }

    public List<IVertex> getParallelVertices() {
        return _parallelVertices;
    }
}
