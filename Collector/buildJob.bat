@echo off

dotnet publish --output "%~dp0/build/" --runtime linux-x64 Collector.sln