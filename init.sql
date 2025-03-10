-- Connect to the pluggable database (XEPDB1)
ALTER SESSION SET CONTAINER = XEPDB1;

-- Create the User
CREATE USER EventEaseDBA IDENTIFIED BY test123;

-- Grant Privileges
GRANT CONNECT, RESOURCE TO EventEaseDBA;
GRANT UNLIMITED TABLESPACE TO EventEaseDBA;

-- Commit the changes
COMMIT;