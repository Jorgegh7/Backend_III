# Sistema Bancario XYZ — Arquitectura Orientada a Eventos con Apache Kafka

## Objetivo

Extender el sistema de la Semana 6 (Backend Central + 3 BFF, con Config Server, Eureka, OAuth2 y Resilience4j) incorporando una arquitectura orientada a eventos para el procesamiento de retiros bancarios: el BFF Cajeros publica la solicitud de forma asíncrona, el Backend Central la procesa y publica el resultado, y el BFF Cajeros permite consultarlo posteriormente.

## Correcciones aplicadas de la Semana 6

Antes de incorporar Kafka, se resolvió el feedback recibido:

- **Eureka**: `bff-web` y `bff-mobile` ahora se registran también en Eureka (antes solo `banco-central-xyz` y `bff-cajeros` lo hacían).
- **Resilience4j**: extendido a `bff-mobile` (Circuit Breaker + Retry + Rate Limiter, mismo patrón que `bff-cajeros`). `bff-web` queda pendiente para una iteración futura por prioridad de tiempo frente a la entrega de Kafka.

## Arquitectura de eventos

Se adopta **arquitectura orientada a eventos (Event-Driven Architecture)** con patrón **Publish/Subscribe**, usando Apache Kafka como broker. No se implementa Saga ni Event Sourcing.

El BFF Cajeros no espera a que el Backend Central termine de procesar un retiro: publica la solicitud, responde de inmediato con estado `PENDIENTE`, y el cliente consulta el resultado posteriormente con un identificador único (`solicitudId`).

```
Infraestructura Kafka (EC2, IP elástica)
  3x Zookeeper (quorum, número impar)
  3x Kafka broker (replication-factor=3)
  Kafka UI — panel de administración

Flujo de retiro
  1. Cliente → POST /retiros → BFF Cajeros
  2. BFF Cajeros → publica RetiroSolicitadoEvent → tópico retiros-solicitados (key=cuentaId)
  3. Banco Central consume (grupo banco-central-retiros, concurrency=3)
  4. Banco Central procesa el retiro (valida cuenta ahorro + fondos, actualiza saldo)
  5. Banco Central → publica RetiroResultadoEvent → retiros-aprobados o retiros-rechazados
  6. BFF Cajeros consume el resultado (grupo bff-cajeros-resultados) y lo guarda en memoria
  7. Cliente → GET /retiros/{solicitudId} → BFF Cajeros devuelve el resultado
```

## Tópicos Kafka

| Tópico | Productor | Consumidor | Particiones | Replication factor |
|---|---|---|---|---|
| `retiros-solicitados` | BFF Cajeros | Banco Central | 3 | 3 |
| `retiros-aprobados` | Banco Central | BFF Cajeros | 3 | 3 |
| `retiros-rechazados` | Banco Central | BFF Cajeros | 3 | 3 |

El `cuentaId` se usa como key del mensaje, para que los eventos de una misma cuenta queden en la misma partición.

## Eventos

`RetiroSolicitadoEvent`: `solicitudId`, `cuentaId`, `monto`, `fechaSolicitud`.

`RetiroResultadoEvent`: `solicitudId`, `cuentaId`, `estado` (`APROBADO`/`RECHAZADO`), `monto`, `saldoInicial`, `saldoFinal`, `motivo`, `fechaProcesamiento`.

Ambas clases (`record` de Java) están duplicadas — con idéntico contenido — bajo el paquete `com.duoc.eventos` en `bff-cajeros` y `banco-central-xyz`, ya que no existe un módulo compartido entre ambos proyectos. La configuración `spring.kafka.consumer.properties.spring.json.trusted.packages=com.duoc.eventos` exige que ambos coincidan exactamente en paquete y estructura para que la deserialización JSON funcione.

## Infraestructura Kafka (Docker, EC2)

