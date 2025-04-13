@echo off

:: Phase 1: Start only the database
echo Starting Oracle database container...
docker compose up -d oracle-xe

:: Wait for Oracle to be fully ready
echo Waiting for Oracle to be ready...
:waitHealthy
docker inspect -f "{{.State.Health.Status}}" oracle-xe | find "healthy" >nul
if %errorlevel% neq 0 (
    timeout /t 5 >nul
    goto waitHealthy
)

:: Additional wait for XEPDB1 to be ready
echo Waiting for XEPDB1 pluggable database...
:waitXEPDB1
docker exec oracle-xe bash -c "echo 'exit' | sqlplus -L SYSTEM/test123@//localhost:1521/XEPDB1" >nul 2>&1
if %errorlevel% neq 0 (
    timeout /t 5 >nul
    goto waitXEPDB1
)

:: Initialize the database
echo Initializing database schema...
docker cp init.sql oracle-xe:/opt/oracle/scripts/init.sql
docker exec oracle-xe bash -c "sqlplus SYSTEM/test123@//localhost:1521/XE @/opt/oracle/scripts/init.sql"

:: Clean up any existing Spring Boot container
echo Removing any existing eventease-backend container...
docker rm -f eventease-backend >nul 2>&1

:: Create uploads directory
echo Creating upload directory...
if not exist .\uploads mkdir .\uploads
icacls .\uploads /grant Everyone:(OI)(CI)F >nul

:: Phase 2: Start Spring Boot application
echo Building Spring Boot application...
docker build -t eventease-backend .

echo Starting Spring Boot application...
docker run -d ^
  --name eventease-backend ^
  -p 8080:8080 ^
  -v %cd%\uploads:/app/upload-dir ^
  -e SPRING_DATASOURCE_URL=jdbc:oracle:thin:@172.17.0.1:1521/XEPDB1 ^
  -e SPRING_DATASOURCE_USERNAME=EventEaseDBA ^
  -e SPRING_DATASOURCE_PASSWORD=test123 ^
  -e STORAGE_LOCATION=/app/upload-dir ^
  eventease-backend

:: Wait for Spring Boot to start
echo Waiting for Spring Boot to start...
set timeout=20
set elapsed=0

:waitSpringBoot
timeout /t 5 >nul
docker logs eventease-backend 2>&1 | find "Started Application" >nul
if %errorlevel% equ 0 goto appStarted

set /a elapsed+=5
if %elapsed% geq %timeout% (
    echo Timeout reached while waiting for Spring Boot
    docker logs eventease-backend
    exit /b 1
)
goto waitSpringBoot

:appStarted
echo Application started successfully!
echo Access it at: http://localhost:8080
pause