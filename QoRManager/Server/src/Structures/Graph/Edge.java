package Structures.Graph;

import Condition.ParameterCost;
import Structures.Graph.interfaces.IVertex;
import Structures.Graph.interfaces.IWeight;

import java.util.*;

/**
 * This class represents an edge between to vertices. The edge is designed to be a model class. There is no "calculation" in it.
 */
public class Edge implements IWeight {
    private final IVertex _source;
    private final IVertex _destination;
    private final UUID _edgeID;
    private final List<ParameterCost> _weights;

    public Edge(IVertex source, IVertex destination, UUID edgeId) {
        if (source == null)
            throw new NullPointerException("The start vertex must not be null.");
        if (destination == null)
            throw new NullPointerException("The end vertex must not be null.");
        if(source.equals(destination))
            throw new IllegalArgumentException("The start vertex must not be the end vertex.");

        _weights = new ArrayList<>();
        _source = source;
        _destination = destination;
        _edgeID = edgeId;
    }

    public IVertex getSource(){
        return _source;
    }

    public IVertex getDestination(){
        return _destination;
    }

    public UUID id(){
        return _edgeID;
    }

    public List<ParameterCost> getWeights() {
        return _weights;
    }

    public ParameterCost getWeight(String nameOfWeight) {
        var costsWithName = _weights.stream().filter(x -> x.getParameterName().equals(nameOfWeight))
                                                                .toList();
        if (!costsWithName.isEmpty())
            return costsWithName.get(0);
        return null;
    }

    @Override
    public void updateWeight(String nameOfWeight, Number weight) {
        var foundWeight = _weights.stream().filter(x -> x.getParameterName().equals(nameOfWeight)).findFirst().orElse(null);
        if (foundWeight != null)
            foundWeight.setValue(weight);
        else {
            var cost = new ParameterCost(weight, nameOfWeight);
            _weights.add(cost);
        }
    }

    @Override
    public Edge clone() {
        var edge = new Edge(_source, _destination, _edgeID);
        for(var weight : _weights)
            edge.updateWeight(weight.getParameterName(), weight.getValue().intValue());
        return edge;
    }

    public Edge clone(IVertex startVertex, IVertex endVertex){
        var newEdge = new Edge(startVertex, endVertex, _edgeID);

        for(var weight : _weights)
            newEdge.updateWeight(weight.getParameterName(), weight.getValue().intValue());

        return newEdge;
    }

    public Edge clone(IVertex startVertex, List<IVertex> alreadyClonedVertices){
        var endVertex = _destination;

        var endAlreadyCloned = false;
        for(var vertex : alreadyClonedVertices) {
            if (vertex.getId() == _destination.getId()){
                endVertex = vertex;
                endAlreadyCloned = true;
            }
        }

        var newEdge = new Edge(startVertex, endAlreadyCloned ? endVertex : endVertex.clone(alreadyClonedVertices), _edgeID);

        for(var weight : _weights)
            newEdge.updateWeight(weight.getParameterName(), weight.getValue().intValue());

        return newEdge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var edge = (Edge) o;
        return _edgeID == edge.id();
    }

    @Override
    public int hashCode() {
        return Objects.hash(_edgeID);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source_id: " + _source.getId() + "source_label: " + _source.getLabel() +
                ", destination_id: " + _destination.getId() + "destination_label: " + _destination.getLabel() +
                ", weight=" + _weights +
                '}';
    }
}
