import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import testrun.MARQ.AlternativesBenchmark;
import testrun.MARQ.CriteriaBenchmark;
import testrun.MARQ.DecisionMaker.DecisionMakerBenchmarksBig;
import testrun.MARQ.DecisionMaker.DecisionMakerBenchmarksHuge;
import testrun.MARQ.DecisionMaker.DecisionMakerBenchmarksNormal;
import testrun.MARQ.DecisionMaker.DecisionMakerBenchmarksSmall;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        var opt = new OptionsBuilder()
                .include(DecisionMakerBenchmarksSmall.class.getSimpleName())
                .include(DecisionMakerBenchmarksBig.class.getSimpleName())
                .include(DecisionMakerBenchmarksNormal.class.getSimpleName())
                .include(DecisionMakerBenchmarksHuge.class.getSimpleName())
                .include(CriteriaBenchmark.class.getSimpleName())
                .include(AlternativesBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}