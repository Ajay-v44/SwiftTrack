-- liquibase formatted sql

-- changeset swifttrack-dev:005-enable-pgvector-extension
CREATE EXTENSION IF NOT EXISTS vector;

-- changeset swifttrack-dev:005-create-driver-memory-table
CREATE TABLE IF NOT EXISTS driver_memory (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id       UUID NOT NULL,
    summary         TEXT NOT NULL,
    embedding       vector(768) NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_driver_memory_driver_id ON driver_memory(driver_id);
