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
```

## **Project Structure**
```
QoRManager
│
├── Server                      # Main server-side implementation
│   │── build.gradle            # Build configuration for the server
│   │── src                     # Source code
│   │   ├── ApplicationSupport  # Application parameter handling
│   │   ├── AvailableResources  # Management of microservices
│   │   ├── BusinessLogic       # Core business logic for QoRManager
│   │   ├── Comparator          # Decision-making comparators
│   │   ├── Condition           # Cost-based decision conditions
│   │   ├── GraphPathSearch     # Algorithms for shortest path search
│   │   ├── Measurement         # Measurement utilities for microservices
│   │   ├── Parser              # Graph and application parsing
│   │   ├── Structures          # Graph structures and utilities
│   │   ├── resources           # Configuration files (e.g., log4j2.xml)
│   │   ├── test                # Unit tests for different components
│   │
├── Shared                      # Shared components and utilities
│   │── build.gradle            # Build configuration for shared components
│   │── src                     # Source code for shared utilities
│   │   ├── Console             # Console utilities
│   │   ├── Events              # Event-handling utilities
│   │   ├── IO                  # Input/Output handling
│   │   ├── Monitoring          # Monitoring and logging
│   │   ├── Network             # Network communication utilities
│   │   ├── Services            # Microservice framework
│   │   ├── test                # Unit tests for shared components
│   │
├── TestData                    # Sample data for testing
│   │── ExampleApplication.xlsx  # Sample application data
│   │── Graph                    # GraphML files for graph-based testing
│   │
├── build.gradle                 # Root Gradle build file
├── settings.gradle               # Gradle settings file
```

### **Description of Key Components**

#### **1. Server (`Server/`)**
- Contains the core logic for handling QoR evaluation.
- Implements decision-making algorithms for microservice quality analysis.
- Uses network communication to interact with services.

#### **2. Shared (`Shared/`)**
- Provides common utilities such as network handling, monitoring, and event management.
- Implements microservice communication and logging.

#### **3. Test Data (`TestData/`)**
- Includes example applications and graph-based data for evaluation and testing.

### **Usage**
- This project is designed to facilitate QoR evaluations.
- The shared library provides reusable functionalities for different components.
- The server manages decision-making and interactions between microservices.
