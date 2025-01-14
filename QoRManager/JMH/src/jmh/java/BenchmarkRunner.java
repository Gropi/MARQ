import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import testrun.MARQ.CriteriaBenchmark;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        var opt = new OptionsBuilder()
                //.include(DecisionMakerBenchmarksSmall.class.getSimpleName())
                //.include(DijkstraBenchmarks.class.getSimpleName())
                .include(CriteriaBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}