--liquibase formatted sql

--changeset opencode:003-seed-admin-user
INSERT INTO users (username, password, role_name, created_at, updated_at)
VALUES ('admin', '$2a$12$rZVYXz.E0bQI0ROs/tV/4uC3sFuA4J.ypaVj6r.4K.zBC.I0R1.6i', 'ADMINISTRATOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
--rollback DELETE FROM users WHERE username = 'admin';