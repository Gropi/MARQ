FROM openjdk:18

COPY ./build/Server-1.0-SNAPSHOT.jar /usr/QoRManagerOperator/
WORKDIR /usr/QoRManagerOperator/

ENTRYPOINT ["java","-jar","/usr/QoRManagerOperator/Server-1.0-SNAPSHOT.jar", "-s", "-p", "5001"]