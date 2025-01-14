#!/bin/bash

containerCount=40
amountPumbaInstances=24

x=1
while [ $x -le $amountPumbaInstances ]
do
        nohup sudo ../pumba/pumba -l debug --random netem --duration 400m --tc-image gaiadocker/iproute2 delay --time 100 --jitter 100 --distribution pareto "re2:^Container\d*" &
        let "x+=1"

done
