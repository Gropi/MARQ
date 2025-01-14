@echo off

if not exist "%~dp0\build\" mkdir "%~dp0\build\"
xcopy /y "%~dp0\..\..\..\Applications\object_labeling\" "%~dp0\build\"