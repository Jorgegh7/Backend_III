# bff-web

Backend For Frontend del canal Web. Expone el mayor nivel de detalle de los 3 BFF (todos los campos de la cuenta), acorde a una sesión de escritorio con mayor privacidad y control. Es el único BFF con un endpoint de **dashboard**, que agrega en una sola respuesta datos de tres fuentes distintas del Backend Central (cuenta, transacciones, movimientos), consultadas en paralelo con `CompletableFuture`.

## Puerto
`8084`

## Tecnologías clave
- Spring Boot 4.1.1, Spring Cloud (Eureka Client)
- Resilience4j (Circuit Breaker + Retry + Rate Limiter)
- Cliente OAuth2 propio (`client_credentials`) para llamadas de servicio a `banco-central-xyz`
- Tres `Client` separados (`CuentaBancariaClient`, `TransaccionClient`, `MovimientoClient`), uno por recurso consumido

## Tolerancia a fallos (Resilience4j)
Aplicado únicamente sobre `CuentaBancariaClient` (el principal), siguiendo el mismo patrón que `bff-cajeros` y `bff-mobile`: `@Retry` + `@CircuitBreaker` + `@RateLimiter` separado, con `ResilienceEventLogger` registrando transiciones de estado en consola.

## Endpoints principales
```
GET /bff-web/cuentas/{id}                   (JWT manual — detalle completo)
GET /bff-web/cuentas/{id}/dashboard          (agregación: cuenta + 5 transacciones + 5 movimientos recientes, en paralelo)
GET /bff-web/cuentas/{id}/saldo-oauth2       (OAuth2 client_credentials, sin pasar token manualmente)
```

## Dependencias con otros servicios
`config-server` (configuración local, no centralizada), `eureka-server`, `banco-central-xyz`, `auth-server`.
