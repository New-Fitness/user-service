--liquibase formatted sql
--changeset Joe Biden:001-create-extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";