CREATE TABLE planned_funds (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id    UUID        NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    date       DATE        NOT NULL,
    amount     INTEGER     NOT NULL CHECK (amount > 0),
    is_deleted BOOLEAN     NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (goal_id, date)
);

CREATE INDEX idx_planned_funds_goal_id ON planned_funds(goal_id);
CREATE INDEX idx_planned_funds_is_deleted ON planned_funds(is_deleted);
