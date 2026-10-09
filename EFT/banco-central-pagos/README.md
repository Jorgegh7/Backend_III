# banco-central-pagos

Microservicio de **transacciones y procesamiento de retiros** de Banco XYZ.
Consume las solicitudes de retiro desde Kafka, descuenta el saldo llamando al servicio de cuentas
y publica el resultado.

- **Puerto:** 8087
- **Registro:** Eureka (`eureka-server:8761`)
- **Configuración:** `config-server` (`config-repo/application.yml`)
- **Base de datos:** PostgreSQL (Neon), base `bancoxyz_eft`, tabla `transacciones`

## Endpoints

Acceso de usuarios (JWT de usuario):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/transacciones` | Lista todas las transacciones (solo rol `EMPLEADO`) |
| GET | `/api/transacciones/credito` | Transacciones de crédito |
| GET | `/api/transacciones/debito` | Transacciones de débito |
| GET | `/api/transacciones/{id}` | Una transacción |
| GET | `/api/transacciones/recientes` | Transacciones recientes (usado por el dashboard) |

Servicio a servicio (token OAuth2):

| Método | Ruta | Scope | Descripción |
|---|---|---|---|
| GET | `/api/oauth2/transacciones/recientes` | `pagos.read` | Transacciones recientes |

Estado del servicio: `/actuator/health` y `/actuator/info` (públicos).

## Procesamiento de retiros (Kafka)

| Rol | Tópico | Evento |
|---|---|---|
| Consumidor | `retiros-solicitados` | `RetiroSolicitadoEvent` (grupo `banco-central-retiros`, 3 consumidores concurrentes) |
| Consumidor | `alertas-seguridad` | `AlertaSeguridadEvent` (grupo `banco-central-pagos-alertas`) |
| Productor | `retiros-aprobados` | `RetiroResultadoEvent` con estado `APROBADO` |
| Productor | `retiros-rechazados` | `RetiroResultadoEvent` con estado `RECHAZADO` y motivo |
| Productor | `transacciones-completadas` | `TransaccionCompletadaEvent`, solo cuando el retiro se aprueba |

Por cada solicitud, el servicio llama a `banco-central-cuentas`
(`POST /api/oauth2/cuentas/{id}/retiro`) con un token OAuth2 obtenido del `auth-server`
(`client_credentials`, scope `cuentas.write`).

## Resiliencia

- **Circuit Breaker** `cuentas` sobre la llamada a Cuentas.
- **Timeouts:** conexión 2 s, lectura 3 s.
- **Sin Retry** en el retiro a propósito: el descuento no es idempotente y un reintento podría descontar dos veces.
- **Fallback:** si Cuentas no está disponible, se publica un rechazo con el motivo
  "El servicio de cuentas no esta disponible. Intenta nuevamente mas tarde".
- Los rechazos de negocio (fondos insuficientes, cuenta inexistente) no cuentan como fallas del servicio,
  por lo que no abren el circuito.

## Seguridad

Dos cadenas de filtros:
1. `/api/oauth2/**`: Resource Server OAuth2, valida el token contra el JWK del `auth-server`.
2. Resto de rutas: JWT de usuario (jjwt), firmado con `jwt.secret` (variable `JWT_SECRET`).

## Estructura

```
banco-central-pagos/
├── src/main/java/com/duoc/banco_central_pagos/
│   ├── controller/   TransaccionController, OAuth2TransaccionController
│   ├── service/      TransaccionService
│   ├── client/       CuentaClient, OAuth2TokenClient
│   ├── kafka/        KafkaRetiroListener, AlertaSeguridadListener
│   ├── config/       RestClientConfig
│   ├── entity/
│   ├── repository/
│   └── dto/
├── src/main/resources/application.properties
└── Dockerfile
```