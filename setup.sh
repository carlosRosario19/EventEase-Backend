#!/bin/bash

# Start Docker Compose
docker compose up -d

# Wait for the Oracle container to be healthy
echo "Waiting for Oracle container to be healthy..."
until [ "$(docker inspect --format '{{json .State.Health.Status}}' oracle-xe)" == "\"healthy\"" ]; do
    sleep 5
done

# Verify Oracle is running by checking for a valid SQL response
echo "Checking Oracle database readiness..."
until docker exec oracle-xe bash -c "echo 'SELECT 1 FROM DUAL;' | sqlplus -L SYSTEM/test123@//localhost:1521/XE" 2>/dev/null | grep -q "1"; do
    sleep 5
done

# Copy the init.sql file into the container
docker cp init.sql oracle-xe:/opt/oracle/scripts/init.sql

# Execute the SQL script inside the container
docker exec -i oracle-xe bash -c "sqlplus SYSTEM/test123@//localhost:1521/XE @/opt/oracle/scripts/init.sql"

echo "Setup completed successfully!"
