package testrun;

import Comparator.DecisionAid.*;
import Comparator.DecisionAid.DataModel.NormalizationMode;
import Comparator.DecisionAid.TOPSIS.Topsis;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IWeight;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.github.atomfinger.touuid.UUIDs.toUUID;

@State(Scope.Benchmark)
@Fork(value = 4)
public class SmallerComparatorBenchmark {
    private final Logger m_Logger = LogManager.getRootLogger();

    //PARAMS
    @Param({"2", "3", "4", "5", "6", "7", "8", "9", "10"})
    public int alternativeCount;
    @Param({"2", "4", "6", "8", "10"})
    public int parameterCount;

    //ALTERNATIVES
    private Map<Integer, List<Vertex>> m_2_Alternatives;
    private Map<Integer, List<Vertex>> m_3_Alternatives;
    private Map<Integer, List<Vertex>> m_4_Alternatives;
    private Map<Integer, List<Vertex>> m_5_Alternatives;
    private Map<Integer, List<Vertex>> m_6_Alternatives;
    private Map<Integer, List<Vertex>> m_7_Alternatives;
    private Map<Integer, List<Vertex>> m_8_Alternatives;
    private Map<Integer, List<Vertex>> m_9_Alternatives;
    private Map<Integer, List<Vertex>> m_10_Alternatives;

    //COMPARATORS
    private ParetoFilter<Vertex> m_ParetoFilter;
    private BoundryFilter<Vertex> m_BoundryFilter;
    private Topsis m_Topsis;

    private Map<Integer, List<Pair<String, Boolean>>> m_Parameter;
    private Map<Integer, Double[]> m_Weights;

    //ECONSTRAINT ARGUMENTS
    private Map<Integer, Map<String, Number>> m_Criteria;

    @Setup
    public void initTests() {
        //GLOBAL PARAMETERS
        var maxParameterCount = 10;
        var parameterRange = 5001;
        var limit = 100;

        var defaultWeightBound = 100;

        m_ParetoFilter = new ParetoFilter<>();
        m_Topsis = new Topsis(m_Logger);
        m_BoundryFilter = new BoundryFilter<>();

        m_Parameter = new HashMap<>();
        m_Weights = new HashMap<>();

        //LIMITS
        m_Criteria = new HashMap<>();

        Configurator.setAllLevels("executionLog", org.apache.logging.log4j.Level.ERROR);
        Configurator.setAllLevels("measurementLog", org.apache.logging.log4j.Level.ERROR);
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);

        m_2_Alternatives= new HashMap<>();
        m_3_Alternatives= new HashMap<>();
        m_4_Alternatives= new HashMap<>();
        m_5_Alternatives= new HashMap<>();
        m_6_Alternatives= new HashMap<>();
        m_7_Alternatives= new HashMap<>();
        m_8_Alternatives= new HashMap<>();
        m_9_Alternatives= new HashMap<>();
        m_10_Alternatives= new HashMap<>();

        var r = new Random();

