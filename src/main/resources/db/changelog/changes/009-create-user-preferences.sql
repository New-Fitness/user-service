--liquibase formatted sql
--changeset Joe Biden:009-create-user-preferences
CREATE TABLE IF NOT EXISTS user_preferences (
                                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                                preference_key VARCHAR(100) NOT NULL,
                                                preference_value VARCHAR(500),
                                                PRIMARY KEY (user_id, preference_key)
);