CREATE TABLE bug_report_attachments (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bug_report_id  UUID         NOT NULL REFERENCES bug_reports(id) ON DELETE CASCADE,
    filename       VARCHAR(255) NOT NULL,
    content_type   VARCHAR(128) NOT NULL,
    size_bytes     BIGINT       NOT NULL,
    storage_key    VARCHAR(512) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_bug_report_attachments_bug_report_id ON bug_report_attachments(bug_report_id);
