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

We have provided an example application chain for the distributed execution of MARQ. To show the scalability of MARQ, we 
have used a dummy system that starts an arbitrary number of applications and models an execution, including transfer, 
based on this. However, with the help of encapsulation, a micro service chain can be realized as shown in Figure 1 of 
the paper. f you encounter any problems, please do not hesitate to contact us.
### Preparation

To make the application executable, please build the **Collector** and **Encapsulation** components (refer to their respective folders in the root directory). Once you have built these projects using the provided shell scripts, copy the contents of the folder:  
`Collector\Collector\bin\Debug\net6.0`  
to:  
`Setup\Live-Tests\Server-Simulation\MicroservicePipeline\_setup\collector`

and the contents of the folder:  
`Encapsulation\Encapsulation\bin\Debug\net6.0`  
to:  
`Setup\Live-Tests\Server-Simulation\MicroservicePipeline\_setup\encapsulation`.

Next, you can build the Docker containers required for running the software.
- To rebuild all examples, use the script `completeRebuildPipeline.sh`.
- If you want to try the scaling example, use the script `dummyRebuild.sh`.  
  Both scripts can be found in the folder:  
  `Setup\Live-Tests\Server-Simulation\MicroservicePipeline\_setup\`.

If you encounter any issues, first run the script `pruneDocker.sh`. If problems persist on your system, try using the 
`noCacheRebuild.sh` script.

### Execution of the Dummy Application

Once you have built the dummy microservices, the collector manages the Docker containers and registers them with the 
QoR-Manager. It is important to start the QoR-Manager first.

To start the execution, you will find the script `startLiveExecution.sh` in the folder `Setup\Live-Tests\`. This script 
will call all necessary applications and scripts, including network manipulation and the collector. **IMPORTANT:** The 
script must be executed on every server participating in the simulation. You may need to adjust the configuration if you 
plan to use the Big or Huge graph. Currently, the value for the number of containers to start (`containerCount`) is set 
to 20. This value determines how many containers will be started on the respective machine. You can pass a parameter to 
the script to specify a different number of containers (e.g., `startLiveExecution.sh 40`).

If you are using multiple servers, feel free to start a different number of containers on each machine. In this case, 
however, you will need to modify the start name configuration on each machine. To do this, edit the file 
`Setup\Live-Tests\Server-Simulation\live\testrunServer.sh`. Ensure that the call to the collector (the last line of the 
script) includes the correct parameters, as the dummy containers are counted sequentially. For example, if you want to 
start containers 21 to 40 on a specific server, add the parameter `-dcsi 20` at the end of the call. In this case, set 
`containerCount` to 21. The default value for `-dcsi` is 1.

After starting the script, all containers will be registered with the QoR-Manager. Once all containers are registered, 
you can start the test by typing `start` in the QoR-Manager's console.

### Network manipulation
By default, we start a network manipulation. We use [Pumba](https://github.com/alexei-led/pumba) for this. Pumba is a
chaos engineering tool, which makes it possible to manipulate the network with different distributions and intensities.
with different distributions and intensities. We manipulate 60% of the server instances. If you want to change this value, you can do so in the
file: `Setup\Live-Tests\Server-Simulation\live\startNetworkManipulation.sh`

## Current Limitations

As this software is intended for experimental purposes, there are certain limitations when executing MARQ, especially in a distributed manner:

- **Microservice Naming:**  
  The mapping from a microservice to a node in the graph is based on the name of the Docker container. Each Docker container connects to the QoR-Manager using this identifier. Therefore, the identifier must be unique and correspond exactly to its representation in the graph.

