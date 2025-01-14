package Structures.Path;

import Condition.ParameterCost;
import Monitoring.Enums.MeasurableValues;
import Structures.Graph.Edge;
import Structures.Graph.interfaces.IVertex;
import Structures.Graph.interfaces.IWeight;

import java.util.ArrayList;
import java.util.List;

public class ShortestPath implements IWeight {
    private Edge _Edge;
    private List<ShortestPath> _History;
    private List<ParameterCost> _Cost;

    public ShortestPath(Edge selectedEdge, List<ParameterCost> costToEndOfEdge) {
        this(selectedEdge, costToEndOfEdge, new ArrayList<>());
    }

    public ShortestPath(Edge selectedEdge, List<ParameterCost> costToEndOfEdge, List<ShortestPath> history) {
        _Edge = selectedEdge;
        _Cost = costToEndOfEdge;
        _History = history;
    }

    public void appendEdge(Edge edgeToAppend, List<ParameterCost> newCost) {
        _History.add(new ShortestPath(_Edge, _Cost));
        _Edge = edgeToAppend;
        _Cost = newCost;
    }

    public ShortestPath clone() {
        var newHistory = new ArrayList<ShortestPath>();

        for(var pathInHistory : _History) {
            newHistory.add(pathInHistory);
        }

        return new ShortestPath(_Edge, _Cost, newHistory);
    }

    public List<ShortestPath> getHistory(){
        var list = new ArrayList<ShortestPath>();

        for(var path : _History) {
            list.add(path);
        }

        return list;
    }

    public Edge getEdge() {
        return _Edge;
    }

    public List<ParameterCost> getCosts() {
        return _Cost;
    }

    public void setEdge(Edge edge) {
        _Edge = edge;
    }

    public IVertex getStartVertex() {
        return _Edge.getSource();
    }

    public IVertex getEndVertex() {
        return _Edge.getDestination();
    }

    @Override
    public void updateWeight(String nameOfWeight, Number weight) {
        var cost = _Cost.stream().filter(x -> x.getParameterName().equals(nameOfWeight)).findFirst().get();
        if (cost != null)
            cost.setValue(weight);
    }

    @Override
    public ParameterCost getWeight(String nameOfWeight) {
        return _Cost.stream().filter(x -> x.getParameterName().equals(nameOfWeight)).findFirst().orElse(null);
    }
}