El clúster corre en una instancia EC2 con IP elástica, mediante `docker-compose.yml`: 3 nodos Zookeeper (coordinación por quorum, número impar de nodos para evitar empates) y 3 brokers Kafka con `replication-factor=3` — cada partición tiene un broker líder y dos réplicas sincronizadas en los otros dos, de forma que la caída de un broker no implica pérdida de datos.

Cada broker declara dos listeners: uno interno (`kafka-N:290NN`, para comunicación entre contenedores) y uno externo (`<IP_ELASTICA>:290NN/390NN/490NN`, para que los microservicios —que corren en la máquina local del desarrollador— se conecten remotamente). Kafka UI (`<IP_ELASTICA>:8090`) permite inspeccionar tópicos, particiones, mensajes y la distribución de líderes/réplicas entre los 3 brokers.

## Tolerancia a fallos (Resilience4j)

Se mantiene el mismo patrón de la Semana 6, aplicado a las llamadas **síncronas** que persisten (ej. `GET /saldo`), no a las asíncronas vía Kafka — el desacople de Kafka ya es en sí mismo un mecanismo de tolerancia a fallos para el flujo de retiro.

| Patrón | Configuración |
|---|---|
| **Retry** | 3 intentos, backoff exponencial (1s, 2s) |
| **Circuit Breaker** | Ventana de 10 llamadas, mínimo 5 para evaluar, abre con ≥50% de fallas o llamadas lentas (>2s) |
| **Rate Limiter** | 5 llamadas cada 10s |

Implementado en `bff-cajeros` y `bff-mobile`, con `ResilienceEventLogger` suscrito a los eventos internos (cambios de estado del circuito, reintentos) para dejar evidencia explícita en consola.

## Almacenamiento del estado de solicitudes (limitación conocida)

`bff-cajeros` guarda el resultado de cada retiro en un `ConcurrentHashMap` en memoria (`RetiroEstadoStore`), indexado por `solicitudId`. Es intencionalmente simple para el alcance de esta entrega: si el proceso se reinicia, el historial de solicitudes se pierde. En una solución productiva correspondería persistir este estado (tabla en base de datos o caché como Redis).

## Garantías de entrega

La comunicación vía Kafka desacopla al BFF del procesamiento bancario, pero no implementa una transacción *exactly-once* extremo a extremo entre Kafka y PostgreSQL. Ante una falla ubicada exactamente entre la confirmación en base de datos y el commit del offset de Kafka, un mensaje podría reprocesarse. Una solución productiva implementaría idempotencia persistente usando `solicitudId`, o el patrón Transactional Outbox.

## Instrucciones de ejecución

Orden de arranque:

```
1. Infraestructura Docker en EC2 (zookeeper x3, kafka x3, kafka-ui) — ya debe estar corriendo
2. config-server     (8888)
3. eureka-server      (8761)
4. auth-server        (9000)
5. banco-central-xyz  (8081)
6. bff-cajeros        (8082)
7. bff-mobile / bff-web (8083 / 8084)
```

Variables de entorno en `banco-central-xyz`: `DB_PASSWORD`, `JWT_SECRET`.

## Prueba end-to-end

```
POST http://localhost:8082/bff-cajero/cuentas/{id}/retiros
Body: { "monto": 100.00 }
→ 202 Accepted, { "solicitudId": "...", "estado": "PENDIENTE" }

GET http://localhost:8082/bff-cajero/cuentas/retiros/{solicitudId}
→ { "estado": "APROBADO", "saldoInicial": ..., "saldoFinal": ..., "motivo": "Retiro procesado correctamente" }
```

## Limitaciones conocidas

- El registro en Eureka sigue siendo demostrativo: ningún servicio consulta a Eureka para resolver direcciones entre sí; todas las llamadas usan URLs fijas.
- `bff-web` aún no tiene Resilience4j implementado.
- El estado de las solicitudes de retiro vive en memoria en `bff-cajeros`, sin persistencia.
- No se implementa garantía *exactly-once* entre el consumo de Kafka y la actualización de saldo en PostgreSQL (ver sección "Garantías de entrega").
