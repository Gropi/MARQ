@echo off

call ./Collector/buildDocker.bat
echo Collector ist gebaut

call ./QoRManagerOperator/buildDocker.bat
echo QoRManager ist gebaut