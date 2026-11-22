@echo off
REM Starts the whole backend in the recommended order:
REM registry -> config server -> business services -> gateway
REM Each service opens in its own window. Close a window to stop that service.
cd /d "%~dp0"

echo Starting Zipkin (Docker Desktop must be running)...
docker compose up -d

echo [1/6] service-registry...
start "" "%~dp01-start-registry.bat"
timeout /t 25 /nobreak >nul

echo [2/6] config-server...
start "" "%~dp02-start-config.bat"
timeout /t 25 /nobreak >nul

echo [3-5/6] vehicule-service, client-service, location-service...
start "" "%~dp03-start-vehicule.bat"
start "" "%~dp04-start-client.bat"
start "" "%~dp05-start-location.bat"
timeout /t 20 /nobreak >nul

echo [6/6] api-gateway...
start "" "%~dp06-start-gateway.bat"

echo.
echo Done. Wait ~30 s, then check http://localhost:8761
pause
