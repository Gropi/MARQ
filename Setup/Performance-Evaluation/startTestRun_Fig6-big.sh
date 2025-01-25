 #!/bin/bash

nohup java -Xms8192M -jar ./Execution/JMH-jmh.jar testrun.MARQ.DecisionMaker.DecisionMakerBenchmarksBig -t 4 -o jmh-results.csv -rf CSV > /dev/null 2>&1
