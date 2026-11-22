@echo off
REM Starts vehicule-service with JDK 21 (independent of the system JAVA_HOME)
title vehicule-service (8081)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0vehicule-service"
call mvn spring-boot:run
pause
