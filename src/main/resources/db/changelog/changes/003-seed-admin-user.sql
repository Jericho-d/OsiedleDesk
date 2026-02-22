--liquibase formatted sql

--changeset opencode:003-seed-admin-user
INSERT INTO users (username, password, role_name, created_at, updated_at)
VALUES ('admin', '$2b$12$ldRwbP.I0b8JiLScMU2kteRcaOOGCWF03jNcpHhYt478sVT0EnXC.', 'ADMINISTRATOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
--rollback DELETE FROM users WHERE username = 'admin';