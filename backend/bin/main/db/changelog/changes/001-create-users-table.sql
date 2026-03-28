--liquibase formatted sql

--changeset opencode:001-create-users-table
CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL PRIMARY KEY NOT NULL,
    username   VARCHAR(50)           NOT NULL UNIQUE,
    password   VARCHAR(255)          NOT NULL,
    role_name  VARCHAR(20)           NOT NULL,
    created_at TIMESTAMP             NOT NULL,
    updated_at TIMESTAMP             NOT NULL
);

CREATE INDEX idx_username ON users (username);
