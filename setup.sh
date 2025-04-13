#!/bin/bash

# Phase 1: Start only the database
echo "Starting Oracle database container..."
docker compose up -d oracle-xe

# Wait for Oracle to be fully ready
echo "Waiting for Oracle to be ready..."
while [ "$(docker inspect -f '{{.State.Health.Status}}' oracle-xe)" != "healthy" ]; do
    sleep 5
done

# Additional wait for XEPDB1 to be ready
echo "Waiting for XEPDB1 pluggable database..."
until docker exec oracle-xe bash -c "echo 'exit' | sqlplus -L SYSTEM/test123@//localhost:1521/XEPDB1" 2>/dev/null; do
    sleep 5
done

# Initialize the database
echo "Initializing database schema..."
docker cp init.sql oracle-xe:/opt/oracle/scripts/init.sql
docker exec oracle-xe bash -c "sqlplus SYSTEM/test123@//localhost:1521/XE @/opt/oracle/scripts/init.sql"

# Clean up any existing Spring Boot container
echo "Removing any existing eventease-backend container..."
docker rm -f eventease-backend 2>/dev/null || true

# Create uploads directory with correct permissions
echo "Creating upload directory..."
mkdir -p ./uploads
chmod 777 ./uploads  # Ensure writable by container user

# Phase 2: Start Spring Boot application
echo "Building Spring Boot application..."
docker build -t eventease-backend .

echo "Starting Spring Boot application..."
docker run -d \
  --name eventease-backend \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/upload-dir \
  -e SPRING_DATASOURCE_URL=jdbc:oracle:thin:@172.17.0.1:1521/XEPDB1 \
  -e SPRING_DATASOURCE_USERNAME=EventEaseDBA \
  -e SPRING_DATASOURCE_PASSWORD=test123 \
  -e STORAGE_LOCATION=/app/upload-dir \
  eventease-backend

echo "Waiting for Spring Boot to start..."
timeout=20
elapsed=0
while ! docker logs eventease-backend 2>&1 | grep -q "Started Application"; do
    sleep 5
    elapsed=$((elapsed + 5))
    if [ $elapsed -ge $timeout ]; then
        echo "Timeout reached while waiting for Spring Boot"
        docker logs eventease-backend
        exit 1
    fi
done

echo "Application started successfully!"
echo "Access it at: http://localhost:8080"