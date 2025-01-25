#!/bin/bash

BASEDIR=$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")

sudo "${BASEDIR}/multiThreadedStop.sh" && \

wait

sudo "${BASEDIR}/multiThreadedRemoval.sh" && \

wait
