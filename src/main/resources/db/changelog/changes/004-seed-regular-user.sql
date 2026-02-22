--liquibase formatted sql

--changeset opencode:004-seed-regular-user
INSERT INTO users (username, password, role_name, created_at, updated_at)
VALUES ('user', '$2a$12$rZVYXz.E0bQI0ROs/tV/4uC3sFuA4J.ypaVj6r.4K.zBC.I0R1.6i', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;
--rollback DELETE FROM users WHERE username = 'user';
