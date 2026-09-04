DROP TABLE IF EXISTS usuarios;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    cuenta_id_legacy BIGINT
);

INSERT INTO usuarios (username, password_hash, rol, cuenta_id_legacy)
VALUES ('steve', '$2a$10$23FJiRn5MKvJmtkUQrX8KuUYyHjlb1KrysDyyTDiIdBHIG29gSwJO', 'CLIENTE', 137)
ON CONFLICT (username) DO NOTHING;

INSERT INTO usuarios (username, password_hash, rol, cuenta_id_legacy)
VALUES ('maria', '$2a$10$23FJiRn5MKvJmtkUQrX8KuUYyHjlb1KrysDyyTDiIdBHIG29gSwJO', 'EMPLEADO', NULL);