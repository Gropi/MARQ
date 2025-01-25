#!/bin/bash 

# DELETE OLD dummy_container
for i in $(sudo docker ps -a -q --filter ancestor=dummy_container:latest --format="{{.ID}}")
do 
	echo "Stop $i &"
	sudo docker stop $i &
done
wait
