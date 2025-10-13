--liquibase formatted sql
--changeset Joe Biden:008-create-user-roles
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                          role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                                          assigned_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                          note VARCHAR(255),
                                          PRIMARY KEY (user_id, role_id)
);