@echo off
REM Starts api-gateway with JDK 21 (independent of the system JAVA_HOME)
title api-gateway (8080)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0api-gateway"
call mvn spring-boot:run
pause
