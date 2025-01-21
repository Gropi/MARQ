# Summary: What Is the Purpose of the Collector?

The **Collector** is a central component of the **MARQ Framework**, designed to facilitate the execution of mission-critical, AI-based microservice chains. Its primary functions include:

1. **Data Collection**  
   The Collector continuously gathers relevant data from microservices, which is utilized by the **Quality of Result (QoR) Manager** to make informed decisions for optimizing and adapting the microservice chain.

2. **Support for Orchestration**  
   In **"serveandcollect"** mode, the Collector acts as a data provider, sending information to the central QoR Manager to enable dynamic runtime adaptation.

3. **Container Management**  
   In **"handlecontainers"** mode, the Collector manages Docker containers, including initializing and simulating dummy containers to support test environments or specific scenarios.

4. **Flexibility and Scalability**  
   The Collector is highly configurable via command-line parameters, allowing adaptation to different scenarios (e.g., specifying the target address, port, container mode, and the number of containers).

In summary, the Collector is a versatile tool that serves as the backbone for data collection and container management within the MARQ Framework, enabling optimal orchestration of microservices.

---

## Starting the Application

The Collector application is written in **C#** and requires **.NET 6.0** to run. Before starting the application, ensure the **QoR-Manager** is already online. If the connection to the QoR-Manager is lost during runtime, you can type `reconnect` in the command line to reestablish the connection.

### Command-Line Parameters

The following table summarizes the command-line parameters supported by the `Collector` program, their functions, default values, and example usage:

| **Parameter**       | **Description**                                                          | **Default Value**               | **Example Usage**          |
|---------------------|--------------------------------------------------------------------------|---------------------------------|----------------------------|
| `-p`                | Specifies the management port for the Collector or Docker Handler.       | `2003` (Collector), `2001` (Handler) | `-p 8080`                |
| `serveandcollect`   | Starts the Collector mode for gathering data.                            | Not enabled by default          | `serveandcollect`          |
| `handlecontainers`  | Starts the Docker Handler mode for managing containers.                  | Not enabled by default          | `handlecontainers`         |
| `-t`                | Specifies the target address (QoR-Manager) for the connection.           | None                            | `-t 192.168.1.100`         |
| `-tp`               | Specifies the target port (QoR-Manager) for the connection.              | None                            | `-tp 8081`                 |
| `-id`               | Specifies the unique ID for the Collector.                               | None                            | `-id Collector1`           |
| `-md`               | Specifies the container mode to use. Possible values: `dummy`, `real`.   | `dummy`                         | `-md real`                 |
| `-dc`               | Specifies the number of dummy containers to create.                      | `10`                            | `-dc 20`                   |
| `-dcsi`             | Specifies the starting index for dummy containers.                       | `1`                             | `-dcsi 5`                  |

### Notes
- **Mandatory Parameters**: Ensure all required parameters (e.g., `-p`, `-t`, and `-tp`) are provided for the application to function correctly.
- **Mode Selection**: Use either `serveandcollect` or `handlecontainers` to define the mode. If neither is specified, the application will log an error and terminate.
- **Default Values**: Parameters without explicit values will use the defaults listed in the table.
- **Error Handling**: Missing parameters for options like `-p`, `-t`, or `-tp` will result in error messages logged to the console.

---

## Example Commands

1. **Command Structure**:
   ```bash
   [-p <server port>] [-i <server IP address>] [serveandcollect | handlecontainers]

2. **Example call**:
    ```bash
    serveandcollect -p 2003 -t 192.168.1.100 -tp 2000 -id Collector1

## Building the Application for Server Execution

The Collector repository includes a script named `buildJob.bat`, which compiles the application and packages all necessary dependencies for execution on Linux systems.

### Steps to Build
1. Run the `buildJob.bat` script to compile the Collector.
2. After the build is complete, the compiled files for Linux will be located at: Collector\bin\Debug\net6.0\linux-x64
### Deployment
- The compiled files can be copied into a Docker container for deployment.
- Detailed instructions for building and deploying the Docker container can be found in the **Setup** section of the repository.