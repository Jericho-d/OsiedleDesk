--liquibase formatted sql

--changeset opencode:007-drop-priority-column
ALTER TABLE issues
    DROP COLUMN IF EXISTS priority;
--rollback ALTER TABLE issues ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';
