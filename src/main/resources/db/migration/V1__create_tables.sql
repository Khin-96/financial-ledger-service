-- Event Store (Write Model)
CREATE TABLE ledger_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4),
    currency VARCHAR(3),
    metadata JSONB,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL,
    UNIQUE(account_id, version)
);

CREATE INDEX idx_account_id ON ledger_events(account_id);
CREATE INDEX idx_occurred_at ON ledger_events(occurred_at);

-- Read Model (Materialized Balances)
CREATE TABLE account_balances (
    account_id UUID PRIMARY KEY,
    balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL,
    last_event_id UUID,
    last_event_version BIGINT,
    updated_at TIMESTAMP
);

-- Transfers (Saga State)
CREATE TABLE transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_transfers_from ON transfers(from_account_id);
CREATE INDEX idx_transfers_to ON transfers(to_account_id);
CREATE INDEX idx_transfers_status ON transfers(status);
