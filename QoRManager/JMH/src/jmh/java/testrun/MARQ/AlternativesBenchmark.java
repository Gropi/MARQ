package testrun.MARQ;

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
public class AlternativesBenchmark {
    private final Logger m_Logger = LogManager.getRootLogger();

    //PARAMS
    @Param({"2", "5", "10", "25", "50", "100", "150", "200", "250", "300"})
    public int alternativeCount;
    @Param({"2", "4", "6", "8", "10"})
    public int parameterCount;

    //ALTERNATIVES
    private Map<Integer, List<Vertex>> m_2_Alternatives;
    private Map<Integer, List<Vertex>> m_5_Alternatives;
    private Map<Integer, List<Vertex>> m_10_Alternatives;
    private Map<Integer, List<Vertex>> m_25_Alternatives;
    private Map<Integer, List<Vertex>> m_FiftyAlternatives;
    private Map<Integer, List<Vertex>> m_HundredAlternatives;
    private Map<Integer, List<Vertex>> m_OneHundredAndFiftyAlternatives;
    private Map<Integer, List<Vertex>> m_TwoHundredAlternatives;
    private Map<Integer, List<Vertex>> m_TwoHundredAndFiftyAlternatives;
    private Map<Integer, List<Vertex>> m_ThreeHundredAlternatives;
    private Map<Integer, List<Vertex>> m_ThreeHundredAndFiftyAlternatives;
    private Map<Integer, List<Vertex>> m_FourHundredAlternatives;
    private Map<Integer, List<Vertex>> m_FourHundredAndFiftyAlternatives;
    private Map<Integer, List<Vertex>> m_FiveHundredAlternatives;

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
        var maxParameterCount = 20;
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

