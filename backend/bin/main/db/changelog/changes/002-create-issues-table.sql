--liquibase formatted sql

--changeset opencode:002-create-issues-table
CREATE TABLE IF NOT EXISTS issues
(
    id          BIGSERIAL PRIMARY KEY NOT NULL,
    creator_id  BIGINT                NOT NULL,
    title       VARCHAR(200)          NOT NULL,
    description TEXT                  NOT NULL,
    status      VARCHAR(20)           NOT NULL,
    priority    VARCHAR(20)           NOT NULL,
    assignee    VARCHAR(50),
    is_sent     BOOLEAN               NOT NULL,
    sent_at     TIMESTAMP,
    created_at  TIMESTAMP             NOT NULL,
    updated_at  TIMESTAMP             NOT NULL,
    CONSTRAINT fk_issues_creator FOREIGN KEY (creator_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_issues_status ON issues (status);
CREATE INDEX idx_issues_creator ON issues (creator_id);
CREATE INDEX idx_issues_created_at ON issues (created_at);
--rollback DROP TABLE issues;