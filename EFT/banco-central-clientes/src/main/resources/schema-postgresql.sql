CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    cuenta_id_legacy BIGINT
);

INSERT INTO usuarios (username, password_hash, rol, cuenta_id_legacy)
VALUES ('steve', '$2a$10$23FJiRn5MKvJmtkUQrX8KuUYyHjlb1KrysDyyTDiIdBHIG29gSwJO', 'CLIENTE', 102)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    rol = EXCLUDED.rol,
    cuenta_id_legacy = EXCLUDED.cuenta_id_legacy;

INSERT INTO usuarios (username, password_hash, rol, cuenta_id_legacy)
VALUES ('alice', '$2a$10$23FJiRn5MKvJmtkUQrX8KuUYyHjlb1KrysDyyTDiIdBHIG29gSwJO', 'CLIENTE', 101)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    rol = EXCLUDED.rol,
    cuenta_id_legacy = EXCLUDED.cuenta_id_legacy;

INSERT INTO usuarios (username, password_hash, rol, cuenta_id_legacy)
VALUES ('maria', '$2a$10$23FJiRn5MKvJmtkUQrX8KuUYyHjlb1KrysDyyTDiIdBHIG29gSwJO', 'EMPLEADO', NULL)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    rol = EXCLUDED.rol,
    cuenta_id_legacy = EXCLUDED.cuenta_id_legacy;