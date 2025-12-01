-- schema.sql
CREATE TABLE IF NOT EXISTS messages (
    id      VARCHAR(60)  DEFAULT RANDOM_UUID() PRIMARY KEY,
    text    VARCHAR      NOT NULL
);

CREATE TABLE IF NOT EXISTS task (
    id      VARCHAR(60)  DEFAULT RANDOM_UUID() PRIMARY KEY,
    title    VARCHAR(255)      NOT NULL,
    description    VARCHAR(1000),
    status VARCHAR(25) NOT NULL DEFAULT 'PENDING',
    created_by_id VARCHAR(60) NOT NULL,

    CONSTRAINT chk_task_status_valid CHECK (status in ('PENDING', 'IN_PROGRESS', 'COMPLETED'))
);