@echo off
REM Starts config-server with JDK 21 (independent of the system JAVA_HOME)
title config-server (8888)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0config-server"
call mvn spring-boot:run
pause
