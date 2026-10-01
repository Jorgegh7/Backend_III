# bff-cajeros

Backend For Frontend del canal Cajero Automático. Expone el mínimo detalle posible (solo saldo, sin nombre del titular), por tratarse de un canal físico público con riesgo de exposición visual a terceros. Es el microservicio con mayor cantidad de patrones de resiliencia implementados: Circuit Breaker, Retry, Rate Limiter, y el flujo completo de mensajería asíncrona con Kafka.

## Puerto
`8082`

## Tecnologías clave
- Spring Boot 4.1.1, Spring Cloud (Eureka Client)
- Resilience4j (Circuit Breaker + Retry + Rate Limiter)
- Spring Kafka (productor de `retiros-solicitados`, consumidor de `retiros-aprobados`/`retiros-rechazados`)
- Cliente OAuth2 propio (`client_credentials`) para llamadas de servicio a `banco-central-xyz`

## Tolerancia a fallos (Resilience4j)
`CuentaBancariaClient.obtenerSaldo(...)` está protegido con `@Retry` (3 intentos, backoff exponencial) y `@CircuitBreaker` (abre con ≥50% de fallas en ventana de 10 llamadas, mínimo 5 para evaluar). Un `RateLimiter` separado limita las llamadas a 5 cada 10 segundos. `ResilienceEventLogger` deja evidencia explícita de cada transición de estado en consola.

## Mensajería asíncrona (Kafka)
`POST /bff-cajero/cuentas/{id}/retiros` publica `RetiroSolicitadoEvent` y responde `202 Accepted` de inmediato, sin esperar el procesamiento. El resultado se consulta después con `GET /bff-cajero/cuentas/retiros/{solicitudId}`, que lee de un almacenamiento en memoria (`RetiroEstadoStore`) poblado por el consumidor de resultados.

## Endpoints principales
```
GET  /bff-cajero/cuentas/{id}/saldo              (JWT manual)
GET  /bff-cajero/cuentas/{id}/saldo-oauth2        (OAuth2 client_credentials, sin pasar token manualmente)
POST /bff-cajero/cuentas/{id}/retiros             (asíncrono, Kafka)
GET  /bff-cajero/cuentas/retiros/{solicitudId}    (consulta de estado)
```

## Dependencias con otros servicios
`config-server` (configuración local, no centralizada), `eureka-server`, `banco-central-xyz`, `auth-server`, Kafka (EC2).
