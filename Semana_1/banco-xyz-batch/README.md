# Banco XYZ - Migración de Procesos Batch Legacy

Proyecto académico que moderniza tres procesos batch legacy de un banco ficticio (Banco XYZ) usando **Spring Batch**, leyendo archivos CSV con datos "sucios" (montos inválidos, tipos corruptos, fechas mal formateadas), aplicando reglas de validación y transformación, y persistiendo los datos limpios en una base de datos PostgreSQL.

## Objetivo

Recrear tres procesos clave del sistema legacy del banco:

1. **Reporte de Transacciones Diarias** — procesa transacciones (débito/crédito), detectando montos inválidos.
2. **Cálculo de Intereses Mensuales** — aplica una tasa de interés según el tipo de cuenta (ahorro, préstamo, hipoteca) y calcula el saldo final.
3. **Generación de Estados de Cuenta Anuales** — procesa el historial de movimientos de cada cuenta (depósitos, retiros, compras).

Cada proceso está implementado como un **Job** de Spring Batch independiente, con su propio `Reader`, `Processor` y `Writer`, y aplica políticas de **skip** para descartar registros inválidos sin detener el proceso completo.

## Estructura del proyecto

```
src/main/java/cl/duoc/bancoxyz/
├── config/          # Configuración de cada Job de Spring Batch (Reader, Writer, Step, Job)
│   ├── TransaccionBatchConfig.java
│   ├── CuentaBancariaBatchConfig.java
│   ├── MovimientoAnualBatchConfig.java
│   └── JobLauncherConfig.java      # Dispara los 3 Jobs en secuencia al arrancar
├── dto/             # DTOs "crudos" que recibe el Reader (todo como String)
├── entity/          # Modelos de datos ya validados, listos para persistir
├── processor/       # Lógica de validación y transformación de cada Job
├── exception/        # Excepciones custom usadas en las políticas de skip
└── util/            # DateParser y utilidades compartidas

src/main/resources/
├── data/semana_1/           # Archivos CSV de origen (transacciones, intereses, cuentas_anuales)
├── schema-postgresql.sql    # Definición de las tablas (se recrean en cada arranque)
└── application.properties   # Configuración de conexión a la base de datos
```

## Reglas de negocio y validación

| Job | Validaciones aplicadas |
|---|---|
| **Transacciones** | Fecha válida; monto debe ser mayor a cero |
| **Intereses** | Tipo de cuenta debe ser `ahorro`, `prestamo` o `hipoteca`; edad entre 18 y 90; saldo no negativo. Se calcula `interes` y `saldo_final` según la tasa del tipo de cuenta (ahorro 0.5%, préstamo 1.5%, hipoteca 0.8% mensual) |
| **Cuentas anuales** | Fecha válida; tipo de transacción debe ser `deposito`, `retiro` o `compra`; el monto debe ser positivo para depósitos y negativo para retiros/compras; descripción no puede superar 255 caracteres |

Los registros que no cumplen estas reglas se **descartan (skip)** sin detener el Job, y quedan registrados en las tablas de metadata de Spring Batch para trazabilidad.

## Tecnologías

- **Java 17** / Spring Boot 4.0.7 / Spring Batch 6.0.4
- **PostgreSQL** (alojado en [Neon](https://neon.tech))
- **Maven**
- Persistencia vía **JDBC** (`JdbcBatchItemWriter`), sin JPA/Hibernate

## Cómo ejecutar el proyecto

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd banco-xyz-batch
```

### 2. Configurar la base de datos

El proyecto se conecta a una base de datos PostgreSQL ya creada (alojada en Neon). La URL y el usuario ya están configurados en `application.properties`; solo falta la contraseña, que se pasa como **variable de entorno** por seguridad — nunca se guarda en el código versionado.

En IntelliJ: `Run → Edit Configurations → Environment variables`, y agrega:

```
DB_PASSWORD=<contraseña de la base de datos>
```

*(La contraseña se comparte por un canal privado con el equipo, no se sube al repositorio.)*

### 3. Ejecutar la aplicación

Corre la clase principal `BancoXyzBatchApplication`. Al arrancar:

1. Se ejecuta automáticamente `schema-postgresql.sql`, recreando las tablas `transacciones`, `cuentas_bancarias` y `movimientos_anuales`.
2. Se disparan los **3 Jobs en secuencia**: transacciones → intereses → estados de cuenta anuales.
3. Cada Job procesa su CSV correspondiente desde `src/main/resources/data/semana_1/`, valida cada registro, salta los inválidos y persiste los válidos en la base de datos.

### 4. Verificar los resultados

Los datos procesados quedan disponibles en las tablas `transacciones`, `cuentas_bancarias` y `movimientos_anuales` de la base de datos. También se puede revisar el detalle de cada ejecución (registros leídos, escritos, saltados) en las tablas de metadata de Spring Batch (`BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`, etc.), generadas automáticamente al arrancar.

## Nota sobre el alcance

Esta entrega cubre el procesamiento de **semana_1** (el conjunto de datos con menor cantidad de anomalías). El diseño del `DateParser` y las validaciones están preparados para extenderse en próximas semanas al procesar `semana_2` y `semana_3`, que incluyen datos con mayor grado de corrupción (múltiples formatos de fecha, valores fuera de rango, etc.).
