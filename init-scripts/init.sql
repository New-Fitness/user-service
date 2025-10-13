DO
$$
    BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_catalog.pg_roles WHERE rolname = 'local_user'
        ) THEN
            CREATE ROLE local_user WITH LOGIN PASSWORD 'local_password';
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_catalog.pg_database WHERE datname = 'user_service_local'
        ) THEN
            CREATE DATABASE user_service_local OWNER local_user;
        END IF;
    END
$$;