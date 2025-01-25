#!/bin/bash 

BASEDIR=$(dirname $0)

# DELETE OLD PLAdummyN_B
for i in $(sudo docker ps -a -q --filter ancestor=dummy_container:latest --format="{{.ID}}")
do 
	echo "Stop $i"
	sudo docker stop $i
done

for i in $(sudo docker ps -a -q --filter ancestor=dummy_container:latest --format="{{.ID}}")
do 
	echo "Kill $i"
	sudo docker rm $i
done

sudo docker image rm dummy_container

sudo docker image prune -a

sudo docker build --no-cache -t dummy_container ${BASEDIR}/../Dummy

sudo docker rmi $(sudo docker images -f dangling=true -q)
