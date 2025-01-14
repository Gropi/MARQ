package Structures.Graph;

import Condition.ParameterCost;
import Monitoring.Enums.MeasurableValues;
import Services.IMicroservice;
import Structures.Graph.interfaces.IVertex;

import java.util.*;

public class Vertex implements IVertex {
    private final UUID _Id;
    private final List<ParameterCost> _Weights;
    private final String _Label;
    private int _Stage;
    private int _ApplicationIndex;
    private int _ApproximationIndex;
    private boolean _IsDecisionMaking;
    private String _ServiceName;
    private IMicroservice _Microservice;
    private int _QoR;

    public Vertex(String label, UUID vertexId, String serviceName) {
        _Weights = new ArrayList<>();
        _Label = label;
        _Id = vertexId;
        _QoR = 0;
        _IsDecisionMaking = false;

        _ServiceName = serviceName;
    }

    /** Method to get the name of the service the vertex represents
     *
     * @return the name of the service
     */
    @Override
    public String getServiceName(){
        return _ServiceName;
    }

    /** Method to update which service the vertex represents
     *
     * @param serviceName new service
     */
    @Override
    public void updateServiceName(String serviceName){
        _ServiceName = serviceName;
    }

    /** Method to get the id of the vertex at hand
     *
     * @return the id of the vertex at hand
     */
    @Override
    public UUID getId(){
        return _Id;
    }

    /** Method to get the label of the vertex at hand
     *
     * @return the label of the vertex at hand
     */
    @Override
    public String getLabel() {
        return _Label;
    }

    /** Method to get a specific weight of the vertex
     *
     * @param nameOfWeight the weight to get
     * @return the value of the weight to get
     */
    @Override
    public ParameterCost getWeight(String nameOfWeight) {
        var costsWithName = _Weights.stream().filter(x -> x.getParameterName().equals(nameOfWeight)).toList();
        if (!costsWithName.isEmpty())
            return costsWithName.get(0);
        return null;
    }

    /** Method to get the weights of the vertex at hand
     *
     * @return the weights of the vertex at hand
     */
    @Override
    public List<ParameterCost> getWeights() {
        return new ArrayList<>(_Weights);
    }

    /** Method to update the weight of the vertex at hand
     *
     * @param nameOfWeight weight to be updated
     * @param weight new value
     */
    @Override
    public void updateWeight(String nameOfWeight, Number weight) {
        if(nameOfWeight == MeasurableValues.LATENCY.name())
            throw new RuntimeException("Latency shouldn't be processed by vertices!");
        if (nameOfWeight == null)
            return;
        var foundWeight = _Weights.stream().filter(x -> x.getParameterName().equals(nameOfWeight)).findFirst().orElse(null);
        if (foundWeight != null)
            foundWeight.setValue(weight);
        else {
            var cost = new ParameterCost(weight, nameOfWeight);
            _Weights.add(cost);
        }
    }

    /** Method to determine whether the vertex at hand is equal to a given object
     *
     * @param o the given object
     * @return whether the object is equal to the vertex at hand
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var vertex = (Vertex) o;
        return _Id == vertex._Id;
    }

    /** Method to get the Quality of Result of the vertex
     *
     * @return the QoR
     */
    @Override
    public int getQoR(){
        return _QoR;
    }

    /** Method to update the Quality of Result of the vertex
     *
     * @param qor the new QoR
     */
    @Override
    public void setQoR(int qor){
        _QoR = qor;
        updateWeight(MeasurableValues.QoR.name(), qor);
    }

    /** Method to hash the vertex
     *
     * @return the hashed value of the vertex id
     */
    @Override
    public int hashCode() {
        return Objects.hash(_Id);
    }

    /** Method to get the stage of the vertex at hand
     *
     * @return the stage of the vertex at hand
     */
    @Override
    public int getStage() {
        return _Stage;
    }

    @Override
    public void setStage(int value){
        _Stage = value;
    }

    /** Method to get the application index
     *
     * @return the application index
     */
    @Override
    public int getApplicationIndex() {
        return _ApplicationIndex;
    }

    /** Method to set the application index
     *
     * @param index the new application index
     */
    @Override
    public void setApplicationIndex(int index) {
        this._ApplicationIndex = index;
    }

    /** Method to get the approximation index
     *
     * @return the approximation index
     */
    @Override
    public int getApproximationIndex() {
        return _ApproximationIndex;
    }

    /** Method to set the approximation index
     *
     * @param index the new approximation index
     */
    @Override
    public void setApproximationIndex(int index) {
        this._ApproximationIndex = index;
    }

    /** Clone vertex at hand without considering the edges
     *
     * @return A clone of the vertex at hand without any edges
     */
    @Override
    public Vertex clone(){
        var vertex = new Vertex(_Label, _Id, _ServiceName);

        for(var cost : _Weights)
            vertex.updateWeight(cost.getParameterName(), cost.getValue().intValue());

        vertex.setStage(_Stage);
        vertex.setQoR(_QoR);
        vertex.setApplicationIndex(getApplicationIndex());
        vertex.setApproximationIndex(getApproximationIndex());

        return vertex;
    }

    /** Recursive method to clone edges and vertices. The vertex at hand is cloned and all its edges and the vertices
     *  reachable by those edges are cloned too. To avoid infinite cloning the already cloned vertices are stored in
     *  the given Array.
     *
     * @param alreadyClonedVertices The vertices which have already been cloned
     * @return A cloned version of the vertex at hand
     */
    @Override
    public Vertex clone(List<IVertex> alreadyClonedVertices){
        var vertex = clone();

        alreadyClonedVertices.add(vertex);

        return vertex;
    }

    @Override
    public boolean isDecisionMakingVertex() {
        return _IsDecisionMaking;
    }

    @Override
    public void setDecisionMakingVertex(boolean isDecisionMaking) {
        _IsDecisionMaking = isDecisionMaking;
    }

    @Override
    public void bindMicroservice(IMicroservice service){
        _Microservice = service;
    }

    @Override
    public IMicroservice getMicroservice(){
        return _Microservice;
    }

    @Override
    public void unbindMicroservice() {
        _Microservice = null;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "id=" + _Id +
                ", label='" + _Label + '\'' +
                ", stage=" + _Stage +
                ", applicationIndex=" + _ApplicationIndex+
                ", approximationIndex=" + _ApproximationIndex +
                '}';
    }
}
