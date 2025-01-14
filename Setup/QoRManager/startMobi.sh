#!/bin/bash

java -jar Server-1.0-SNAPSHOT.jar -i 130.83.163.46 -p 2000 -an TestApplication -gl ./TestData/Graph/graph_for_simplifier_small.graphml -dm mobidic -cl ./TestData/TestPictures/ -mpc 100 -ppc 1 -tl ./results/ -sim