        for(int p = 1; p <= maxParameterCount; p++) {
            //PROMETHEE PARAMETERS

            //--CRITERIA
            var criteriaList = new ArrayList<Pair<String, Boolean>>();

            if(p > 1){
                var oldList = m_Parameter.get(p-1);
                for(var pair : oldList) {
                    criteriaList.add(pair);
                }
            }
            criteriaList.add(new Pair<>("Parameter" + p, r.nextBoolean()));
            m_Parameter.put(p, criteriaList);

            //LIMITS
            var limitMap = new HashMap<String, Number>();
            for(int i = 1; i <= p; i++) {
                limitMap.put("Parameter" + i, limit);
            }
            m_Criteria.put(p, limitMap);

            //--WEIGHTS
            var preweights = new Integer[p];
            var totalWeights = 0;
            for(int i = 0; i < p; i++) {
                var value = r.nextInt(defaultWeightBound) + 1;
                preweights[i] = value;
                totalWeights += value;
            }
            var weights = new Double[p];
            for(int i = 0; i < p; i++) {
                weights[i] = preweights[i]*1d/totalWeights;
            }
            m_Weights.put(p, weights);

            //PRIORIZATION COMPARATOR
            var prioList = new ArrayList<String>();
            for(var prio : criteriaList) {
                prioList.add(prio.getFirst());
            }

            //ALTERNATIVES
            m_2_Alternatives.put(p, new ArrayList<>());
            m_3_Alternatives.put(p, new ArrayList<>());
            m_4_Alternatives.put(p, new ArrayList<>());
            m_5_Alternatives.put(p, new ArrayList<>());
            m_6_Alternatives.put(p, new ArrayList<>());
            m_7_Alternatives.put(p, new ArrayList<>());
            m_8_Alternatives.put(p, new ArrayList<>());
            m_9_Alternatives.put(p, new ArrayList<>());
            m_10_Alternatives.put(p, new ArrayList<>());

            for(int i = 1; i <= 2; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_2_Alternatives.get(p).add(vertex);
                m_3_Alternatives.get(p).add(vertex);
                m_4_Alternatives.get(p).add(vertex);
                m_5_Alternatives.get(p).add(vertex);
                m_6_Alternatives.get(p).add(vertex);
                m_7_Alternatives.get(p).add(vertex);
                m_8_Alternatives.get(p).add(vertex);
                m_9_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }
            for(int i = 3; i <= 3; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_3_Alternatives.get(p).add(vertex);
                m_4_Alternatives.get(p).add(vertex);
                m_5_Alternatives.get(p).add(vertex);
                m_6_Alternatives.get(p).add(vertex);
                m_7_Alternatives.get(p).add(vertex);
                m_8_Alternatives.get(p).add(vertex);
                m_9_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }
            for(int i = 4; i <= 4; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_4_Alternatives.get(p).add(vertex);
                m_5_Alternatives.get(p).add(vertex);
                m_6_Alternatives.get(p).add(vertex);
                m_7_Alternatives.get(p).add(vertex);
                m_8_Alternatives.get(p).add(vertex);
                m_9_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }
            for(int i = 5; i <= 5; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_5_Alternatives.get(p).add(vertex);
                m_6_Alternatives.get(p).add(vertex);
                m_7_Alternatives.get(p).add(vertex);
                m_8_Alternatives.get(p).add(vertex);
                m_9_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }
            for(int i = 6; i <= 6; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_6_Alternatives.get(p).add(vertex);
                m_7_Alternatives.get(p).add(vertex);
                m_8_Alternatives.get(p).add(vertex);
                m_9_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }
            for(int i = 7; i <= 7; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_7_Alternatives.get(p).add(vertex);
                m_8_Alternatives.get(p).add(vertex);
                m_9_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }
            for(int i = 8; i <= 8; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_8_Alternatives.get(p).add(vertex);
                m_9_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }
            for(int i = 9; i <= 9; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_9_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }
            for(int i = 10; i <= 10; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_10_Alternatives.get(p).add(vertex);
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisSimpleNormalizationBenchmarkAverageTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.SIMPLE, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisLinearNormalizationBenchmarkAverageTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.LINEAR, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisVectorNormalizationBenchmarkAverageTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.VECTOR, blackhole);
    }

    private void TopsisBenchmark(NormalizationMode normalizingMode, Blackhole blackhole) {
        Double[] closeness;
        var currentOptimumValue = Double.MIN_VALUE;

        if(alternativeCount == 2) {
            closeness = m_Topsis.getCloseness(m_2_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 3) {
            closeness = m_Topsis.getCloseness(m_3_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 4) {
            closeness = m_Topsis.getCloseness(m_4_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 5) {
            closeness = m_Topsis.getCloseness(m_5_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 6) {
            closeness = m_Topsis.getCloseness(m_6_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 7) {
            closeness = m_Topsis.getCloseness(m_7_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 8) {
            closeness = m_Topsis.getCloseness(m_8_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 9) {
            closeness = m_Topsis.getCloseness(m_9_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 10) {
            closeness = m_Topsis.getCloseness(m_10_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else {
            throw new RuntimeException("Invalid test case chosen!");
        }

        for (var aDouble : closeness) {
            if (aDouble > currentOptimumValue) {
                currentOptimumValue = aDouble;
            }
        }

        blackhole.consume(closeness);
        blackhole.consume(currentOptimumValue);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EConstraintBenchmarkAverageTime(Blackhole blackhole) {
        EConstraintBenchmark(blackhole);
    }

    private void EConstraintBenchmark(Blackhole blackhole) {
        List<Vertex> paretoOptima = null;
        List<? extends IWeight> alternativesInBoundry = null;
        var currentOptimumValue = Integer.MIN_VALUE;

        if(alternativeCount == 2) {
            paretoOptima = m_ParetoFilter.findOptima(m_2_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 3) {
            paretoOptima = m_ParetoFilter.findOptima(m_3_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 4) {
            paretoOptima = m_ParetoFilter.findOptima(m_4_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 5) {
            paretoOptima = m_ParetoFilter.findOptima(m_5_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 6) {
            paretoOptima = m_ParetoFilter.findOptima(m_6_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 7) {
            paretoOptima = m_ParetoFilter.findOptima(m_7_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 8) {
            paretoOptima = m_ParetoFilter.findOptima(m_8_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 9) {
            paretoOptima = m_ParetoFilter.findOptima(m_9_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 10) {
            paretoOptima = m_ParetoFilter.findOptima(m_10_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else {
            throw new RuntimeException("Invalid test case chosen!");
        }

        alternativesInBoundry = m_BoundryFilter.filter(paretoOptima, m_Parameter.get(parameterCount), m_Criteria.get(parameterCount));

        for (var candidate : alternativesInBoundry) {
            var candidateValue = candidate.getWeight(m_Parameter.get(1).get(0).getFirst()).getValue().intValue();
            if (candidateValue > currentOptimumValue) {
                currentOptimumValue = candidateValue;
            }
        }

        blackhole.consume(paretoOptima);
        blackhole.consume(alternativesInBoundry);
        blackhole.consume(currentOptimumValue);
    }
}
