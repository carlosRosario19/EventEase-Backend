# Oracle XE Database Setup

This project includes scripts to set up and configure an Oracle XE database using Docker.

## Prerequisites

Before running the setup, ensure you have the following installed:

[Docker Desktop](https://www.docker.com/products/docker-desktop/)

For **Windows users**, running scripts in PowerShell may require changing the execution policy:
```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## Setup Instructions

### Linux & macOS
1. Open a terminal.
2. Navigate to the project directory.
3. Run the setup script:
   ```sh
   ./setup.sh
   ```

### Windows (Command Prompt or PowerShell)
1. Open **Command Prompt** or **PowerShell** as Administrator.
2. Navigate to the project directory:
3. Run the setup script:
   ```bat
   setup.bat
   ```

## What the Setup Script Does

1. Starts the Oracle XE container using Docker Compose.
2. Waits for the database listener to be ready.
3. Copies the `init.sql` script into the running container.
4. Executes `init.sql` to initialize the database.

## Stopping and Removing the Container

To stop and remove the Oracle XE container:

### Linux & macOS & Windows
```sh
docker compose down
```

This README provides setup instructions for all users, ensuring a smooth installation of the Oracle XE database on different operating systems.

