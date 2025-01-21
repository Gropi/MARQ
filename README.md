# MARQ - Engineering <ins>M</ins>ission-Critical <ins>A</ins>I-based Software with Automated <ins>R</ins>esult <ins>Q</ins>uality Adaptation

Welcome to the repository of the MARQ Framework, a system for executing mission-critical AI-based microservice chains with the goal of meeting various constraints. This framework leverages the ability to measure the Quality of Result (QoR) and adapt the microservice chain accordingly.

## Reference
If you wish to use this work for scientific purposes, please note that it was published at the
25th IEEE/ACM International Conference on Software Engineering (ICSE) in 2025.

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

## License
The project is licensed under the Apache License Version 2.0. For more details, see the `LICENSE` file.

## Contributions and Special Thanks
In addition to Uwe Gropengießer, the author and visionary behind the MARQ Framework, many individuals have contributed to its development. Special thanks go to:

- **Prof. Dr. Max Mühlhäuser** for his invaluable support in idea generation and collaborative discussions.
- **Elias Dietz**, who provided development support and contributed to the conceptualization of MARQ through numerous constructive discussions.
- **Achref Doula, Florian Brandherm, Osama Abboud, and Xun Xiao**, who were always available for discussions and feedback.

We extend our gratitude to everyone who contributed to making the MARQ Framework possible.
