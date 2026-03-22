--liquibase formatted sql

--changeset opencode:005-add-account-lockout-columns
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS account_locked_until  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_failed_login     TIMESTAMP;

COMMENT ON COLUMN users.failed_login_attempts IS 'Number of consecutive failed login attempts';
COMMENT ON COLUMN users.account_locked_until IS 'Account is locked until this timestamp (null if not locked)';
COMMENT ON COLUMN users.last_failed_login IS 'Timestamp of the last failed login attempt';
