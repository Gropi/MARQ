#!/bin/bash 

# DELETE OLD PLAN_B
for i in $(sudo docker ps -a -q --filter ancestor=plan_b:latest --format="{{.ID}}")
do 
	echo "Stop $i &"
	sudo docker stop $i &
done

# DELETE OLD STASI_UWE
for i in $(sudo docker ps -a -q --filter ancestor=stasi_uwe:latest --format="{{.ID}}")
do 
	echo "Stop $i &"
	sudo docker stop $i &
done

# DELETE OLD KLEINE_KRATZE
for i in $(sudo docker ps -a -q --filter ancestor=kleine_kratze:latest --format="{{.ID}}")
do 
	echo "Stop $i &"
	sudo docker stop $i &
done

# DELETE OLD HAUPTSACHE_SCHNAPS
for i in $(sudo docker ps -a -q --filter ancestor=hauptsache_schnaps:latest --format="{{.ID}}")
do 
	echo "Stop $i &"
	sudo docker stop $i
done

# DELETE OLD SCHNAPSDROSSEL_QUINTIN
for i in $(sudo docker ps -a -q --filter ancestor=schnapsdrossel_quintin:latest --format="{{.ID}}")
do 
	echo "Stop $i &"
	sudo docker stop $i &
done
wait
