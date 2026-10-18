CREATE TABLE check_results (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    monitor_id BIGINT NOT NULL REFERENCES monitors(id) ON DELETE CASCADE,
    checked_at TIMESTAMPTZ NOT NULL,
    duration_ms BIGINT NOT NULL CHECK (duration_ms >= 0),
    http_status INTEGER,
    successful BOOLEAN NOT NULL,
    error_type VARCHAR(50),
    CONSTRAINT check_error_type_match 
    CHECK ((successful AND error_type IS NULL) OR (NOT successful AND error_type IS NOT NULL))
);

CREATE INDEX idx_check_results_monitor_time 
    ON check_results(monitor_id, checked_at DESC);