-- ===========================================
-- V1: Roles, Usuarios y Tabla de Unión
-- ===========================================

-- Tabla de roles del sistema
CREATE TABLE IF NOT EXISTS roles (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(30)  NOT NULL UNIQUE
);

-- Tabla de usuarios (identidad de autenticación)
CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL    PRIMARY KEY,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de relación usuario-roles (M:M)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_users_email  ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- Seed: roles del sistema (sin dependencias)
-- El usuario administrador es creado por DataInitializer en tiempo de ejecución
INSERT INTO roles (name) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_TEACHER'),
    ('ROLE_STUDENT')
ON CONFLICT (name) DO NOTHING;
