@echo off

call "%~dp0\copySources.bat"

docker build -t qor_manager_operator %~dp0