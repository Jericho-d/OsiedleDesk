--liquibase formatted sql

--changeset opencode:006-create-attachments-table
CREATE TABLE IF NOT EXISTS attachments
(
    id           BIGSERIAL PRIMARY KEY NOT NULL,
    issue_id     BIGINT                NOT NULL,
    filename     VARCHAR(255)          NOT NULL,
    content_type VARCHAR(100)          NOT NULL,
    data         BYTEA                 NOT NULL,
    size         BIGINT                NOT NULL,
    created_at   TIMESTAMP             NOT NULL,
    CONSTRAINT fk_attachments_issue FOREIGN KEY (issue_id) REFERENCES issues (id) ON DELETE CASCADE
);

CREATE INDEX idx_attachments_issue ON attachments (issue_id);
--rollback DROP TABLE attachments;
