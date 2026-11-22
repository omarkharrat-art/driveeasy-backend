@echo off
REM Builds all modules with JDK 21
title DriveEasy build
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
call mvn clean install
pause
