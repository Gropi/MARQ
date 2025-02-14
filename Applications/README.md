# Applications Folder Documentation

## **Overview**
This folder contains example AI scripts for various microservices we used in MARQ framework as described in the MARQ paper. 
Each subdirectory represents an independent service designed to perform specific tasks. These microservices are 
structured to support modular integration and facilitate different functionalities. You can run MARQ without this example
applications by using a dummy setup (see `Setup` in the main folder).

## **Folder Structure**
```
Applications
│
├── FaceDetection
│   ├── face_detection_webservice.py   # Web service for face detection
│
├── object_labeling
│   ├── .gitignore                     # Git ignore file for dependencies and logs
│   ├── main.py                         # Main application script for object labeling
│   ├── requirements.txt                # Dependencies for the object labeling service
│
├── object_recognition
│   ├── coco_classes.json               # JSON file mapping COCO dataset classes
│   ├── detect_image.py                 # Script for object recognition
```

## **Microservices Description**

### **1. Face Detection (`FaceDetection/`)**
- Implements a web service for face detection.
- The main script `face_detection_webservice.py` runs a server that processes image inputs.

### **2. Object Labeling (`object_labeling/`)**
- Handles object labeling within images.
- The `main.py` script contains the core functionality.
- Dependencies required are listed in `requirements.txt`.

### **3. Object Recognition (`object_recognition/`)**
- Implements object recognition using pre-trained models.
- The `detect_image.py` script processes images and detects objects based on `coco_classes.json`.

## **Purpose**
- These services are exclusively used to provide the microservices necessary for MARQ, as outlined in the MARQ paper.
- Combining each service with the `Encapsulation`, each service operates independently and can be deployed separately depending on the requirements.

## **Usage**
- Ensure that the required dependencies are installed before running each microservice.
- Services can be deployed as REST APIs or executed as standalone scripts, depending on the setup requirements.

