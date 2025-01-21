# How to Run the Software

The MARQ software consists of two components: the **server**, which runs a single instance and manages the orchestration of tasks, and multiple **collectors**, which gather data required by the QoRManager for decision-making.

## Server

The main component of the MARQ system is the `Server`. This **Java project** provides all the necessary functionalities for MARQ to adapt the microservice chain dynamically during runtime.

### Steps to Run the Server
1. **Start the Server**: Launch the `Server` component.
2. **Connect Collectors**:
    - Each `Collector` establishes a connection to the server.
    - The server logs connected collectors on the command line.
3. **Start Execution**:
    - Once all collectors are connected, type `start` in the server's command line interface to begin executing the defined graph.
    - **Note**: The microservices must have names matching the graph node names. For example, if the graph node is named `dummy1`, a microservice with the same name must exist. Otherwise, the execution will fail.

---

## MARQ Execution Parameters

The following table summarizes the command-line parameters used by the `getTestbedParameters` method, along with their descriptions and example values:

| **Parameter Flag** | **Mapped Key**                | **Description**                                                                                                                                          | **Example Value**       |
|---------------------|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|
| `-dm`              | `DECISION_MAKER`              | Specifies the decision-making logic to use: `all`, `mobidic`, or `marq`.                                                                                 | `"all"`                 |
| `-cl`              | `CONTENT_LOCATION`            | Path to content required for execution (used with image recognition).                                                                                    | `"/path/to/content"`    |
| `-mpc`             | `MAX_PICTURE_COUNT`           | Maximum number of pictures to process (used with image recognition).                                                                                     | `100`                  |
| `-an`              | `APPLICATION_NAME`            | Name of the application being executed.                                                                                                                  | `"MyApplication"`       |
| `-gl`              | `GRAPH_LOCATION`              | Path to the graph file for execution.                                                                                                                    | `"/path/to/graph.graphml"` |
| `-gf`              | `GRAPH_FOLDER`                | Directory containing graph files for processing.                                                                                                         | `"/path/to/graphs"`     |
| `-ppc`             | `PICTURE_PER_CYCLE`           | Number of pictures processed per cycle.                                                                                                                  | `10`                    |
| `-tl`              | `TARGET_LOCATION`             | Target location for output results.                                                                                                                      | `"/output/path"`        |
| `-sim`             | `SIMULATION_MODE`             | Enables simulation mode (`true` when provided).                                                                                                          | N/A                     |
| `-tr`              | `TEST_REPEATS`                | Number of times to repeat the test.                                                                                                                      | `5`                     |
| `-dlr`             | `DEADLINE_STEP_SIZE`          | Configures the deadline step size: start value, step size, and end value.                                                                                 | `4000,500...5500`       |
| `-dl`              | `START_DEADLINE`              | Initial deadline for the process.                                                                                                                        | `500`                   |
| `-sg`              | `STORE_RANDOMIZED_GRAPHS`     | Enables storing of randomized graphs (`true` when provided).                                                                                             | N/A                     |

### Notes
- Parameters without explicit values (e.g., `-sim`, `-sg`) are activated by simply including the flag.
- Ensure paired flags (e.g., `-dm`, `-cl`) are followed by their respective values.
- Improperly formatted or unspecified flags will not be processed.

---

## Connection Parameters

The following table summarizes the parameters parsed by the `ConnectionInformation` class:

| **Parameter Flag** | **Description**                                                  | **Default Value**     | **Example Value** |
|--------------------|------------------------------------------------------------------|-----------------------|-------------------|
| `-c`               | Sets the connection mode to **client**.                          | `false` (server mode) | N/A               |
| `-s`               | Sets the connection mode to **server**.                          | `true` (default)      | N/A               |
| `-p`               | Specifies the management port for communication.                 | `2000`                | `8080`            |
| `-i`               | Specifies the IP address for the connection.                     | `127.0.0.1`           | `192.168.1.100`   |

### Notes
- By default, the system starts in **server mode** (`-s`).
- The `-p` flag requires a valid integer port number. The default is `2000`.
- The `-i` flag requires a valid IP address. The default is `127.0.0.1`.

---

## Example Command

Below is an example of how to start the application with all necessary parameters:

```bash
-i 10.130.22.54 -p 2000 -an TestApplication -gl ./TestData/Graph/Paper/normal.graphml -dm all -tr 10 -dlr 4000,500...5500
