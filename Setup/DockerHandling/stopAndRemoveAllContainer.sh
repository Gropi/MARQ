#!/bin/bash

BASEDIR=$(dirname $0)

sudo ${BASEDIR}/multiThreadedStop.sh

wait

sudo ${BASEDIR}/multiThreadedRemoval.sh

wait
