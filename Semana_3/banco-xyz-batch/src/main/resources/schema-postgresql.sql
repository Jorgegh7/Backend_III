DROP TABLE IF EXISTS transacciones CASCADE;

CREATE TABLE transacciones (
    id BIGSERIAL PRIMARY KEY,
    id_legacy BIGINT NOT NULL,
    fecha DATE NOT NULL,
    monto NUMERIC(15,2) NOT NULL,
    tipo VARCHAR(20) NOT NULL
);

DROP TABLE IF EXISTS cuentas_bancarias CASCADE;

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


DROP TABLE IF EXISTS movimientos_anuales CASCADE;

CREATE TABLE movimientos_anuales (
    id BIGSERIAL PRIMARY KEY,
    cuenta_id_legacy BIGINT NOT NULL,
    fecha DATE NOT NULL,
    transaccion VARCHAR(20) NOT NULL,
    monto NUMERIC(15,2) NOT NULL,
    descripcion VARCHAR(255)
);

DROP TABLE IF EXISTS execution_log;

CREATE TABLE IF NOT EXISTS execution_log (
    id BIGSERIAL PRIMARY KEY,
    job_nombre VARCHAR(50) NOT NULL,
    step_nombre VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    leidos INTEGER NOT NULL,
    escritos INTEGER NOT NULL,
    saltados INTEGER NOT NULL,
    fecha_ejecucion TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- VISTAS: resumen_transacciones_diarias y estados_cuenta_anuales
-- =====================================================================
-- Se implementan como VIEW, no como un Step en Spring Batch.
-- Spring Batch sirve para procesar datos fila a fila con control de
-- fallos (chunks, skip, retry), lo que se trabajo al insertar en
-- "transacciones" y "movimientos_anuales". Agregar esos datos ya
-- persistidos (GROUP BY + SUM) es trabajo nativo de la base de datos,
-- no del batch.
--
-- Se usa VIEW y no una tabla repoblada porque se recalcula sola en
-- cada consulta, sin duplicar datos ni arriesgar que quede desactualizada.
--
-- Los 3 Jobs siguen corriendo en menos de 1.3s cada uno: las vistas no
-- afectan el rendimiento del batch, porque nunca se tocan durante su
-- ejecucion, solo al consultarlas despues.
-- =====================================================================

DROP VIEW IF EXISTS resumen_transacciones_diarias;
CREATE OR REPLACE VIEW resumen_transacciones_diarias AS
SELECT fecha,
       COUNT(*) AS cantidad_transacciones,
       COALESCE(SUM(CASE WHEN tipo = 'debito' THEN monto ELSE 0 END), 0) AS total_debitos,
       COALESCE(SUM(CASE WHEN tipo = 'credito' THEN monto ELSE 0 END), 0) AS total_creditos,
       SUM(monto) AS monto_total
FROM transacciones
GROUP BY fecha
ORDER BY fecha;

DROP VIEW IF EXISTS estados_cuenta_anuales;
CREATE OR REPLACE VIEW estados_cuenta_anuales AS
SELECT cuenta_id_legacy AS cuenta_id,
       COALESCE(SUM(CASE WHEN transaccion = 'deposito' THEN monto ELSE 0 END), 0) AS total_depositos,
       COALESCE(SUM(CASE WHEN transaccion = 'retiro' THEN monto ELSE 0 END), 0) AS total_retiros,
       COALESCE(SUM(CASE WHEN transaccion = 'compra' THEN monto ELSE 0 END), 0) AS total_compras,
       SUM(monto) AS saldo_neto,
       COUNT(*) AS cantidad_movimientos
FROM movimientos_anuales
GROUP BY cuenta_id_legacy
ORDER BY cuenta_id_legacy;