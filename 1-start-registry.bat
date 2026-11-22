@echo off
REM Starts service-registry with JDK 21 (independent of the system JAVA_HOME)
title service-registry (8761)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0service-registry"
call mvn spring-boot:run
pause
