 #!/bin/bash

nohup java -Xms8192M -cp BenchmarkRunner -jar ./Execution/JMH-jmh.jar -t 4 -o jmh-results.csv -rf CSV > /dev/null 2>&1
