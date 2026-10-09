# bff-cajeros

Backend for Frontend del canal **cajeros automáticos**. Consulta saldos en `banco-central-cuentas` y gestiona
los retiros de forma asíncrona mediante Kafka.

- **Puerto:** 8082
- **Registro:** Eureka (`eureka-server:8761`)
- **Servicio que consume:** `banco-central-cuentas` (puerto 8086)

## Endpoints

| Método | Ruta | Seguridad | Descripción |
|---|---|---|---|
| GET | `/bff-cajero/cuentas/{id}/saldo` | Header `Authorization` con el JWT del usuario | Saldo de la cuenta |
| GET | `/bff-cajero/cuentas/{id}/saldo-oauth2` | Sin JWT de usuario; el BFF usa su propio token OAuth2 | Saldo consultado servicio a servicio |
| POST | `/bff-cajero/cuentas/{id}/retiros` | Token | Solicita un retiro; responde `202 Accepted` con estado `PENDIENTE` y un `solicitudId` |
| GET | `/bff-cajero/cuentas/retiros/{solicitudId}` | Token | Consulta el resultado del retiro |
| GET | `/actuator/health`, `/actuator/info` | Públicos | Estado del servicio |

Cuerpo del retiro: `{ "monto": 100 }`.

Respuesta de la consulta de estado, una vez procesado el retiro:

```json
{
  "solicitudId": "…",
  "cuentaId": 335,
  "estado": "APROBADO",
  "monto": 100,
  "saldoInicial": 12180.00,
  "saldoFinal": 12080.00,
  "motivo": "Retiro procesado correctamente",
  "fechaProcesamiento": "…"
}
```

## Mensajería (Kafka)

| Rol | Tópico | Evento |
|---|---|---|
| Productor | `retiros-solicitados` | `RetiroSolicitadoEvent`, al recibir un retiro |
| Consumidor | `retiros-aprobados` y `retiros-rechazados` | `RetiroResultadoEvent`, con el resultado del retiro |

Los resultados recibidos se guardan en memoria (`RetiroEstadoStore`) y se consultan por `solicitudId`.
El estado se pierde si el servicio se reinicia (limitación documentada en el informe).

## Resiliencia

Resilience4j sobre el cliente hacia Cuentas (instancia `bancoCentral`): Circuit Breaker con fallback,
Retry y Rate Limiter. La solicitud de retiro no se reintenta porque se publica una sola vez en Kafka.

## Estructura

```
bff-cajeros/
├── src/main/java/com/duoc/bff_cajeros/
│   ├── controller/   CajeroController
│   ├── service/      CajeroService, RetiroEstadoStore
│   ├── client/       CuentaBancariaClient, CuentaBancariaOAuth2Client
│   ├── kafka/        KafkaResultadoListener
│   ├── dto/
│   └── exception/
├── src/main/resources/application.properties
└── Dockerfile
```