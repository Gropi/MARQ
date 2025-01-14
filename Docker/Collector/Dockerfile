FROM openjdk:18

COPY ./build/Collector-1.0-SNAPSHOT.jar /usr/QoRCollector/
WORKDIR /usr/QoRCollector/

ENTRYPOINT ["java", "-jar", "/usr/QoRCollector/Collector-1.0-SNAPSHOT.jar", "-p", "5001"] 
