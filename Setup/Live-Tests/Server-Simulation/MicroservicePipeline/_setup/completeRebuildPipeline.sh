#!/bin/bash 

BASEDIR=$(dirname $0)

sudo "$BASEDIR/chmod_folders.sh"
sudo "$BASEDIR/deployResourcesToBuildPaths.sh"
sudo "$BASEDIR/rebuildContainers.sh"
