# MARQ - Example Graphs

This section of the repository contains the graphs used for evaluation purposes. We provide four distinct graphs with varying complexities based on the number of nodes: 10, 19, 40, and 120 nodes.

**Important:** If you wish to execute a graph of a specific size, ensure you have as many microservices as the node count requires.

## Folder Structure

Each graph directory consists of the following subfolders:

- **base**: Contains the fundamental graphs without any test-specific cost or runtime information. You can use these graphs with the `-sim` execution parameter of the `QoRManager` to generate randomized graphs.

- **randomized**: Includes graphs created during test executions with randomized cost and runtime information.

## Online Graph Viewer

If you would like to take a look at the structure of the diagrams, we recommend that you use the online viewer: https://graphonline.top/en/

## Graph Glossary

The following table provides an explanation of the values that may appear in a GraphML file:

| Term               | Explanation                                                                                                 |
|--------------------|-------------------------------------------------------------------------------------------------------------|
| `node`             | Represents a single node in the graph.                                                                      |
| `edge`             | Represents a connection (edge) between two nodes in the graph.                                              |
| `positionX`        | X-coordinate for graphical placement of the node.                                                           |
| `positionY`        | Y-coordinate for graphical placement of the node.                                                           |
| `id`               | Unique identifier for nodes and edges.                                                                      |
| `mainText`         | Main text displayed on the node in graphical representations.                                               |
| `upText`           | Header text displayed on the node in graphical representations.                                             |
| `size`             | Size of the node in graphical representations.                                                              |
| `application`      | Application index indicating the application associated with the node, relative to its stage.               |
| `approximation`    | Approximation index linked to a specific application and stage.                                             |
| `executionTime`    | Execution time associated with the node.                                                                    |
| `CPU`              | CPU usage required by the node's process.                                                                   |
| `RAM`              | RAM usage required by the node's process.                                                                   |
| `energy`           | Energy consumed by the node during execution.                                                               |
| `parameter1`-`5`   | Additional optimization parameters available for advanced configurations.                                   |
| `QoR`              | Quality of Result (QoR) value for the node's output.                                                        |
| `stage`            | Stage identifier to group nodes based on their position in the graph's workflow.                            |
| `source`           | ID of the source node in an edge.                                                                           |
| `target`           | ID of the target node in an edge.                                                                           |
| `isDirect`         | Boolean value indicating whether the edge is directed (`true`) or undirected (`false`).                     |
| `weight`           | Numerical value representing the weight or importance of the edge.                                          |
| `useWeight`        | Boolean value indicating whether the edge's weight is considered in computations.                           |
| `transmissionTime` | Time required to transmit data between the source and target nodes along the edge.                          |
| `arrayStyleStart`  | Currently unused GraphML value.                                                                             |
| `arrayStyleFinish` | Currently unused GraphML value.                                                                             |
| `model_width`      | Width of the edge in graphical representations, affecting its visual thickness.                             |
| `model_type`       | Type of the edge in graphical representations (e.g., linear, curved).                                       |
| `model_curveValue` | Curvature applied to the edge in graphical representations.                                                 |

This glossary is intended to help you understand the structure and content of the graph files. If additional terms or features are present, refer to the README files in specific folders or the [GraphML documentation](http://graphml.graphdrawing.org/).
