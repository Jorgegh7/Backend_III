# bff-mobile

Backend for Frontend del canal **mobile**. Entrega respuestas reducidas para la app móvil y delega los datos
en `banco-central-cuentas`.

- **Puerto:** 8083
- **Registro:** Eureka (`eureka-server:8761`)
- **Servicio que consume:** `banco-central-cuentas` (puerto 8086)

## Endpoints

| Método | Ruta | Seguridad | Descripción |
|---|---|---|---|
| GET | `/bff-mobile/cuentas/{id}/saldo` | Header `Authorization` con el JWT del usuario | Saldo y datos mínimos de la cuenta, en formato liviano |
| GET | `/bff-mobile/cuentas/{id}/saldo-oauth2` | Sin JWT de usuario; el BFF usa su propio token OAuth2 | Saldo consultado servicio a servicio |
| GET | `/actuator/health`, `/actuator/info` | Públicos | Estado del servicio |

El BFF reenvía el JWT del usuario a Cuentas, que valida que la cuenta pertenezca a quien consulta
(`403` si es de otro usuario, `404` si no existe).

## Resiliencia

Resilience4j sobre el cliente hacia Cuentas: Circuit Breaker, Retry y Rate Limiter, con fallbacks tipados.
Los errores de negocio (acceso denegado, cuenta no encontrada, token inválido) no se reintentan ni abren el circuito.

## Estructura

```
bff-mobile/
├── src/main/java/com/duoc/bff_mobile/
│   ├── controller/   MobileController
│   ├── service/      MobileService
│   ├── client/       CuentaBancariaClient, CuentaBancariaOAuth2Client
│   ├── dto/
│   └── exception/
├── src/main/resources/application.properties
└── Dockerfile
```