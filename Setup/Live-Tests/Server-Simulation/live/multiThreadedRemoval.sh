#!/bin/bash 

# DELETE OLD dummy_container
for i in $(sudo docker ps -a -q --filter ancestor=dummy_container:latest --format="{{.ID}}")
do 
	echo "Kill $i &"
	sudo docker rm $i &
done

