# Protobuf

We utilize [Protobuf](https://protobuf.dev/) to model the communication between the `QoRManager` and other components, such as the `Encapsulation` and `Collector`. Protobuf is a user-friendly framework that automatically generates communication skeletons in various programming languages. For the microservice chain, we use **C#**, while for the `QoRManager`, we use **Java**. By relying on Protobuf's definition language, we ensure compatibility between systems as long as both use the same protocol version.

## Folder Structure

The Protobuf-related files and scripts are organized as follows:

- **`File` Folder**:  
  Contains the following:
  - The `.proto` file defining the communication protocol.
  - A `buildProtobuf.bat` script for building communication skeletons.

  **Note**: MARQ development occurred on Windows machines. If you plan to use this on Linux, manual adjustments to the script may be required.

- **`QoRManager`, `Collector`, and `Encapsulation` Folders**:  
  After running the `buildProtobuf.bat` script, the generated communication skeletons are copied into their respective folders under the main directory.

- **`Protobuf_Compiler` Folder**:  
  Includes the Windows version of the Protobuf compiler, ensuring version consistency across machines.

## How to Use

1. Navigate to the `File` folder.
2. Execute the `buildProtobuf.bat` script to generate the communication skeletons.
3. Verify that the generated files are copied into the appropriate directories:
  - `QoRManager`
  - `Collector`
  - `Encapsulation`
4. If you are working on a Linux environment, adapt the `buildProtobuf.bat` script to your system's requirements before execution.

By following these steps, you can maintain a consistent communication protocol across different components of the MARQ framework.
