@echo off

call "%~dp0\copySources.bat"

docker build -t yolo_object_labeling %~dp0