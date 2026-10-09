# banco-central-cuentas

Microservicio de **cuentas bancarias** de Banco XYZ. Entrega la información y el saldo de las cuentas,
y descuenta saldo cuando Pagos procesa un retiro.

- **Puerto:** 8086
- **Registro:** Eureka (`eureka-server:8761`)
- **Configuración:** `config-server` (`config-repo/application.yml`)
- **Base de datos:** PostgreSQL (Neon), base `bancoxyz_eft`, tablas `cuentas_bancarias` y `movimientos_anuales`

## Endpoints

Acceso de usuarios (JWT de usuario):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/cuentas` | Lista todas las cuentas (solo rol `EMPLEADO`) |
| GET | `/api/cuentas/{id}` | Detalle de una cuenta |
| GET | `/api/cuentas/{id}/saldo` | Saldo de una cuenta |
| GET | `/api/cuentas/{id}/mobile` | Vista reducida para mobile (nombre, saldo, tipo) |
| GET | `/api/movimientos` | Movimientos anuales |
| GET | `/api/movimientos/{id}` | Un movimiento anual |
| GET | `/api/movimientos/recientes` | Movimientos recientes |

Un usuario solo puede consultar su propia cuenta; si consulta otra recibe `403`, y si la cuenta no existe, `404`.

Servicio a servicio (token OAuth2):

| Método | Ruta | Scope | Descripción |
|---|---|---|---|
| GET | `/api/oauth2/cuentas/{id}/saldo` | `cuentas.read` | Saldo de una cuenta |
| POST | `/api/oauth2/cuentas/{id}/retiro` | `cuentas.write` | Descuenta saldo; lo invoca Pagos |

Cuerpo del retiro: `{ "monto": 100 }`.
Respuesta: `{ "saldoInicial": 12180.00, "montoRetirado": 100, "saldoFinal": 12080.00 }`.

Estado del servicio: `/actuator/health` y `/actuator/info` (públicos).

## Datos

El saldo vigente de una cuenta es la columna `saldo_final`
(`saldo_final = saldo + interes − retiros`). La columna `saldo` conserva el valor original de la migración.

## Seguridad

Dos cadenas de filtros:
1. `/api/oauth2/**`: Resource Server OAuth2, valida el token contra el JWK del `auth-server` y exige el scope indicado.
2. Resto de rutas: JWT de usuario (jjwt), firmado con `jwt.secret` (variable `JWT_SECRET`).

## Mensajería y resiliencia

No usa Kafka. Recibe llamadas REST de `banco-central-pagos`, que es quien lo protege con
Circuit Breaker y timeouts. Este servicio no invoca a otros.

## Estructura

```
banco-central-cuentas/
├── src/main/java/com/duoc/banco_central_cuentas/
│   ├── controller/   CuentaBancariaController, OAuth2CuentaBancariaController, MovimientoAnualController
│   ├── service/      CuentaBancariaService, MovimientoAnualService
│   ├── entity/       CuentaBancaria, MovimientoAnual
│   ├── repository/
│   ├── dto/
│   └── exception/
├── src/main/resources/application.properties
└── Dockerfile
```