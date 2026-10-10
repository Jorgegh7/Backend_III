# Banco XYZ – Migración a Microservicios (EFT Backend III)

Evaluación Final Transversal de **Backend III (PBY2203) – Duoc UC**.
Migración de un sistema bancario monolítico a una arquitectura de microservicios con
Spring Cloud, Spring Batch, patrón BFF, seguridad distribuida y mensajería asíncrona con Apache Kafka.

## 1. Arquitectura

```
 Canales:  Web        Mobile       Cajeros
             │           │            │
         bff-web     bff-mobile   bff-cajeros        (patrón BFF, un backend por canal)
           8084        8083          8082
             └───────────┼────────────┘
                         │  REST + JWT / OAuth2
        ┌────────────────┼────────────────┐
  banco-central-    banco-central-   banco-central-
     clientes          cuentas           pagos         (microservicios de dominio)
       8085             8086             8087
        │                 │                │
        └────── Apache Kafka (EC2, 3 brokers) ──────┘  (eventos asíncronos)

 Infraestructura:  config-server 8888 · eureka-server 8761 · auth-server 9000
 Base de datos:    PostgreSQL (Neon), base bancoxyz_eft
 Batch:            banco-xyz-batch (Spring Batch, ejecución bajo demanda)
```

## 2. Componentes

| Componente | Puerto | Función |
|---|---|---|
| `config-server` | 8888 | Configuración centralizada (`config-repo`, modo native) |
| `eureka-server` | 8761 | Registro y descubrimiento de servicios |
| `auth-server` | 9000 | Servidor de autorización OAuth2 (`client_credentials`) |
| `banco-central-clientes` | 8085 | Clientes, login y alertas de seguridad |
| `banco-central-cuentas` | 8086 | Cuentas y movimientos |
| `banco-central-pagos` | 8087 | Procesamiento de retiros y transacciones |
| `bff-cajeros` | 8082 | BFF canal cajeros |
| `bff-mobile` | 8083 | BFF canal mobile |
| `bff-web` | 8084 | BFF canal web |
| `banco-xyz-batch` | – | 3 procesos Spring Batch (perfil `batch`) |

Cada microservicio tiene su propio `README.md` con detalle de endpoints y configuración.

## 3. Tecnologías

Java 17 (auth-server en Java 21) · Spring Boot 4.x · Spring Cloud · Spring Batch 6 ·
Spring Security (OAuth2 Resource Server, JWT) · Resilience4j · Apache Kafka ·
PostgreSQL (Neon) · Docker / Docker Compose · Spring Boot Actuator.

## 4. Procesos clave de la migración

1. **Batch con Spring Batch:** 3 jobs (transacciones, cuentas bancarias, movimientos anuales)
   con paralelismo (TaskExecutor de 3 hilos), políticas de finalización, reintento y salto de registros,
   y **reejecución automática** ante fallos críticos (3 intentos con espera; código de salida 1 si se agotan).
2. **Microservicios** de dominio: clientes, cuentas y pagos.
3. **Patrón BFF** para 3 canales (web, mobile, cajeros).
4. **Seguridad distribuida:** OAuth2 `client_credentials` con scopes y JWT de usuario.
5. **Mensajería asíncrona con Kafka:** 5 tópicos (`retiros-solicitados`, `retiros-aprobados`,
   `retiros-rechazados`, `transacciones-completadas`, `alertas-seguridad`).

## 5. Flujo de retiro (asíncrono)

1. `POST /bff-cajero/cuentas/{id}/retiros` → el BFF publica en `retiros-solicitados` y responde `PENDIENTE`.
2. **Pagos** consume la solicitud y llama por REST a **Cuentas** (`POST /api/oauth2/cuentas/{id}/retiro`, scope `cuentas.write`)
   para descontar el saldo.
3. Según el resultado, Pagos publica en `retiros-aprobados` o `retiros-rechazados`.
   Si el retiro es aprobado, publica además `transacciones-completadas`.
4. **Clientes** consume `transacciones-completadas` para la notificación.
5. `GET /bff-cajero/cuentas/retiros/{solicitudId}` consulta el estado final.

La llamada de Pagos a Cuentas está protegida con Circuit Breaker y timeouts (conexión 2 s, lectura 3 s).
Si Cuentas no responde, Pagos publica un rechazo controlado en lugar de fallar.
Tras 3 intentos fallidos de login, **Clientes** publica una alerta en `alertas-seguridad`.

## 6. Resiliencia

Resilience4j (Circuit Breaker, Retry y Rate Limiter) con fallbacks tipados.
No se aplica Retry al retiro (operación no idempotente).

## 7. Datos

El saldo vigente de una cuenta está en la columna `saldo_final`
(`saldo_final = saldo + interes − retiros`). La columna `saldo` conserva el valor original de la migración.
Los `id` de cuentas **cambian cada vez que se ejecuta el batch**; consultarlos antes de probar.

## 8. Cómo ejecutar

Ver **[instrucciones.md](instrucciones.md)** (puesta en marcha y pruebas) y **[despliegue.md](despliegue.md)**
(Docker, Kafka en EC2 y variables de entorno).

## 9. Decisiones de alcance

- Sin API Gateway ni balanceador de carga.
- Sin escalado con `--scale` en la demostración; la escalabilidad horizontal se argumenta en el informe
  (servicios sin estado, registro en Eureka y consumidores Kafka por grupo con 3 particiones).
- Sin healthchecks de Docker en los servicios; el estado se verifica con Actuator.
- Sin HTTPS: la seguridad se resuelve con OAuth2/JWT y los servicios se comunican dentro de la red Docker `banco-net`.
- Health de `auth-server` protegido tras autenticación.

## 10. Estructura del repositorio

```
EFT/
├── auth-server/
├── config-server/
├── config-repo/
├── eureka-server/
├── banco-central-clientes/
├── banco-central-cuentas/
├── banco-central-pagos/
├── bff-cajeros/
├── bff-mobile/
├── bff-web/
├── banco-xyz-batch/
├── docker-compose.yml
├── readme.md
├── instrucciones.md
└── despliegue.md
```

## 11. Seguridad de credenciales

Las contraseñas y secretos van en un archivo `.env` local que **no se versiona**
(`DB_PASSWORD`, `JWT_SECRET`, `OAUTH_CLIENT_SECRET`). Los valores del repositorio son solo de desarrollo.