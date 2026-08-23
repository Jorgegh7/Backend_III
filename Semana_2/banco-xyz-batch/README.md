# Banco XYZ - Migración de Procesos Batch Legacy

Proyecto académico que moderniza tres procesos batch legacy de un banco ficticio (Banco XYZ) usando **Spring Batch**, leyendo archivos CSV con datos "sucios" (montos inválidos, tipos corruptos, fechas mal formateadas), aplicando reglas de validación y transformación, y persistiendo los datos limpios en una base de datos PostgreSQL.

## Objetivo

Recrear tres procesos clave del sistema legacy del banco:

1. **Reporte de Transacciones Diarias** — procesa transacciones (débito/crédito), detectando montos inválidos.
2. **Cálculo de Intereses Mensuales** — aplica una tasa de interés según el tipo de cuenta (ahorro, préstamo, hipoteca) y calcula el saldo final.
3. **Generación de Estados de Cuenta Anuales** — procesa el historial de movimientos de cada cuenta (depósitos, retiros, compras).

Cada proceso está implementado como un **Job** de Spring Batch independiente, con su propio `Reader`, `Processor` y `Writer`, y aplica políticas personalizadas de manejo de errores y procesamiento paralelo.

## Estructura del proyecto

```
src/main/java/cl/duoc/bancoxyz/
├── config/          # Configuración de cada Job de Spring Batch y del pool de hilos
│   ├── TransaccionBatchConfig.java
│   ├── CuentaBancariaBatchConfig.java
│   ├── MovimientoAnualBatchConfig.java
│   ├── TaskExecutorConfig.java      # Pool de hilos para procesamiento paralelo
│   └── JobLauncherConfig.java       # Dispara los 3 Jobs en secuencia al arrancar
├── dto/             # DTOs "crudos" que recibe el Reader (todo como String)
├── entity/          # Modelos de datos ya validados, listos para persistir
├── processor/       # Lógica de validación y transformación de cada Job
├── policy/          # Políticas personalizadas de skip, retry y completion (chunk)
├── listener/        # Listeners de Job, Step y Skip (uno de cada tipo por Job)
├── exception/       # Excepciones custom usadas en las políticas de manejo de errores
└── util/            # DateParser y utilidades compartidas

src/main/resources/
├── data/                     # Archivos CSV de origen, organizados por semana
├── schema-postgresql.sql     # Definición de las tablas (se recrean en cada arranque)
└── application.properties    # Configuración de conexión a la base de datos
```

## Reglas de negocio y validación

| Job | Validaciones aplicadas |
|---|---|
| **Transacciones** | Fecha válida (múltiples formatos soportados); monto no vacío y mayor a cero; tipo debe ser `debito` o `credito` |
| **Intereses** | Tipo de cuenta debe ser `ahorro`, `prestamo` o `hipoteca`; edad entre 18 y 120 años; saldo no negativo ni vacío. Se calcula `interes` y `saldo_final` según la tasa del tipo de cuenta (ahorro 0.5%, préstamo 1.5%, hipoteca 0.8% mensual) |
| **Cuentas anuales** | Fecha válida; tipo de transacción debe ser `deposito`, `retiro` o `compra`; el monto debe ser positivo para depósitos y negativo para retiros/compras (el campo `transaccion` indica dirección real del dinero); descripción no puede superar 255 caracteres |

## Manejo de errores y procesamiento paralelo

### Políticas personalizadas (paquete `policy`)

En vez de la configuración declarativa simple (`.skip(Clase.class).skipLimit(n)`), el proyecto implementa tres políticas propias, reutilizadas por los 3 Jobs:

- **`BancoXyzSkipPolicy`**: decide si un registro se salta según el tipo de excepción, con límites distintos para errores de datos (`RegistroInvalidoException`) y de formato de fecha (`FechaInvalidaException`). Cualquier excepción no controlada (ej. un problema real de conexión) no se salta — deja que el Job falle, ya que no es un problema de dato corregible.
- **`BancoXyzRetryPolicy`**: reintenta hasta 3 veces cualquier excepción que **no** sea de negocio (pensada para fallas transitorias, como un corte momentáneo de conexión a la base de datos). Los datos inválidos nunca se reintentan, porque reintentar un dato corrupto no cambia el resultado.
- **`BancoXyzCompletionPolicy`**: cierra cada chunk por cantidad de ítems (5) o por tiempo transcurrido (2 segundos), lo que ocurra primero, en vez de un tamaño de chunk fijo.

### Listeners (paquete `listener`)

Cada Job tiene 3 listeners dedicados que registran su ciclo de vida en el log:

- **`JobExecutionListener`**: resumen al iniciar y finalizar el Job completo (estado final, ítems leídos/escritos/saltados por cada Step).
- **`StepExecutionListener`**: inicio y fin de cada Step, con sus contadores.
- **`SkipListener`**: detalle de cada ítem saltado, indicando en qué etapa ocurrió (lectura, procesamiento o escritura).

### Procesamiento paralelo (multithreading)

Cada Step se ejecuta con un `TaskExecutor` (`ThreadPoolTaskExecutor`) configurado con una política de escalamiento fija: exactamente **3 hilos** (`core = max = 3`), una cola de espera de 10 tareas para chunks adicionales, y `CallerRunsPolicy` como salvaguarda si el pool y la cola estuvieran saturados. El `FlatFileItemReader` se envuelve en un `SynchronizedItemStreamReader` para garantizar lectura segura entre hilos.

*Nota: el uso de multithreading implica que la funcionalidad de reinicio preciso de Spring Batch (retomar exactamente desde la línea donde falló) queda limitada, ya que varios hilos leen el archivo simultáneamente. Esto está documentado y es un comportamiento esperado, no un error — no afecta el resultado final del procesamiento.*

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
3. Cada Job procesa su CSV correspondiente, con chunks de 5 ítems procesados en paralelo (3 hilos), valida cada registro según sus políticas personalizadas, y persiste los válidos en la base de datos.

### 4. Verificar los resultados

Los datos procesados quedan disponibles en las tablas `transacciones`, `cuentas_bancarias` y `movimientos_anuales`. El log de consola muestra, por cada Job, un resumen de ítems leídos/escritos/saltados (vía los listeners), y el detalle de cada salto puntual. También se puede revisar el detalle de cada ejecución en las tablas de metadata de Spring Batch (`BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`, etc.), generadas automáticamente al arrancar.

## Nota sobre el alcance

Esta entrega cubre el procesamiento de los conjuntos de datos de **semana_1** y **semana_2**, incorporando en la segunda semana las técnicas avanzadas descritas arriba (políticas personalizadas, listeners, multithreading), junto con validaciones adicionales para manejar campos vacíos y múltiples formatos de fecha presentes en el dataset de semana_2.
