CREATE TABLE completions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id      UUID        NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    milestone_id UUID        NOT NULL REFERENCES milestones(id) ON DELETE CASCADE,
    amount       INTEGER     NOT NULL CHECK (amount >= 0),
    timestamp    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_completions_goal_id ON completions(goal_id);
CREATE INDEX idx_completions_milestone_id ON completions(milestone_id);
