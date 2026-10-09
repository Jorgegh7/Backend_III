# banco-xyz-batch

Proceso batch con **Spring Batch 6** que migra los datos del sistema legacy de Banco XYZ a la base
PostgreSQL (Neon, base `bancoxyz_eft`). Se ejecuta bajo demanda, no queda corriendo como servicio.

## Jobs

Se ejecutan en secuencia al iniciar la aplicación:

| Job | Archivo de entrada | Tabla de destino |
|---|---|---|
| `transaccionJob` | `data/semana_3/movimientos_financieros_diarios.csv` | `transacciones` |
| `cuentaBancariaJob` | `data/semana_3/intereses_trimestrales.csv` | `cuentas_bancarias` |
| `movimientoAnualJob` | `data/semana_3/estados_financieros_anuales.csv` | `movimientos_anuales` |

Cada job tiene un solo step con patrón reader → processor → writer (lectura de CSV, validación y transformación,
escritura con `JdbcBatchItemWriter`). Los CSV originales de las semanas anteriores también están en `resources/data/`.

## Manejo de errores

- **Política de finalización** (`BancoXyzCompletionPolicy`): cada chunk se cierra al llegar a 500 registros
  o a los 2 segundos, lo que ocurra primero.
- **Política de reintento** (`BancoXyzRetryPolicy`) para errores transitorios.
- **Política de salto** (`BancoXyzSkipPolicy`) para registros inválidos, que se omiten y se registran.
- **Excepciones propias:** `RegistroInvalidoException` y `FechaInvalidaException`.
- **Listeners** de job, step y salto: registran el estado final y los conteos de leídos, escritos y saltados.
  Cada step guarda su resultado en la tabla `execution_log` como evidencia de la ejecución.

## Paralelismo

Los steps procesan con un `TaskExecutor` de 3 hilos. El reader se envuelve en `SynchronizedItemStreamReader`
para que solo un hilo lea a la vez; el procesamiento y la escritura se hacen en paralelo.
Por eso el reader no guarda estado de reinicio (`saveState(false)`).

## Reejecución automática ante fallos críticos

Si un job termina en un estado distinto de `COMPLETED`, se vuelve a lanzar hasta 3 veces con una espera entre intentos.
Como los jobs usan `RunIdIncrementer`, cada intento crea una instancia nueva y reprocesa desde el inicio.
Si se agotan los intentos, el proceso termina con código de salida 1 y no ejecuta los jobs siguientes.

| Propiedad | Valor por defecto |
|---|---|
| `batch.reejecucion.max-intentos` | 3 |
| `batch.reejecucion.espera-ms` | 5000 |
| `batch.transacciones.archivo` | `classpath:data/semana_3/movimientos_financieros_diarios.csv` |

La última propiedad permite cambiar el archivo de entrada del job de transacciones.

## Metadatos y evidencia

Spring Batch guarda sus tablas de metadatos en la misma base (`spring.batch.jdbc.initialize-schema=always`).
Las tablas de negocio se crean con `spring.sql.init.mode=always`.

## Ejecución

Perfil de Docker Compose `batch`. Ver `instrucciones.md`.

## Estructura

```
banco-xyz-batch/
├── src/main/java/cl/duoc/bancoxyz/
│   ├── config/       CuentaBancariaBatchConfig, MovimientoAnualBatchConfig,
│   │                 TransaccionBatchConfig, TaskExecutorConfig, JobLauncherConfig
│   ├── dtos/
│   ├── entities/
│   ├── processor/
│   ├── listener/
│   ├── policies/