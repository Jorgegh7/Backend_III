# Banco XYZ - Migración de Procesos Batch Legacy

Proyecto académico que moderniza tres procesos batch legacy de un banco ficticio (Banco XYZ) usando **Spring Batch**, leyendo archivos CSV con datos "sucios" (montos inválidos, tipos corruptos, fechas mal formateadas, nombres inválidos), aplicando reglas de validación y transformación, y persistiendo los datos limpios en una base de datos PostgreSQL.

## Objetivo

Recrear tres procesos clave del sistema legacy del banco:

1. **Reporte de Transacciones Diarias** — procesa transacciones (débito/crédito), detectando montos y fechas inválidas.
2. **Cálculo de Intereses Mensuales** — aplica una tasa de interés según el tipo de cuenta (ahorro, préstamo, hipoteca) y calcula el saldo final.
3. **Generación de Estados de Cuenta Anuales** — procesa el historial de movimientos de cada cuenta (depósitos, retiros, compras, pagos).

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
├── schema-postgresql.sql     # Tablas, execution_log y vistas de resumen (se recrean en cada arranque)
└── application.properties    # Configuración de conexión a la base de datos
```

## Reglas de negocio y validación

| Job | Validaciones aplicadas |
|---|---|
| **Transacciones** | Fecha válida (formatos `yyyy-MM-dd`, `yyyy/MM/dd`, `dd-MM-yyyy`, `dd/MM/yyyy`); monto no vacío y mayor a cero; tipo debe ser `debito` o `credito` |
| **Intereses** | Nombre no vacío ni `"Unknown"`; saldo y edad no vacíos; tipo de cuenta debe ser `ahorro`, `prestamo` o `hipoteca`; edad entre 18 y 120 años; saldo no negativo. Se calcula `interes` y `saldo_final` según la tasa del tipo de cuenta (ahorro 0.5%, préstamo 1.5%, hipoteca 0.8% mensual) |
| **Cuentas anuales** | Fecha válida; monto no vacío y distinto de cero; el tipo de transacción (`deposito`, `retiro`, `compra`, `pago`) se normaliza aceptando la variante `depósito` con tilde; descripción no puede superar 255 caracteres |

*Nota: en `semana_3`, la validación de signo del monto según el tipo de transacción (depósito positivo / retiro-compra negativo) se simplificó a "monto distinto de cero", dado el nivel de corrupción intencional del dataset.*

## Manejo de errores y procesamiento paralelo

### Políticas personalizadas (paquete `policy`)

En vez de la configuración declarativa simple (`.skip(Clase.class).skipLimit(n)`), el proyecto implementa tres políticas propias, reutilizadas por los 3 Jobs:

- **`BancoXyzSkipPolicy`**: decide si un registro se salta según el tipo de excepción, con límites propios y contadores independientes (`AtomicInteger`, seguros entre hilos) para errores de datos (`RegistroInvalidoException`) y de formato de fecha (`FechaInvalidaException`). Cualquier excepción no controlada no se salta — deja que el Job falle.
- **`BancoXyzRetryPolicy`**: reintenta hasta 3 veces cualquier excepción que **no** sea de negocio (pensada para fallas transitorias). Los datos inválidos nunca se reintentan.

*Nota de corrección: se detectó y corrigió un bug de recursión infinita en `BancoXyzRetryPolicy.canRetry()` — la rama que maneja excepciones no contempladas explícitamente llamaba incorrectamente a `canRetry(context)` (a sí misma) en vez de `super.canRetry(context)` (delegando a `SimpleRetryPolicy`), lo que producía un desborde de pila. Ver detalle completo en el Informe Técnico.*
- **`BancoXyzCompletionPolicy`**: cierra cada chunk por cantidad de ítems o por tiempo transcurrido, lo que ocurra primero.

### Listeners (paquete `listener`)

Cada Job tiene 3 listeners dedicados: `JobExecutionListener` (resumen al iniciar/finalizar el Job), `StepExecutionListener` (contadores del Step, persistidos en `execution_log`), y `SkipListener` (detalle de cada ítem saltado, con la etapa y el motivo).

### Trazabilidad: tabla `execution_log`

Spring Batch 6 no persiste sus propias tablas de metadata (`BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`) a menos que se agregue explícitamente la dependencia `spring-boot-starter-batch-jdbc` — decisión que se evaluó y se descartó, ya que introduce un conflicto de `PlatformTransactionManager` con los transaction managers propios del proyecto. En su lugar, cada `StepListener` persiste su propio resumen (Job, Step, estado, leídos/escritos/saltados, fecha) en una tabla propia, `execution_log`, cumpliendo la misma función de auditoría sin depender de la infraestructura interna de Spring Batch.

### Procesamiento paralelo (multithreading)

Cada Step se ejecuta con un `TaskExecutor` (`ThreadPoolTaskExecutor`) configurado con una política de escalamiento fija: **6 hilos** (`core = max = 6`), una cola de espera de 100 tareas para chunks adicionales, y `CallerRunsPolicy` como salvaguarda ante saturación. El `FlatFileItemReader` se envuelve en un `SynchronizedItemStreamReader` para garantizar lectura segura entre hilos.

Se compararon 9 combinaciones de hilos y tamaño de chunk sobre el dataset de `semana_3` (1000 filas por archivo):

| Hilos | Chunk | transaccionJob | cuentaBancariaJob | movimientoAnualJob | Total |
|------:|------:|----------------:|--------------------:|---------------------:|--------:|
| 3 | 50 | 3.381 s | 3.056 s | 2.817 s | **9.254 s** |
| 6 | 200 | 1.989 s | 1.403 s | 1.299 s | **4.691 s** |
| 10 | 200 | 1.780 s | 1.337 s | 1.227 s | **4.344 s** |
| 8 | 200 | 1.551 s | 1.337 s | 1.229 s | **4.117 s** |
| 8 | 300 | 1.371 s | 0.996 s | 0.785 s | **3.152 s** |
| 3 | 250 | 1.393 s | 0.960 s | 0.744 s | **3.097 s** |
| 8 | 550 | 1.282 s | 0.691 s | 0.829 s | **2.802 s** |
| 3 | 500 | 1.211 s | 0.835 s | 0.838 s | **2.884 s** |
| **6** | **500** | **1.234 s** | **0.791 s** | **0.756 s** | **2.781 s** |

El tamaño de chunk resultó ser el parámetro con mayor impacto en el rendimiento, muy por encima del número de hilos: subir el chunk de 50 a 500 (con solo 3 hilos) mejora el tiempo total más de 3 veces (9.254 s → 2.884 s), mientras que subir de 3 a 8 hilos con un chunk fijo apenas aporta mejoras marginales. Esto confirma que el cuello de botella real del proceso es la cantidad de viajes hacia la base de datos remota (Neon), no la capacidad de cómputo en paralelo — un chunk más grande reduce esos viajes, mientras que más hilos solo compiten por los mismos núcleos físicos sin acelerar la escritura.

Subir de 6 a 8-10 hilos con chunk fijo no aporta ganancia adicional consistente, e incluso empeora levemente en algunos casos, ya que el número de núcleos físicos limita el paralelismo real. La mejor combinación encontrada fue **6 hilos / chunk 500** (2.781 s totales), superando incluso a configuraciones con más hilos (8 hilos / chunk 550: 2.802 s). **La configuración final adoptada es 6 hilos / chunk 500.**

*Nota: se evaluó lanzar los 3 Jobs en paralelo entre sí (en vez de en secuencia), pero se descartó — con varios hilos por Job, correr los 3 simultáneamente multiplicaría los hilos activos a la vez, superando ampliamente los núcleos físicos disponibles y probablemente empeorando el rendimiento total en vez de mejorarlo.*

*Nota: el uso de multithreading implica que la funcionalidad de reinicio preciso de Spring Batch queda limitada, ya que varios hilos leen el archivo simultáneamente. Es un comportamiento esperado, no un error.*

## Resumen y auditoría: vistas SQL

Los procesos de transacciones y estados de cuenta anuales requieren, además de persistir el detalle fila por fila, un resumen agregado (transacciones por día, movimientos por cuenta). Esto se implementa como **vistas SQL** (`resumen_transacciones_diarias`, `estados_cuenta_anuales`) en vez de un Step adicional dentro de los Jobs:

- Spring Batch está pensado para procesar datos fila a fila con control de fallos (chunks, skip, retry) — eso ya ocurrió al insertar en `transacciones` y `movimientos_anuales`. Agregar esos datos ya persistidos (`GROUP BY` + `SUM`) es trabajo nativo del motor de base de datos, no del batch.
- Usar `VIEW` (y no una tabla repoblada con `DELETE` + `INSERT`) evita duplicar datos: la vista se recalcula sola en cada consulta, siempre contra el estado actual de las tablas base.
- Esta separación no afecta el rendimiento del batch: los 3 Jobs siguen completando en menos de 1.3 segundos cada uno, ya que las vistas nunca se tocan durante la ejecución, solo al consultarlas.

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

1. Se ejecuta automáticamente `schema-postgresql.sql`, recreando las tablas `transacciones`, `cuentas_bancarias`, `movimientos_anuales`, `execution_log`, y las vistas `resumen_transacciones_diarias`/`estados_cuenta_anuales`.
2. Se disparan los **3 Jobs en secuencia** (uno espera a que el anterior termine antes de arrancar): transacciones → intereses → estados de cuenta anuales.
3. Cada Job procesa su CSV correspondiente en paralelo (6 hilos, chunk de 500), valida cada registro según sus políticas personalizadas, y persiste los válidos en la base de datos.

### 4. Verificar los resultados

Los datos procesados quedan disponibles en las tablas `transacciones`, `cuentas_bancarias` y `movimientos_anuales`. La tabla `execution_log` muestra un resumen de cada ejecución (leídos/escritos/saltados por Job). Las vistas `resumen_transacciones_diarias` y `estados_cuenta_anuales` entregan los reportes agregados. El log de consola muestra, además, el detalle de cada registro saltado vía los `SkipListener`.

## Nota sobre el alcance

Esta entrega cubre el procesamiento de los conjuntos de datos de **semana_1**, **semana_2** y **semana_3**, incorporando progresivamente las técnicas descritas arriba: políticas personalizadas y multithreading (semana_2), y ajustes de validación para el dataset con mayor grado de corrupción, junto con la comparación de configuraciones de escalamiento (semana_3).
