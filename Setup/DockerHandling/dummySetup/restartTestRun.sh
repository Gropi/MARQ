#!/bin/bash

containerCount=10

.././stopAndRemoveAllContainer.sh

sudo .././BasecontainerHandler/Collector handleContainers -tp 2000 -p 3000 -t "130.83.163.116" -dc $containerCount
