# Setting Up MARQ 

This section provides instructions and scripts for deploying MARQ on a distributed system or a local machine. Additionally, it includes scripts to execute the JMH test bed on your local device. Please note that running JMH from an Integrated Development Environment (IDE) might impact the measurements. We recommend using a JAR-based measurement approach, which closely resembles real-world usage.

## Folder Structure

- **Live-Tests:**  
  Contains scripts to run the QoR-Manager in live mode. In this mode, various microservices register with the QoR-Manager, which manages current conditions at the edge. For available configurations, refer to the `/QoR-Manager` directory, where all executable parameters for the QoR-Manager are described.

- **Performance-Evaluation:**  
  Includes various JMH test cases for performance evaluation.

- **TestData:**  
  Contains copies of graphs from the `/QoR-Manager/TestData/Graph/Paper` directory. This data serves as input for different test scenarios.


## Executing Performance Evaluation Tests

To perform the performance tests, follow these steps:

1. **Build the JAR Files:**

  - Open the `/QoR-Manager` project using an IDE like IntelliJ.

  - Navigate to the Gradle build settings:

    - Go to `JMH` → `Tasks` → `jmh`.

    - Build the `jmhJar` task.

  - This process will generate two files in the `/QoR-Manager/JMH/build/libs` directory.

2. **Prepare for Test Execution:**

  - Copy the generated files to the `/Setup/Performance-Evaluation/Execution/` folder.

3. **Run the Performance Tests:**

  - Execute the desired `.sh` scripts to start the performance tests.

  - **Note:** The `startTestPerformance_full.sh` script may take several days to complete.

### Post-Execution: Understanding the Results

After completing the tests, two new files will be generated:

- `jmh-results.csv`: Contains detailed execution information for each test case and iteration.

- `jmh-result.csv`: Provides summarized information of the test runs.

The `jmh-result.csv` file summarizes each benchmark, detailing:

- Number of samples executed.

- Execution score.

- Measurement units.

- Parameters used during execution.

With this information, you can create the necessary graphics and data representations to illustrate the performance differences between methods.

### JMH Benchmark Results

The performance test results are summarized in the following table:

| Benchmark                                                   | Mode | Threads | Samples | Score          | Score Error (99.9%) | Unit  | Param: alternativeCount | Param: parameterCount |
|-------------------------------------------------------------|------|---------|---------|----------------|---------------------|-------|-------------------------|-----------------------|
| testrun.ComparatorBenchmark.ParetoFilterBenchmarkThroughput | thrpt| 4       | 80      | 49,665,191.479 | 3,955,990.093       | ops/s | 50                      | 2                     |
| testrun.ComparatorBenchmark.ParetoFilterBenchmarkThroughput | thrpt| 4       | 80      | 8,616,473.555  | 423,737.445         | ops/s | 50                      | 4                     |
| testrun.ComparatorBenchmark.ParetoFilterBenchmarkThroughput | thrpt| 4       | 80      | 3,042,145.563  | 133,534.856         | ops/s | 50                      | 6                     |
| testrun.ComparatorBenchmark.ParetoFilterBenchmarkThroughput | thrpt| 4       | 80      | 1,695,909.048  | 35,337.079          | ops/s | 50                      | 8                     |
| testrun.ComparatorBenchmark.ParetoFilterBenchmarkThroughput | thrpt| 4       | 80      | 1,330,722.099  | 15,099.792          | ops/s | 50                      | 10                    |

**Description of Columns:**

- **Benchmark:** Specifies the fully qualified name of the executed benchmark test.

- **Mode:** Indicates the benchmarking mode used during the test. 'thrpt' stands for 'Throughput', measuring the number of operations per unit of time.

- **Threads:** Denotes the number of threads utilized during the benchmark execution.

- **Samples:** Represents the number of measurement samples collected during the benchmarking process.

- **Score:** Shows the primary metric value obtained from the benchmark. For throughput mode, it reflects the number of operations performed per second.

- **Score Error (99.9%):** Provides the margin of error for the score with a 99.9% confidence interval, indicating the statistical uncertainty of the measured score.

- **Unit:** Specifies the unit of measurement for the score. In throughput mode, this is typically 'operations per second' (ops/s).

- **Param: alternativeCount:** Represents a parameter used in the benchmark, with its corresponding value.

- **Param: parameterCount:** Another benchmark parameter, with its corresponding value.

These columns provide a comprehensive overview of the benchmark's configuration and performance metrics, allowing for detailed analysis and comparison of different test scenarios.


## Execute Distributed Application



## Current Limitations

As this software is intended for experimental purposes, there are certain limitations when executing MARQ, especially in a distributed manner:

- **Microservice Naming:**  
  The mapping from a microservice to a node in the graph is based on the name of the Docker container. Each Docker container connects to the QoR-Manager using this identifier. Therefore, the identifier must be unique and correspond exactly to its representation in the graph.

