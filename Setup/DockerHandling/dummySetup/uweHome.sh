#!/bin/bash

containerCount=10

.././stopAndRemoveAllContainer.sh

sudo .././BasecontainerHandler/Collector handleContainers -tp 2000 -p 3250 -t "10.130.22.82" -dc $containerCount -cm 1
