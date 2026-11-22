@echo off
REM Starts location-service with JDK 21 (independent of the system JAVA_HOME)
title location-service (8083)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0location-service"
call mvn spring-boot:run
pause
