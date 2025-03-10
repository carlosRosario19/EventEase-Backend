#!/bin/bash

# Start Docker Compose
docker compose up -d

# Wait until the Oracle container is running
echo "Waiting for Oracle container to start..."
until docker exec oracle-xe ls /opt/oracle; do
    sleep 5
done

# Wait until the Oracle listener is ready
echo "Waiting for Oracle database listener to be available..."
until docker exec oracle-xe bash -c "echo 'exit' | sqlplus -L SYSTEM/test123@//localhost:1521/XE" 2>/dev/null; do
    sleep 5
done

# Copy the init.sql file into the container
docker cp init.sql oracle-xe:/opt/oracle/scripts/init.sql

# Execute the SQL script inside the container
docker exec -it oracle-xe bash -c "sqlplus SYSTEM/test123@//localhost:1521/XE @/opt/oracle/scripts/init.sql"
