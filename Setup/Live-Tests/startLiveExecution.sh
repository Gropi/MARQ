#!/bin/bash

BASEDIR=$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")

# Get current IP address.
IP_ADDRESS=$(hostname -I | awk '{print $1}')

# Stop old test run
sudo "$BASEDIR/Server-Simulation/live/stopAndRemoveAllContainer.sh" && \

# Stop network manipulation
sudo "$BASEDIR/Server-Simulation/live/stopNetworkingManipulation.sh" && \

# Open a new terminal window and execute the JAR file
gnome-terminal --title="QoR-Manager" -- bash -c "java -jar '$BASEDIR/QoR-Manager/Server-1.0-SNAPSHOT.jar' -an TestApplication -i $IP_ADDRESS -p 2000 -gl '$BASEDIR/../TestData/Graph/Paper/normal.graphml' -dm all -tr 10 -dlr 4500,500...6500 -sim; exec bash"

# Wait for 2 seconds, just in case the VM is slow!
sleep 2

echo ""
echo "---------------------------------"
echo ""
echo "We will now stop all existing dockers to clean up the system. Then we start the new test run."
echo ""
echo "---------------------------------"

sudo "$BASEDIR/Server-Simulation/live/stopAndRemoveAllContainer.sh" && \

# Start Test Servers
# Open a new terminal window and execute the JAR file
gnome-terminal --title="Server Simulation" -- bash -c "sudo '$BASEDIR/Server-Simulation/live/testrunServer.sh'; exec bash"

# Wait for 20 seconds, just in case the VM is slow!
sleep 20

# Start network manipulation
sudo "$BASEDIR/Server-Simulation/live/startNetworkManipulation.sh" &

echo ""
echo "---------------------------------"
echo ""
echo "Please enter a 'start' in the created terminal with the name 'QoR-Manager'."
echo ""
echo "---------------------------------"
