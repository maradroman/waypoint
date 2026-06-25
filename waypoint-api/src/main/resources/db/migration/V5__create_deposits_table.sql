CREATE TABLE deposits (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id    UUID        NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    amount     INTEGER     NOT NULL CHECK (amount > 0),
    note       TEXT        NOT NULL DEFAULT '',
    timestamp  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_deposits_goal_id ON deposits(goal_id);
