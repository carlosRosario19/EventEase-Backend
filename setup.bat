@echo off
echo Starting Docker Compose...
docker compose up -d

:waitcontainer
timeout /t 5 >nul
docker inspect -f "{{.State.Running}}" oracle-xe 2>nul | find "true" >nul
if %errorlevel% neq 0 goto waitcontainer

echo Waiting for Oracle database listener to be available...
:waitlistener
timeout /t 5 >nul
docker exec oracle-xe sqlplus -L SYSTEM/test123@//localhost:1521/XE -S /NOLOG @- <nul 2>nul
if %errorlevel% neq 0 goto waitlistener

echo Copying init.sql into the container...
docker cp init.sql oracle-xe:/opt/oracle/scripts/init.sql

echo Executing the SQL script inside the container...
docker exec -it oracle-xe bash -c "sqlplus SYSTEM/test123@//localhost:1521/XE @/opt/oracle/scripts/init.sql"
