@echo off

if not exist "%~dp0\build\" mkdir "%~dp0\build\"
xcopy /y "%~dp0\..\..\Collector\build\libs\Collector-1.0-SNAPSHOT.jar" "%~dp0\build\"