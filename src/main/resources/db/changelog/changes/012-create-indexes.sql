--liquibase formatted sql
--changeset Joe Biden:012-create-indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_profiles_region ON user_profiles(region);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_user_status_translations_desc ON user_status_translations(description);
CREATE INDEX IF NOT EXISTS idx_gender_translations_desc ON gender_translations(description);
CREATE INDEX IF NOT EXISTS idx_permission_translations_desc ON permission_translations(description);
CREATE INDEX IF NOT EXISTS idx_role_translations_desc ON role_translations(description);
