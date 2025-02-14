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

## **Project Structure**
```
Encapsulation
│── buildJob.bat               # Build script
│── Encapsulation.sln          # Solution file for the project
│── README.md                  # Project documentation
│
├── CommonLibrary              # Shared library for common utilities
│   │── Communication           # Handles communication interfaces
│   │   │── ICommunicationFacade.cs
│   │   │
│   │   ├── Connection          # Manages connections
│   │   │   │── IClient.cs
│   │   │   │── IServer.cs
│   │   │   ├── impl
│   │   │       │── AsynchronousSocketListener.cs
│   │   │       │── Client.cs
│   │   │       │── Server.cs
│   │   │
│   │   ├── DataModel           # Defines communication data models
│   │   │   │── ConnectionInformation.cs
│   │   │   │── NetworkConversation.cs
│   │   │
│   │   ├── Facade
│   │   │   │── CommunicationFacade.cs
│
│   ├── IO                      # Input/Output handling
│   │   │── IOHandler.cs
│   │   ├── impl
│   │       │── IOHandlerImpl.cs
│
│   ├── Parser                  # Parsing utilities
│   │   │── IExcelParser.cs
│   │   ├── impl
│   │   │   │── ExcelParser.cs
│   │   ├── Objects
│   │       │── IExcelParsable.cs
│
│   ├── Properties              # Resource and localization files
│       │── Resources.Designer.cs
│       │── Resources.resx
│
├── Encapsulation               # Core implementation of the project
│   │── Encapsulation.csproj
│   │── Encapsulation.csproj.user
│   │── nlog.config             # Logging configuration
│   │── Program.cs              # Main entry point
│
│   ├── Businesslogic           # Core business logic
│   │   │── DeployerBL.cs
│   │   │── DummyExecutionBL.cs
│   │   │── FREncapsulationBL.cs
│   │   │── MagickBlurringBL.cs
│   │   │── TSEncapsulationBL.cs
│
│   ├── Communication
│   │   ├── DataModel           # Handles communication-related data
│   │       │── CommunicationMessages.cs
│   │       │── FoundObject.cs
│   │       │── ImageProcesser.cs
│   │       │── Person.cs
│   │       │── VectorClock.cs
│
│   ├── Helper
│   │   │── ICommunicationHelper.cs
│   │   ├── impl
│   │       │── CommunicationHelper.cs
│
│   ├── Properties              # Application settings
│   │   │── launchSettings.json
│   │   │── Resources.Designer.cs
│   │   │── Resources.resx
│
│   ├── Simulation              # Handles simulated executions
│   │   │── SimulatedParameter.cs
│
├── TestEncapsulation           # Unit tests for Encapsulation
│   │── TestEncapsulation.csproj
│   │── Usings.cs
│
│   ├── Businesslogic
│   │   │── DummyExecutionBL_Test.cs
│   │   │── TSEncapsulationBL_Test.cs
│
│   ├── Communication
│   │   ├── DataModel           # Tests for communication models
│   │       │── VectorClock_Test.cs
│
│   ├── Helper                  # Test helper utilities
│       │── TestHelper.cs
```

### **Description of Key Components**

#### **1. Core Components**
- `Encapsulation.sln`: The solution file containing the main project structure.
- `Program.cs`: The entry point of the application.
- `nlog.config`: Configuration for logging functionalities.
- `buildJob.bat`: A build script to automate the build process.

#### **2. Common Library (`CommonLibrary/`)**
- Contains core utilities used throughout the project.
- Manages communication interfaces, connection handling, and data modeling.
- Implements an I/O handler and Excel parsing utilities.

#### **3. Business Logic (`Encapsulation/Businesslogic/`)**
- Implements various business logic components, including deployment and execution logic.

#### **4. Communication (`Encapsulation/Communication/`)**
- Defines communication structures, including messages and object processing.

#### **5. Simulation (`Encapsulation/Simulation/`)**
- Implements functionalities for running simulations using simulated parameters.

#### **6. Testing (`TestEncapsulation/`)**
- Contains unit tests for different components, including business logic, communication models, and helpers.

### **Usage**
- This project is designed to encapsulate multiple functionalities, ensuring modularity.
- Tests are structured to validate different business logic components.
- Logging is managed via `nlog.config` to provide debugging insights.

