#!/bin/bash

BASEDIR=$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")

# Stop network manipulation
sudo "$BASEDIR/stopNetworkingManipulation.sh" && \

# Take the transferred value or set the default value to 20
containerCount=${1:-20}
amountPumbaInstances=$(echo "$containerCount * 0.6" | bc)

x=1
while [ $x -le $amountPumbaInstances ]
do
	nohup sudo "$BASEDIR/../pumba/pumba" -l debug --random netem --duration 4000m --tc-image gaiadocker/iproute2 delay --time 100 --jitter 100 --distribution pareto "re2:^Container\d*" &
	#nohup sudo "$BASEDIR/../pumba/pumba" -l debug --random netem --duration 4000m --tc-image gaiadocker/iproute2 delay --distribution pareto --jitter 10 "re2:^Container\d*" &
        let "x+=1"

done
