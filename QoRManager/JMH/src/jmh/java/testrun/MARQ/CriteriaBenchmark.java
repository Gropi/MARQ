package testrun.MARQ;

import Comparator.DecisionAid.BoundryFilter;
import Comparator.DecisionAid.DataModel.NormalizationMode;
import Comparator.DecisionAid.ParetoFilter;
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
public class CriteriaBenchmark {
    private final Logger m_Logger = LogManager.getRootLogger();
    //PARAMS
    @Param({"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"})
    public int alternativeCount;
    @Param({"2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "25", "30", "35", "40"})
    public int parameterCount;

    //ALTERNATIVES
    private List<Vertex> m_Alternatives;

    //COMPARATORS
    private ParetoFilter<Vertex> m_ParetoFilter;
    private BoundryFilter<Vertex> m_BoundryFilter;
    private Topsis m_Topsis;

    //PROMETHEE ARGUMENTS
    private List<Pair<String, Boolean>> m_Parameter;
    private Map<Integer, Double[]> m_Weights;

    //ECONSTRAINT ARGUMENTS
    private Map<Integer, Map<String, Number>> m_Criteria;

    @Setup
    public void initTests() {
        //GLOBAL PARAMETERS
        var maxParameterCount = 40;
        var maxAlternatives = 15;
        var parameterRange = 5001;
        var limit = 100;

        var defaultWeightBound = 100;

        m_ParetoFilter = new ParetoFilter<>();
        m_Topsis = new Topsis(m_Logger);
        m_BoundryFilter = new BoundryFilter<>();
        m_Alternatives = new ArrayList<>();

        m_Parameter = new ArrayList<>();
        m_Weights = new HashMap<>();

        //LIMITS
        m_Criteria = new HashMap<>();

        Configurator.setAllLevels("executionLog", Level.ERROR);
        Configurator.setAllLevels("measurementLog", Level.ERROR);
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);


        var r = new Random();

        for(int paramter = 1; paramter <= maxParameterCount; paramter++) {
            m_Parameter.add(new Pair<>("Parameter" + paramter, r.nextBoolean()));

            //LIMITS
            var limitMap = new HashMap<String, Number>();
            for(int i = 1; i <= paramter; i++) {
                limitMap.put("Parameter" + i, limit);
            }
            m_Criteria.put(paramter, limitMap);

            var preweights = new Integer[paramter];
            var totalWeights = 0;
            for(int i = 0; i < paramter; i++) {
                var value = r.nextInt(defaultWeightBound) + 1;
                preweights[i] = value;
                totalWeights += value;
            }
            var weights = new Double[paramter];
            for(int i = 0; i < paramter; i++) {
                weights[i] = preweights[i]*1d/totalWeights;
            }
            m_Weights.put(paramter, weights);
        }

        for(int i = 1; i <= maxAlternatives; i++) {
            var vertex = new Vertex("Vertex" + i, toUUID(i), "");
            for(int j = 1; j <= maxParameterCount; j++) {
                vertex.updateWeight("Parameter" + j, r.nextInt(parameterRange)-(parameterRange-1)/2);
            }
            m_Alternatives.add(vertex);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 30)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisSimpleNormalizationBenchmarkSingleShotTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.SIMPLE, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisLinearNormalizationBenchmarkSingleShotTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.LINEAR, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void TopsisSimpleNormalizationBenchmarkThroughput(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.SIMPLE, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void TopsisLinearNormalizationBenchmarkThroughput(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.LINEAR, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisSimpleNormalizationBenchmarkAverageTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.SIMPLE, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) //Mode.All
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void TopsisLinearNormalizationBenchmarkAverageTime(Blackhole blackhole) {
        TopsisBenchmark(NormalizationMode.LINEAR, blackhole);
    }

    private void TopsisBenchmark(NormalizationMode normalizingMode, Blackhole blackhole) {
        var closeness = m_Topsis.getCloseness(m_Alternatives.subList(0, alternativeCount - 1), m_Parameter.subList(0, parameterCount - 1), m_Weights.get(parameterCount), normalizingMode);
        blackhole.consume(closeness);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void ParetoBenchmarkAverageTime(Blackhole blackhole) {
        ParetoBenchmark(blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void ParetoBenchmarkThroughput(Blackhole blackhole) {
        ParetoBenchmark(blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void ParetoBenchmarkSingleShotTime(Blackhole blackhole) {
        ParetoBenchmark(blackhole);
    }

    private void ParetoBenchmark(Blackhole blackhole) {
        var paretoOptima = m_ParetoFilter.findOptima(m_Alternatives.subList(0, alternativeCount - 1), m_Parameter.subList(0, parameterCount - 1));
        blackhole.consume(paretoOptima);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EConstraintBenchmarkAverageTime(Blackhole blackhole) {
        EConstraintBenchmark(blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void EConstraintBenchmarkSingleShotTime(Blackhole blackhole) {
        EConstraintBenchmark(blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void EConstraintBenchmarkThroughput(Blackhole blackhole) {
        EConstraintBenchmark(blackhole);
    }

    private void EConstraintBenchmark(Blackhole blackhole) {
        var paretoOptima = m_ParetoFilter.findOptima(m_Alternatives.subList(0, alternativeCount - 1), m_Parameter.subList(0, parameterCount - 1));
        List<? extends IWeight> alternativesInBoundary;
        var currentOptimumValue = Integer.MIN_VALUE;

        alternativesInBoundary = m_BoundryFilter.filter(paretoOptima, m_Parameter.subList(0, parameterCount - 1), m_Criteria.get(parameterCount));

        for (var candidate : alternativesInBoundary) {
            var candidateValue = candidate.getWeight(m_Parameter.get(0).getFirst()).getValue().intValue();
            if (candidateValue > currentOptimumValue) {
                currentOptimumValue = candidateValue;
            }
        }

        blackhole.consume(paretoOptima);
        blackhole.consume(alternativesInBoundary);
        blackhole.consume(currentOptimumValue);
    }
}