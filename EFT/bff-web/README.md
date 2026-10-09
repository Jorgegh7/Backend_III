# bff-web

Backend for Frontend del canal **web**. Entrega el detalle completo de la cuenta y un dashboard que combina
datos de `banco-central-cuentas` y `banco-central-pagos`.

- **Puerto:** 8084
- **Registro:** Eureka (`eureka-server:8761`)
- **Servicios que consume:** `banco-central-cuentas` (8086) y `banco-central-pagos` (8087)

## Endpoints

| Método | Ruta | Seguridad | Descripción |
|---|---|---|---|
| GET | `/bff-web/cuentas/{id}` | Header `Authorization` con el JWT del usuario | Detalle completo de la cuenta |
| GET | `/bff-web/cuentas/{id}/dashboard` | Header `Authorization` con el JWT del usuario | Cuenta, movimientos y transacciones recientes en una sola respuesta |
| GET | `/bff-web/cuentas/{id}/saldo-oauth2` | Sin JWT de usuario; el BFF usa su propio token OAuth2 | Saldo consultado servicio a servicio |
| GET | `/actuator/health`, `/actuator/info` | Públicos | Estado del servicio |

## Resiliencia

Resilience4j con una instancia por servicio consumido (`bancoCentral` para Cuentas y `pagos` para Pagos):
Circuit Breaker, Retry y Rate Limiter, con fallbacks tipados.
Los errores de negocio (acceso denegado, cuenta no encontrada, token inválido) no se reintentan ni abren el circuito.

## Estructura

```
bff-web/
├── src/main/java/com/duoc/bff_web/
│   ├── controller/   WebController
│   ├── service/      WebService, DashboardService
│   ├── client/       CuentaBancariaClient, TransaccionClient, MovimientoClient, CuentaBancariaOAuth2Client
│   ├── config/       RestClientConfig
│   ├── dto/
│   └── exception/
├── src/main/resources/application.properties
└── Dockerfile
```