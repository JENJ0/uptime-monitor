CREATE TABLE monitors (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    interval_seconds INTEGER NOT NULL CHECK (interval_seconds >= 30),
    timeout_ms INTEGER NOT NULL CHECK (timeout_ms BETWEEN 100 AND 10000),
    expected_status INTEGER NOT NULL CHECK (expected_status BETWEEN 100 AND 599),
    created_at TIMESTAMPTZ NOT NULL
);