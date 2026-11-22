@echo off
REM Starts client-service with JDK 21 (independent of the system JAVA_HOME)
title client-service (8082)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0client-service"
call mvn spring-boot:run
pause
