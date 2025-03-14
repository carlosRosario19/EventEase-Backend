-- Connect to the pluggable database (XEPDB1)
ALTER SESSION SET CONTAINER = XEPDB1;

-- Create the User
CREATE USER EventEaseDBA IDENTIFIED BY test123;

-- Grant Privileges
GRANT CONNECT, RESOURCE TO EventEaseDBA;
GRANT UNLIMITED TABLESPACE TO EventEaseDBA;

-- Switch to the EventEaseDBA schema
ALTER SESSION SET CURRENT_SCHEMA = EventEaseDBA;

-- Create Tables
CREATE TABLE USERS (
    USERNAME NVARCHAR2(128) CONSTRAINT USER_ID_PK PRIMARY KEY,
    PASSWORD NVARCHAR2(128) NOT NULL,
    ENABLED CHAR(1) CONSTRAINT USER_ENABLED_CK CHECK (ENABLED IN ('Y','N')) NOT NULL
);

CREATE TABLE AUTHORITY_TYPE (
    TYPE_DESCRIPTION NVARCHAR2(128) CONSTRAINT AUTHORITY_TYPE_UK UNIQUE
);

CREATE TABLE AUTHORITIES (
    USERNAME NVARCHAR2(128) NOT NULL,
    AUTHORITY NVARCHAR2(128) NOT NULL
);
ALTER TABLE AUTHORITIES ADD CONSTRAINT AUTHORITIES_UNIQUE UNIQUE (USERNAME, AUTHORITY);
ALTER TABLE AUTHORITIES ADD CONSTRAINT AUTHORITIES_USERNAME_FK FOREIGN KEY (USERNAME) REFERENCES USERS (USERNAME) ENABLE;
ALTER TABLE AUTHORITIES ADD CONSTRAINT AUTHORITIES_AUTHORITY_FK FOREIGN KEY (AUTHORITY) REFERENCES AUTHORITY_TYPE (TYPE_DESCRIPTION) ENABLE;

CREATE TABLE MEMBERS (
    MEMBER_ID NUMBER(10) CONSTRAINT MEMBER_ID_PK PRIMARY KEY,
    FIRST_NAME VARCHAR2(50) NOT NULL,
    LAST_NAME VARCHAR2(50) NOT NULL,
    PHONE VARCHAR2(20) NOT NULL,
    CREATED_AT DATE NOT NULL,
    USERNAME NVARCHAR2(128) CONSTRAINT MEMBER_USERNAME_FK REFERENCES USERS (USERNAME)
);

CREATE TABLE EVENTS (
    EVENT_ID NUMBER(10) CONSTRAINT EVENT_ID_PK PRIMARY KEY,
    TITLE VARCHAR2(50) NOT NULL,
    DESCRIPTION VARCHAR2(200) NOT NULL,
    CATEGORY VARCHAR2(50) NOT NULL,
    DATE_TIME DATE NOT NULL,
    LOCATION VARCHAR2(50) NOT NULL,
    TOTAL_TICKETS NUMBER(7) NOT NULL,
    TICKETS_SOLD NUMBER(7) NOT NULL,
    PRICE_PER_TICKET NUMBER(7) NOT NULL,
    MEMBER_ID NUMBER(10) CONSTRAINT EVENT_MEMBER_ID_FK REFERENCES MEMBERS (MEMBER_ID),
    CREATED_AT DATE NOT NULL
);

CREATE TABLE PAYMENTS (
    PAYMENT_ID NUMBER(10) CONSTRAINT PAYMENT_ID_PK PRIMARY KEY,
    AMOUNT NUMBER(10) NOT NULL,
    PAYMENT_DATE DATE NOT NULL,
    PAYMENT_METHOD VARCHAR2(200) NOT NULL,
    TRANSACTION_ID VARCHAR2(200) CONSTRAINT PAYMENT_TRANSACTION_UK UNIQUE,
    PAYER_EMAIL VARCHAR2(100) NOT NULL
);

CREATE TABLE TICKETS (
    TICKET_ID NUMBER(10) CONSTRAINT TICKET_ID_PK PRIMARY KEY,
    EVENT_ID NUMBER(10) CONSTRAINT TICKET_EVENT_FK REFERENCES EVENTS (EVENT_ID),
    PAYMENT_ID NUMBER(10) CONSTRAINT TICKET_PAYMENT_FK REFERENCES PAYMENTS (PAYMENT_ID),
    TICKET_CODE VARCHAR2(200) CONSTRAINT TICKET_CODE_UK UNIQUE,
    ATTENDEE_NAME VARCHAR2(200) NOT NULL,
    ATTENDEE_EMAIL VARCHAR2(200) NOT NULL,
    PURCHASE_AT DATE NOT NULL
);

-- Insert Authority Types
INSERT INTO AUTHORITY_TYPE (TYPE_DESCRIPTION) VALUES ('ROLE_MEMBER');

-- Insert User
INSERT INTO USERS (USERNAME, PASSWORD, ENABLED) VALUES ('user', '$2a$10$QmkM0KXiapvOLT5Frw6DVe8O5FmT/UZkfKIdKEU57OHhH6eGaXcQ2', 'Y');

INSERT INTO AUTHORITIES (USERNAME, AUTHORITY) VALUES ('user', 'ROLE_MEMBER');

-- Commit the changes
COMMIT;