        Configurator.setAllLevels("executionLog", Level.ERROR);
        Configurator.setAllLevels("measurementLog", Level.ERROR);
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);

        m_2_Alternatives = new HashMap<>();
        m_5_Alternatives = new HashMap<>();
        m_10_Alternatives = new HashMap<>();
        m_25_Alternatives = new HashMap<>();
        m_FiftyAlternatives = new HashMap<>();
        m_HundredAlternatives = new HashMap<>();
        m_OneHundredAndFiftyAlternatives = new HashMap<>();
        m_TwoHundredAlternatives = new HashMap<>();
        m_TwoHundredAndFiftyAlternatives = new HashMap<>();
        m_ThreeHundredAlternatives = new HashMap<>();
        m_ThreeHundredAndFiftyAlternatives = new HashMap<>();
        m_FourHundredAlternatives = new HashMap<>();
        m_FourHundredAndFiftyAlternatives = new HashMap<>();
        m_FiveHundredAlternatives = new HashMap<>();

        var r = new Random();

        for(int p = 1; p <= maxParameterCount; p++) {
            //PROMETHEE PARAMETERS

            //--CRITERIA
            var criteriaList = new ArrayList<Pair<String, Boolean>>();

            if(p > 1){
                var oldList = m_Parameter.get(p-1);
                criteriaList.addAll(oldList);
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

            //ALTERNATIVES
            m_2_Alternatives.put(p, new ArrayList<>());
            m_5_Alternatives.put(p, new ArrayList<>());
            m_10_Alternatives.put(p, new ArrayList<>());
            m_25_Alternatives.put(p, new ArrayList<>());
            m_FiftyAlternatives.put(p, new ArrayList<>());
            m_HundredAlternatives.put(p, new ArrayList<>());
            m_OneHundredAndFiftyAlternatives.put(p, new ArrayList<>());
            m_TwoHundredAlternatives.put(p, new ArrayList<>());
            m_TwoHundredAndFiftyAlternatives.put(p, new ArrayList<>());
            m_ThreeHundredAlternatives.put(p, new ArrayList<>());
            m_ThreeHundredAndFiftyAlternatives.put(p, new ArrayList<>());
            m_FourHundredAlternatives.put(p, new ArrayList<>());
            m_FourHundredAndFiftyAlternatives.put(p, new ArrayList<>());
            m_FiveHundredAlternatives.put(p, new ArrayList<>());
            for(int i = 1; i <= 10; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                if (i <= 2)
                    m_2_Alternatives.get(p).add(vertex);
                if (i <= 5)
                    m_5_Alternatives.get(p).add(vertex);
                m_10_Alternatives.get(p).add(vertex);
            }

            for(int i = 1; i <= 25; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_25_Alternatives.get(p).add(vertex);
                m_FiftyAlternatives.get(p).add(vertex);
                m_HundredAlternatives.get(p).add(vertex);
                m_OneHundredAndFiftyAlternatives.get(p).add(vertex);
                m_TwoHundredAlternatives.get(p).add(vertex);
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 26; i <= 50; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_FiftyAlternatives.get(p).add(vertex);
                m_HundredAlternatives.get(p).add(vertex);
                m_OneHundredAndFiftyAlternatives.get(p).add(vertex);
                m_TwoHundredAlternatives.get(p).add(vertex);
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 51; i <= 75; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_HundredAlternatives.get(p).add(vertex);
                m_OneHundredAndFiftyAlternatives.get(p).add(vertex);
                m_TwoHundredAlternatives.get(p).add(vertex);
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 76; i <= 100; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_HundredAlternatives.get(p).add(vertex);
                m_OneHundredAndFiftyAlternatives.get(p).add(vertex);
                m_TwoHundredAlternatives.get(p).add(vertex);
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 101; i <= 125; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_OneHundredAndFiftyAlternatives.get(p).add(vertex);
                m_TwoHundredAlternatives.get(p).add(vertex);
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 126; i <= 150; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_OneHundredAndFiftyAlternatives.get(p).add(vertex);
                m_TwoHundredAlternatives.get(p).add(vertex);
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 151; i <= 175; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_TwoHundredAlternatives.get(p).add(vertex);
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 176; i <= 200; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_TwoHundredAlternatives.get(p).add(vertex);
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 201; i <= 250; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_TwoHundredAndFiftyAlternatives.get(p).add(vertex);
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 251; i <= 300; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_ThreeHundredAlternatives.get(p).add(vertex);
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 301; i <= 350; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_ThreeHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 351; i <= 400; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_FourHundredAlternatives.get(p).add(vertex);
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 401; i <= 450; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_FourHundredAndFiftyAlternatives.get(p).add(vertex);
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
            for(int i = 451; i <= 500; i++) {
                var vertex = new Vertex("Vertex" + i, toUUID(i), "");
                for(int j = 1; j <= p; j++) {
                    vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
                }
                m_FiveHundredAlternatives.get(p).add(vertex);
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisSimpleNormalizationBenchmarkSingleShotTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.SIMPLE, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisLinearNormalizationBenchmarkSingleShotTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.LINEAR, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisVectorNormalizationBenchmarkSingleShotTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.VECTOR, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void TopsisSimpleNormalizationBenchmarkThroughput(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.SIMPLE, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void TopsisLinearNormalizationBenchmarkThroughput(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.LINEAR, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void TopsisVectorNormalizationBenchmarkThroughput(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.VECTOR, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisSimpleNormalizationBenchmarkAverageTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.SIMPLE, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisLinearNormalizationBenchmarkAverageTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.LINEAR, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisVectorNormalizationBenchmarkAverageTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.VECTOR, blackhole);
    }

    private void TopsisBenchmark(NormalizationMode normalizingMode, Blackhole blackhole) {
        Double[] closeness;
        var currentOptimumValue = Double.MIN_VALUE;

        if(alternativeCount == 2) {
            closeness = m_Topsis.getCloseness(m_2_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 5) {
            closeness = m_Topsis.getCloseness(m_5_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 10) {
            closeness = m_Topsis.getCloseness(m_10_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 25) {
            closeness = m_Topsis.getCloseness(m_25_Alternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 50) {
            closeness = m_Topsis.getCloseness(m_FiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 100) {
            closeness = m_Topsis.getCloseness(m_HundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 150) {
            closeness = m_Topsis.getCloseness(m_OneHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        }  else if(alternativeCount == 200) {
            closeness = m_Topsis.getCloseness(m_TwoHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 250) {
            closeness = m_Topsis.getCloseness(m_TwoHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 300) {
            closeness = m_Topsis.getCloseness(m_ThreeHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 350) {
            closeness = m_Topsis.getCloseness(m_ThreeHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 400) {
            closeness = m_Topsis.getCloseness(m_FourHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 450) {
            closeness = m_Topsis.getCloseness(m_FourHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
        } else if(alternativeCount == 500) {
            closeness = m_Topsis.getCloseness(m_FiveHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount), m_Weights.get(parameterCount), normalizingMode);
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
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void ParetoBenchmarkAverageTime(Blackhole blackhole) {
        ParetoBenchmark(blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void ParetoBenchmarkThroughput(Blackhole blackhole) {
        ParetoBenchmark(blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 100)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void ParetoBenchmarkSingleShotTime(Blackhole blackhole) {
        ParetoBenchmark(blackhole);
    }

    private void ParetoBenchmark(Blackhole blackhole) {
        List<Vertex> paretoOptima;

        if(alternativeCount == 2) {
            paretoOptima = m_ParetoFilter.findOptima(m_2_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 5) {
            paretoOptima = m_ParetoFilter.findOptima(m_5_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 10) {
            paretoOptima = m_ParetoFilter.findOptima(m_10_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 25) {
            paretoOptima = m_ParetoFilter.findOptima(m_25_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 50) {
            paretoOptima = m_ParetoFilter.findOptima(m_FiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 100) {
            paretoOptima = m_ParetoFilter.findOptima(m_HundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 150) {
            paretoOptima = m_ParetoFilter.findOptima(m_OneHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 200) {
            paretoOptima = m_ParetoFilter.findOptima(m_TwoHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 250) {
            paretoOptima = m_ParetoFilter.findOptima(m_TwoHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 300) {
            paretoOptima = m_ParetoFilter.findOptima(m_ThreeHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 350) {
            paretoOptima = m_ParetoFilter.findOptima(m_ThreeHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 400) {
            paretoOptima = m_ParetoFilter.findOptima(m_FourHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 450) {
            paretoOptima = m_ParetoFilter.findOptima(m_FourHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 500) {
            paretoOptima = m_ParetoFilter.findOptima(m_FiveHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else {
            throw new RuntimeException("Invalid test case chosen!");
        }

        blackhole.consume(paretoOptima);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EConstraintBenchmarkAverageTime(Blackhole blackhole) {
        EConstraintBenchmark(blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EConstraintBenchmarkSingleShotTime(Blackhole blackhole) {
        EConstraintBenchmark(blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void EConstraintBenchmarkThroughput(Blackhole blackhole) {
        EConstraintBenchmark(blackhole);
    }

    private void EConstraintBenchmark(Blackhole blackhole) {
        List<Vertex> paretoOptima;
        List<? extends IWeight> alternativesInBoundry;
        var currentOptimumValue = Integer.MIN_VALUE;

        if(alternativeCount == 2) {
            paretoOptima = m_ParetoFilter.findOptima(m_2_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 5) {
            paretoOptima = m_ParetoFilter.findOptima(m_5_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 10) {
            paretoOptima = m_ParetoFilter.findOptima(m_10_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 25) {
            paretoOptima = m_ParetoFilter.findOptima(m_25_Alternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 50) {
            paretoOptima = m_ParetoFilter.findOptima(m_FiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 100) {
            paretoOptima = m_ParetoFilter.findOptima(m_HundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 150) {
            paretoOptima = m_ParetoFilter.findOptima(m_OneHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 200) {
            paretoOptima = m_ParetoFilter.findOptima(m_TwoHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 250) {
            paretoOptima = m_ParetoFilter.findOptima(m_TwoHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 300) {
            paretoOptima = m_ParetoFilter.findOptima(m_ThreeHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 350) {
            paretoOptima = m_ParetoFilter.findOptima(m_ThreeHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 400) {
            paretoOptima = m_ParetoFilter.findOptima(m_FourHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 450) {
            paretoOptima = m_ParetoFilter.findOptima(m_FourHundredAndFiftyAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else if(alternativeCount == 500) {
            paretoOptima = m_ParetoFilter.findOptima(m_FiveHundredAlternatives.get(parameterCount), m_Parameter.get(parameterCount));
        } else {
            throw new RuntimeException("Invalid test case chosen!");
        }

        alternativesInBoundry = m_BoundryFilter.filter(paretoOptima, m_Parameter.get(parameterCount), m_Criteria.get(parameterCount));

        for (IWeight candidate : alternativesInBoundry) {
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