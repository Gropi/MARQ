#!/bin/bash

containerCount=40

.././stopAndRemoveAllContainer.sh

sudo .././BasecontainerHandler/Collector handleContainers -tp 2000 -p 3500 -t "130.83.163.116" -dc $containerCount
