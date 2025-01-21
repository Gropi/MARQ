# The Encapsulation Module

The **Encapsulation** module of the MARQ Framework is responsible for managing specific encapsulated services or containers. 
Depending on the selected mode, it can handle tasks such as Torchserve operations, face recognition, deployment, 
image blurring, or dummy execution.

---

## Starting the Application

The Encapsulation application is written in **C#** and requires **.NET 6.0** to run. You can start the application by specifying the appropriate command-line parameters.

### Command-Line Parameters

The following table summarizes the command-line parameters supported by the `Encapsulation` program, their functions, default values, and example usage:

| **Parameter** | **Description**                                      | **Default Value** | **Example Usage**       |
|---------------|------------------------------------------------------|-------------------|-------------------------|
| `-p`          | Specifies the port for communication.               | `2002`            | `-p 8080`              |
| `-ep`         | Specifies the endpoint to connect to.               | None              | `-ep http://localhost` |
| `-id`         | Specifies the unique ID for the Encapsulation.      | None              | `-id Encapsulation1`   |
| `ts`          | Selects the **Torchserve** service for encapsulation. | Not enabled by default | `ts`               |
| `fr`          | Selects the **Face Recognition** service for encapsulation. | Not enabled by default | `fr`               |
| `dep`         | Selects the **Deployer** service for encapsulation.  | Not enabled by default | `dep`              |
| `blur`        | Selects the **Image Blurring** service for encapsulation. | Not enabled by default | `blur`             |
| `dummy`       | Selects the **Dummy Execution** service for encapsulation. | Not enabled by default | `dummy`            |

### Notes
- **Mandatory Parameters**: Ensure that required parameters like `-p` and `-ep` are provided to avoid errors.
- **Service Selection**: Use one of the service options (`ts`, `fr`, `dep`, `blur`, `dummy`) to specify the task the Encapsulation module should perform.
- **Default Values**: If a parameter is not provided, the application will use its default value where applicable.
- **Error Handling**: Missing or incorrectly formatted parameters will result in an error message being logged.

---

## Example Commands

1. **Starting with Torchserve Service**:
   ```bash
   ts -p 2002 -ep http://localhost:8080 -id Encapsulation1

2. **Starting with Face Recognition Service:**:
   ```bash
   fr -p 2002 -ep http://localhost:8081 -id Encapsulation2

## Building the Application for Encapsulation Execution

The Encapsulation module includes a script named `buildJob.bat`, which compiles the application and packages all necessary dependencies for execution on Linux systems.

### Steps to Build
1. Navigate to the Encapsulation project directory.
2. Run the `buildJob.bat` script to compile the application.
3. After the build is complete, the compiled files for Linux will be located at: Encapsulation\bin\Debug\net6.0\linux-x64


### Deployment
- The compiled files can be copied into a Docker container for deployment.
- For further details on deploying the Encapsulation service, refer to the **Setup** section in the repository.

### Note
- The Encapsulation application is designed to work with different container services, such as Torchserve, Face Recognition, and others. Ensure you select the appropriate container mode by providing the correct command-line arguments when starting the application.


