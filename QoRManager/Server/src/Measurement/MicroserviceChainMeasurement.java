package Measurement;

import Monitoring.Enums.MeasurableValues;
import Network.DataModel.CommunicationMessages;
import Structures.Graph.interfaces.IVertex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MicroserviceChainMeasurement {

    //Variables for true measurement
    public static String MAP_KEY_QOR = "QoR";
    public static String MAP_KEY_ENERGY = "ENERGY";
    public static String MAP_KEY_COST = "COST";
    public static String MAP_KEY_IDLE_TIME_CONSUMED = "IdleTime";
    public static String MAP_KEY_EXECUTION_TIME_CONSUMED = "ExecutionTime";
    public static String MAP_KEY_TRANSMISSION_CONSUMED = "TransmissionTime";

    ConcurrentHashMap<IVertex, ConcurrentHashMap<String, Object>> _GlobalAdditionalInformation;
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<IVertex, ConcurrentHashMap<String, Object>>> _VertexInformationPerSG;

    private final int _Deadline;

    //Variables for initial selection
    private double _Initial_TotalTimeTaken;
    private List<Double> _Initial_QoROfPaths;
    private HashMap<UUID, Double> _Initial_EnergyOfVertices;
    private HashMap<UUID, Double> _Initial_CostOfVertices;

    public MicroserviceChainMeasurement(int deadline) {

        _Deadline = deadline;
        _VertexInformationPerSG = new ConcurrentHashMap<>();
        _GlobalAdditionalInformation = new ConcurrentHashMap<>();

        _Initial_TotalTimeTaken = 0d;
        _Initial_QoROfPaths = new ArrayList<>();

        _Initial_EnergyOfVertices = new HashMap<>();
        _Initial_CostOfVertices = new HashMap<>();

    }

    public void addSelectedVertex(IVertex selectedVertex, CommunicationMessages.TerminationMessage terminationMessage, List<Integer> subgraphIndices) {
        ConcurrentHashMap<IVertex, ConcurrentHashMap<String, Object>> additionalInformation;

        for(var index : subgraphIndices) {

            if(_VertexInformationPerSG.containsKey(index)){
                additionalInformation = _VertexInformationPerSG.get(index);
            } else {
                additionalInformation = new ConcurrentHashMap<>();
                _VertexInformationPerSG.put(index, additionalInformation);
            }
            if(additionalInformation.containsKey(selectedVertex))
                throw new RuntimeException("The same vertex can not be executed twice in one subgraph");

            var vertexMap = new ConcurrentHashMap<String, Object>();
            vertexMap.put(MAP_KEY_QOR, selectedVertex.getQoR());
            vertexMap.put(MAP_KEY_ENERGY, selectedVertex.getWeight(MeasurableValues.ENERGY.name()).getValue());
            vertexMap.put(MAP_KEY_COST, selectedVertex.getWeight(MeasurableValues.COST.name()).getValue());

            vertexMap.put(MAP_KEY_EXECUTION_TIME_CONSUMED, terminationMessage.getExecutionTime());
            vertexMap.put(MAP_KEY_IDLE_TIME_CONSUMED, terminationMessage.getIdleTime());
            vertexMap.put(MAP_KEY_TRANSMISSION_CONSUMED, terminationMessage.getTransmissionTime());

            additionalInformation.put(selectedVertex, vertexMap);

            if(!_GlobalAdditionalInformation.containsKey(selectedVertex))
                _GlobalAdditionalInformation.put(selectedVertex, vertexMap);
        }
    }

    public Map<Integer, Map<IVertex, Map<String, Object>>> getAdditionalInformation() {
        var additionalInformation = new HashMap<Integer, Map<IVertex, Map<String, Object>>>();

        var subgraphs = _VertexInformationPerSG.keySet();

        for(var graph : subgraphs) {
            var vertexMap = new HashMap<IVertex, Map<String, Object>>();
            additionalInformation.put(graph, vertexMap);

            var additionalInformationForGraph =  _VertexInformationPerSG.get(graph);
            var verticesInSubgraph = additionalInformationForGraph.keySet();

            for(var vertex : verticesInSubgraph) {
                var informationMap = new HashMap<String, Object>();
                vertexMap.put(vertex, informationMap);

                var additionalInformationForVertex = additionalInformationForGraph.get(vertex);
                var informationOfVertex = additionalInformationForVertex.keySet();

                for(var information : informationOfVertex) {
                    informationMap.put(information, additionalInformationForVertex.get(information));
                }
            }
        }

        return additionalInformation;
    }

    private double getSGTime(int sgIndex) {
        var relevantInformation = _VertexInformationPerSG.get(sgIndex);
        var keys = relevantInformation.keySet();
        var result = 0d;

        for(var key : keys) {
            var vertexMap = relevantInformation.get(key);
            var totalTimeForVertex = (Double)vertexMap.get(MAP_KEY_IDLE_TIME_CONSUMED)
                    + (Double)vertexMap.get(MAP_KEY_EXECUTION_TIME_CONSUMED)
                    + (Double)vertexMap.get(MAP_KEY_TRANSMISSION_CONSUMED);

            result += totalTimeForVertex;
        }

        return result;
    }

    public double getTotalTime() {
        var totalTime = 0d;
        var subgraphs = _VertexInformationPerSG.keySet();

        for(var graph : subgraphs) {
            var graphTime = getSGTime(graph);
            if(graphTime > totalTime) {
                totalTime = graphTime;
            }
        }

        return totalTime;
    }

    private int getSGQoR(int sgIndex) {
        var relevantInformation = _VertexInformationPerSG.get(sgIndex);
        var keys = relevantInformation.keySet();
        var result = 100d;

        for(var key : keys) {
            var vertexMap = relevantInformation.get(key);
            var vertexQoR =  (Integer)vertexMap.get(MAP_KEY_QOR);

            result *= vertexQoR;
            result = result /100;
        }

        if(result > 100) {
            throw new RuntimeException("QoR of Path exceeded 100%");
        }

        return (int)result;
    }

    public int getTotalEnergy() {
        var totalEnergy = 0;

        var vertices = _GlobalAdditionalInformation.keySet();

        for(var vertex : vertices) {
            var vertexMap = _GlobalAdditionalInformation.get(vertex);
            var vertexEnergy =  (Integer)vertexMap.get(MAP_KEY_ENERGY);

            totalEnergy += vertexEnergy;
        }

        return totalEnergy;
    }

    public int getTotalCost() {
        var totalCost = 0;

        var vertices = _GlobalAdditionalInformation.keySet();

        for(var vertex : vertices) {
            var vertexMap = _GlobalAdditionalInformation.get(vertex);
            var vertexCost = (int) Math.round((double) vertexMap.get(MAP_KEY_COST));

            totalCost += vertexCost;
        }

        return totalCost;
    }

    public int getTotalQoRAcrossPaths() {
        var totalQoR = 0;
        var subgraphs = _VertexInformationPerSG.keySet();
        var sgCount = subgraphs.size();

        for(var graph : subgraphs) {
            var graphQoR = getSGQoR(graph);
            totalQoR += graphQoR;
        }
        var result = totalQoR/sgCount;

        if(result > 100) {
            /**
             * System.out.println(sgCount);
             * for(var graph : subgraphs) {
             *      System.out.println(getSGQoR(graph));
             * }
             */
            throw new RuntimeException("Total QoR exceeded 100%");
        }

        return result;
    }

    public int getDeadline() {
        return _Deadline;
    }

    //Methods to get initial approximation of measurement
    public void addPathQoR(double pathQoR) {
        _Initial_QoROfPaths.add(pathQoR);
    }

    public void addVertexEnergy(UUID vertexID, double vertexEnergy) {
        if(!_Initial_EnergyOfVertices.containsKey(vertexID))
            _Initial_EnergyOfVertices.put(vertexID, vertexEnergy);
    }

    public void addVertexCost(UUID vertexID,double vertexCost) {
        if(!_Initial_CostOfVertices.containsKey(vertexID))
            _Initial_CostOfVertices.put(vertexID, vertexCost);
    }

    public void setTotalTimeTaken(double totalTime) {
        _Initial_TotalTimeTaken = totalTime;
    }

    public void clearInitialParameters() {
        _Initial_TotalTimeTaken = 0d;
        _Initial_QoROfPaths = new ArrayList<>();
        _Initial_EnergyOfVertices = new HashMap<>();
        _Initial_CostOfVertices = new HashMap<>();
    }

    public int getInitialQoRAcrossPaths() {
        double result = 0;

        for(var qor : _Initial_QoROfPaths){
            result += qor;
        }
        result = result/ _Initial_QoROfPaths.size();

        return (int)(result);
    }

    public int getInitialEnergyAcrossPaths() {
        double result = 0;
        var vertices = _Initial_EnergyOfVertices.keySet();

        for(var vertex : vertices){
            result += _Initial_EnergyOfVertices.get(vertex);
        }

        return (int)(result);
    }

    public int getInitialCostAcrossPaths() {
        double result = 0;
        var vertices = _Initial_CostOfVertices.keySet();

        for(var vertex : vertices){
            result += _Initial_CostOfVertices.get(vertex);
        }

        return (int)(result);
    }

    public double getInitialTotalTimeTaken() {
        return _Initial_TotalTimeTaken;
    }
}
