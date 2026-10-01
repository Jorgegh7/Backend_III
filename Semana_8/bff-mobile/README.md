# bff-mobile

Backend For Frontend del canal Móvil. Expone un nivel de detalle intermedio (titular, saldo, tipo de cuenta) — más que Cajeros, menos que Web — acorde a un dispositivo personal usado en espacios semi-públicos. Consulta un endpoint liviano del Backend Central (`/api/cuentas/{id}/mobile`) para evitar transferir el objeto completo de la cuenta.

## Puerto
`8083`

## Tecnologías clave
- Spring Boot 4.1.1, Spring Cloud (Eureka Client)
- Resilience4j (Circuit Breaker + Retry + Rate Limiter)
- Cliente OAuth2 propio (`client_credentials`) para llamadas de servicio a `banco-central-xyz`

## Tolerancia a fallos (Resilience4j)
Mismo patrón que `bff-cajeros`: `CuentaBancariaClient.obtenerCuentaResumen(...)` protegido con `@Retry` + `@CircuitBreaker`, `@RateLimiter` en un método separado, y `ResilienceEventLogger` registrando transiciones de estado.

## Endpoints principales
```
GET /bff-mobile/cuentas/{id}/saldo           (JWT manual — nombre, saldo, tipo de cuenta)
GET /bff-mobile/cuentas/{id}/saldo-oauth2    (OAuth2 client_credentials, sin pasar token manualmente)
```

## Dependencias con otros servicios
`config-server` (configuración local, no centralizada), `eureka-server`, `banco-central-xyz`, `auth-server`.
