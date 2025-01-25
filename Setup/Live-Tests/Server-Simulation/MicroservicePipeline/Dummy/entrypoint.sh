#!/bin/bash

set -e

if [[ "$1" = "serveAndCollect" ]]; then
    ./encapsulation/Encapsulation dummy -id "$2" &
    ./collector/Collector serveAndCollect -id "$2" &
else
    eval "$@"
fi

# prevent docker exit
tail -f /dev/null
