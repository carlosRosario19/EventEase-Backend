@echo off
setlocal enabledelayedexpansion

:: Configuration - adjust these as needed
set ORACLE_USER=SYSTEM
set ORACLE_PASSWORD=test123
set ORACLE_PDB=XEPDB1
set APP_USER=EventEaseDBA
set APP_PASSWORD=test123
set SPRING_TIMEOUT=60
set ORACLE_HEALTH_TIMEOUT=150
set ORACLE_PDB_TIMEOUT=150

:: Phase 1: Start only the database
echo [1/6] Starting Oracle database container...
docker compose up -d oracle-xe
if %errorlevel% neq 0 (
    echo ERROR: Failed to start Oracle container
    exit /b 1
)

:: Wait for Oracle container to be running
echo [2/6] Waiting for Oracle container to start (timeout: !ORACLE_HEALTH_TIMEOUT! seconds)...
set elapsed=0

:waitContainer
docker inspect -f "{{.State.Status}}" oracle-xe | find "running" >nul
if %errorlevel% equ 0 goto containerRunning

timeout /t 5 >nul
set /a elapsed+=5
if !elapsed! geq %ORACLE_HEALTH_TIMEOUT% (
    echo ERROR: Timeout waiting for Oracle container to start
    echo Last status: 
    docker inspect -f "{{.State.Status}}" oracle-xe
    echo Container logs:
    docker logs oracle-xe
    exit /b 1
)
goto waitContainer

:containerRunning
echo Oracle container is running, waiting for health check...

:: Wait for health check
set elapsed=0

:waitHealthy
docker inspect -f "{{.State.Health.Status}}" oracle-xe | find "healthy" >nul
if %errorlevel% equ 0 goto dbReady

timeout /t 5 >nul
set /a elapsed+=5
if !elapsed! geq %ORACLE_HEALTH_TIMEOUT% (
    echo ERROR: Timeout waiting for Oracle health check
    echo Current health status:
    docker inspect -f "{{.State.Health.Status}}" oracle-xe
    echo Container logs:
    docker logs oracle-xe
    exit /b 1
)
goto waitHealthy

:dbReady
echo Oracle is healthy, waiting for %ORACLE_PDB%...

:: Wait for PDB to be accessible
set elapsed=0

:waitPDB
echo Testing connection to %ORACLE_PDB%...
docker exec oracle-xe bash -c "echo 'exit' | sqlplus -L %ORACLE_USER%/%ORACLE_PASSWORD%@//localhost:1521/%ORACLE_PDB%" >nul 2>&1
if %errorlevel% equ 0 goto initDB

timeout /t 5 >nul
set /a elapsed+=5
if !elapsed! geq %ORACLE_PDB_TIMEOUT% (
    echo ERROR: Timeout waiting for %ORACLE_PDB% to be accessible
    echo Trying to get connection error:
    docker exec oracle-xe bash -c "echo 'exit' | sqlplus -L %ORACLE_USER%/%ORACLE_PASSWORD%@//localhost:1521/%ORACLE_PDB%"
    exit /b 1
)
goto waitPDB

:initDB
echo [3/6] Initializing database schema...
docker cp init.sql oracle-xe:/opt/oracle/scripts/init.sql
docker exec oracle-xe bash -c "sqlplus %ORACLE_USER%/%ORACLE_PASSWORD%@//localhost:1521/XE @/opt/oracle/scripts/init.sql"
if %errorlevel% neq 0 (
    echo ERROR: Failed to initialize database
    exit /b 1
)

:: Clean up any existing Spring Boot container
echo [4/6] Removing any existing eventease-backend container...
docker rm -f eventease-backend >nul 2>&1

:: Create uploads directory
echo [5/6] Creating upload directory...
if not exist .\uploads mkdir .\uploads
icacls .\uploads /grant Everyone:(OI)(CI)F >nul

:: Phase 2: Start Spring Boot application
echo [6/6] Building and starting Spring Boot application...
docker build -t eventease-backend .
if %errorlevel% neq 0 (
    echo ERROR: Failed to build Spring Boot application
    exit /b 1
)

echo Starting Spring Boot application (timeout: !SPRING_TIMEOUT! seconds)...
docker run -d ^
  --name eventease-backend ^
  -p 8080:8080 ^
  -v "%cd%\uploads:/app/upload-dir" ^
  -e SPRING_DATASOURCE_URL="jdbc:oracle:thin:@host.docker.internal:1521/%ORACLE_PDB%" ^
  -e SPRING_DATASOURCE_USERNAME="%APP_USER%" ^
  -e SPRING_DATASOURCE_PASSWORD="%APP_PASSWORD%" ^
  -e STORAGE_LOCATION="/app/upload-dir" ^
  eventease-backend
if %errorlevel% neq 0 (
    echo ERROR: Failed to start Spring Boot application
    exit /b 1
)

:: Wait for Spring Boot to start
set elapsed=0

:waitSpringBoot
timeout /t 5 >nul
docker logs eventease-backend 2>&1 | find "Started Application" >nul
if %errorlevel% equ 0 goto appStarted

set /a elapsed+=5
if !elapsed! geq %SPRING_TIMEOUT% (
    echo ERROR: Timeout reached while waiting for Spring Boot
    echo Last logs:
    docker logs eventease-backend --tail 50
    exit /b 1
)
goto waitSpringBoot

:appStarted
echo.
echo ============================================
echo Application started successfully!
echo Oracle XE running on port 1521
echo Spring Boot running on port 8080
echo.
echo Access the application at: http://localhost:8080
echo ============================================
pause