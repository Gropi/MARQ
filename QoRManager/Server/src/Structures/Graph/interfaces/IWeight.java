package Structures.Graph.interfaces;

import Condition.ParameterCost;

public interface IWeight {
    void updateWeight(String nameOfWeight, Number weight);

    ParameterCost getWeight(String nameOfWeight);
}
