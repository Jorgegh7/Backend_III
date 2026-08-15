DROP TABLE IF EXISTS transacciones;

CREATE TABLE transacciones (
    id BIGSERIAL PRIMARY KEY,
    id_legacy BIGINT NOT NULL,
    fecha DATE NOT NULL,
    monto NUMERIC(15,2) NOT NULL,
    tipo VARCHAR(20) NOT NULL
);

DROP TABLE IF EXISTS cuentas_bancarias;

CREATE TABLE cuentas_bancarias (
    id BIGSERIAL PRIMARY KEY,
    cuenta_id_legacy BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    saldo NUMERIC(15,2) NOT NULL,
    edad INTEGER NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    interes NUMERIC(15,2) NOT NULL,
    saldo_final NUMERIC(15,2) NOT NULL
);


DROP TABLE IF EXISTS movimientos_anuales;

CREATE TABLE movimientos_anuales (
    id BIGSERIAL PRIMARY KEY,
    cuenta_id_legacy BIGINT NOT NULL,
    fecha DATE NOT NULL,
    transaccion VARCHAR(20) NOT NULL,
    monto NUMERIC(15,2) NOT NULL,
    descripcion VARCHAR(255)
);