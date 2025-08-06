CREATE TABLE premium_numbers (
    phone_number VARCHAR2(20) PRIMARY KEY,
    cost_1 NUMBER(10,2),
    duration_1 NUMBER,
    cost_2 NUMBER(10,2),
    duration_2 NUMBER,
    cost_3 NUMBER(10,2),
    duration_3 NUMBER
);

CREATE TABLE voice_calls (
    id NUMBER PRIMARY KEY,
    caller_number VARCHAR2(20) NOT NULL,
    called_number VARCHAR2(20) NOT NULL,
    duration_seconds NUMBER,
    charged_amount NUMBER(10,2),
    call_timestamp TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_called_number FOREIGN KEY (called_number) REFERENCES premium_numbers(phone_number)
);
