package Structures.Graph.interfaces;

import Condition.ParameterCost;
import Services.IMicroservice;

import java.util.List;
import java.util.UUID;

public interface IVertex extends IWeight{
    void updateServiceName(String serviceName);
    UUID getId();
    String getLabel();
    List<ParameterCost> getWeights();

    int getQoR();
    void setQoR(int qor);

    int getStage();
    void setStage(int value);

    IVertex clone();
    IVertex clone(List<IVertex> alreadyClonedVertices);

    boolean isDecisionMakingVertex();
    void setDecisionMakingVertex(boolean isDecisionMaking);

    int getApplicationIndex();
    void setApplicationIndex(int index);
    int getApproximationIndex();
    void setApproximationIndex(int index);

    String getServiceName();
    void bindMicroservice(IMicroservice service);
    IMicroservice getMicroservice();
    void unbindMicroservice();
}
