# MARQ - Engineering <ins>M</ins>ission-Critical <ins>A</ins>I-based Software with Automated <ins>R</ins>esult <ins>Q</ins>uality Adaptation

Welcome to the official Git repository of the MARQ Framework. This repository serves as the central resource for accessing the source code, documentation, and development materials of MARQ.

## Purpose of This Repository
This repository provides:

- The full MARQ source code, enabling modification, extension, and deployment.
- Comprehensive documentation on setup, configuration, and execution.
- Guidelines for contributing to MARQ’s development.
- Example configurations and test cases for evaluating MARQ in different environments.

The MARQ Framework is designed to execute mission-critical AI-based microservice chains while ensuring compliance with 
key constraints such as execution deadlines, energy consumption, and Quality of Result (QoR). This repository contains all necessary resources to run, analyze, and extend MARQ for research and practical applications.

## Reference
If you wish to use this work for scientific purposes, please note that it was published at the
**25th IEEE/ACM International Conference on Software Engineering (ICSE) in 2025**.

----

## Repository Structure
The repository is organized into several key areas. To better understand the structure, we provide descriptions of each subfolder, along with its contents and purpose. Each subfolder also includes its own `README` file, offering detailed descriptions of the specific functionalities within the folder.

- **Applications:**  
  This folder contains example applications that were used in the paper to demonstrate the capabilities of the MARQ framework. These applications showcase how the framework can be applied in real-world scenarios.

- **Collector:**  
  The Collector is the component responsible for gathering runtime data from the microservices. This data is critical for the QoRManager to make informed decisions about adapting the microservice chain. The folder includes the source code, build scripts (e.g., `buildJob.bat`), and additional documentation on how to deploy and configure the Collector. In the end, this module handles the docker management for the dummy execution. 

- **Encapsulation:**  
  This folder contains the Encapsulation component, which manages the encapsulated services or containers. Depending on the configuration, it can handle tasks such as Torchserve operations, face recognition, deployment, image blurring, or dummy execution. The folder includes the application code and the necessary scripts to build and deploy the Encapsulation module. This component is mainly developed to be able to gether data from existing applications such as Torchserve, to enable the QoR-Management. 

- **Graphs:**  
  This folder contains both `base` and `randomized` graphs used during the evaluation phase. These graphs represent different levels of complexity and are utilized to test the framework under various constraints. A `README` file explains the structure and content of the graphs and provides guidance on how to use them for evaluations.

- **Protobuf:**  
  The `Protobuf` folder contains all necessary files for automatically generating the communication protocol between the various modules used in MARQ. This ensures seamless interaction between components. Detailed instructions on generating protocol buffers and integrating them into the modules are provided in the folder's `README` file.

- **QoRManager:**  
  The `QoRManager` folder contains the core functionality of MARQ. It includes the **Java** component that orchestrates the microservice chain and applies adaptation strategies. The QoRManager also implements various decision-making algorithms critical for optimizing the execution based on runtime data. The `README` file in this folder provides detailed instructions on how to configure, build, and execute the QoRManager.

- **Setup:**  
  This folder contains the necessary tools and instructions for deploying and running the entire MARQ framework, including the microservice chains, on a Linux server. It includes Docker files, scripts for setup, and step-by-step guides for creating and running the required environment.

----
## Project Dependencies
Below you can find the current dependencies on 3rd party libraries. A current version can always be found in the 
respective `build.gradle` files.

| Library                                              | Version | Description                                                                  |
|------------------------------------------------------|--------|------------------------------------------------------------------------------|
| **JDK**                                              | 18.0.2 | The minimum Java environment for running the software.                       |
| **Apache HttpClient**                                | 4.5.14 | A robust HTTP client for communication over HTTP protocols.                  |
| **Google Protocol Buffers (protobuf-gradle-plugin)** | 0.9.1  | A Gradle plugin for integrating Protocol Buffers into the build process.     |
| **Google Protocol Buffers (protobuf-java)**          | 3.21.7 | A library for serializing structured data using Protocol Buffers.            |
| **Mockito Core**                                     | 5.15.2 | Core library for creating mock objects in unit tests.                        |
| **Mockito All**                                      | 1.10.19 | A comprehensive package containing all Mockito modules for mocking in tests. |
| **Mockito Inline**                                   | 5.2.0  | Enables inline mocking capabilities with Mockito.                            |
| **Apache POI (POI-OOXML)**                           | 5.2.2  | A library for reading and writing Microsoft Office Open XML file formats.    |
| **Apache Log4j API**                                 | 2.17.2 | The API component of the Log4j logging framework.                            |
| **Apache Log4j Core**                                | 2.17.2 | The core implementation of the Log4j logging framework.                      |
| **JUnit Jupiter**                                    | 5.8.2  | The JUnit 5 framework for writing and executing tests.                       |
| **JUnit Jupiter Engine**                             | 5.8.2  | The test engine for running JUnit 5 tests.                                   |
| **Google Gson**                                      | 2.10.1 | A library for converting Java objects to JSON and vice versa.                |
| **Atomfinger ToUUID**                                | 1.0.1  | A utility library for converting data into UUIDs.                            |
| **JetBrains Annotations**                            | 20.1.0 | Annotations to aid in code analysis and improve code quality.                |
| **OpenJDK JMH Core**                                 | 1.36   | The core library for the Java Microbenchmarking Harness (JMH).               |
| **OpenJDK JMH Generator Annotation Processor**       | 1.36   | An annotation processor for generating benchmarking code with JMH.           |

----
## Hardware Requirements

The hardware dependencies refer to the execution of MARQ. Other additional microservice-based AI applications may have
different requirements, so please consider them if necessary. We refer to an execution of MARQ using the dummy 
application that demonstrates how MARQ works.
### Minimum Requirements

- **Processor**: 2 GHz dual-core.
- **Memory (RAM)**: 12 GB.
- **Storage**: 100 MB of available disk space, after installed JDK.
- **Network**: Available connection to the microservices.

### Recommended Requirements

- **Processor**: 3 GHz quad-core.
- **Memory (RAM)**: 16 GB.
- **Storage**: 250 MB of available disk space, after installed JDK.
- **Network**: Available connection to the microservices.

### Additional Requirements

- **Operating System**: Windows 11 and Ubuntu 20.04.

----
## License
The project is licensed under the Apache License Version 2.0. For more details, see the `LICENSE` file.

----
## Contributions and Special Thanks
In addition to Uwe Gropengießer, the author and visionary behind the MARQ Framework, many individuals have contributed to its development. Special thanks go to:

- **Prof. Dr. Max Mühlhäuser** for his invaluable support in idea generation and collaborative discussions.
- **Elias Dietz**, who provided development support and contributed to the conceptualization of MARQ through numerous constructive discussions.
- **Achref Doula, Florian Brandherm, Osama Abboud, and Xun Xiao**, who were always available for discussions and feedback.

We extend our gratitude to everyone who contributed to making the MARQ Framework possible.
