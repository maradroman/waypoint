CREATE TABLE transfers (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id      UUID         NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    milestone_id UUID         NOT NULL REFERENCES milestones(id) ON DELETE CASCADE,
    amount       INTEGER      NOT NULL,
    type         VARCHAR(32)  NOT NULL DEFAULT 'allocate' CHECK (type IN ('allocate', 'withdraw', 'legacy')),
    comment      TEXT         NOT NULL DEFAULT '',
    timestamp    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_transfers_goal_id ON transfers(goal_id);
CREATE INDEX idx_transfers_milestone_id ON transfers(milestone_id);
