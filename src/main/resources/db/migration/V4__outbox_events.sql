-- V4__outbox_events.sql
-- Purpose: Implement Outbox pattern for transactional event publishing

CREATE TABLE IF NOT EXISTS outbox_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(64) NOT NULL,
    aggregate_id    UUID NOT NULL,
    event_type      VARCHAR(64) NOT NULL,
    payload_json    TEXT NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
   attempts        INT NOT NULL DEFAULT 0,
    next_retry_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at         TIMESTAMPTZ,
    last_error      TEXT,

    CONSTRAINT outbox_events_status_chk CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status_retry
    ON outbox_events(status, next_retry_at)
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate
    ON outbox_events(aggregate_type, aggregate_id);
