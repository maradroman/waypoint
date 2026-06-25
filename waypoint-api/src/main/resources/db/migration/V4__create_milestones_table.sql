CREATE TABLE milestones (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id      UUID         NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    title        VARCHAR(120) NOT NULL,
    cost         INTEGER      NOT NULL DEFAULT 0 CHECK (cost >= 0),
    details      TEXT         NOT NULL DEFAULT '',
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    completed    BOOLEAN      NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    sort_order   INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_milestones_goal_id ON milestones(goal_id);
