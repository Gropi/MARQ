#!/bin/bash

containerCount=40

.././stopAndRemoveAllContainer.sh

sudo .././BasecontainerHandler/Collector handleContainers -tp 2000 -p 3250 -t "10.130.22.34" -dc $containerCount
