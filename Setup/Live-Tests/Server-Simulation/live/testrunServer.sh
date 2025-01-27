#!/bin/bash

BASEDIR=$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")

# Get current IP address.
IP_ADDRESS=$(hostname -I | awk '{print $1}')


# Define the amount of containers to create.
# Take the transferred value or set the default value to 20
containerCount=${1:-20}

sudo "$BASEDIR/../BasecontainerHandler/Collector" handleContainers -tp 2000 -p 3250 -t $IP_ADDRESS -dc $containerCount
