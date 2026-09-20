# Sistema Bancario XYZ — Microservicios y Seguridad en la Nube con Spring Cloud

## Objetivo

Extender el sistema BFF de Banco XYZ (Backend Central + 3 BFF) con infraestructura de Spring Cloud: configuración centralizada, descubrimiento de servicios, tolerancia a fallos y un segundo mecanismo de autenticación estándar (OAuth2), sin modificar la funcionalidad de negocio ya construida.

## Arquitectura general

```
Infraestructura
  config-server (8888)  →  config-repo/           (configuración)
  eureka-server (8761)  ←  banco-central-xyz       (registro)
  auth-server   (9000)  →  emite JWT (client_credentials)

Clientes (BFF)
  BFF Cajeros (8082)  con Resilience4j (Circuit Breaker + Retry + Rate Limiter)
  BFF Mobile  (8083)
  BFF Web     (8084)
        │
        ▼
Backend Central
  banco-central-xyz (8081)
    - Login usuario → JWT manual
    - Token de auth-server → OAuth2 Resource Server
        │
        ▼
  PostgreSQL (Neon)
```

## Las 8 piezas del proyecto

| # | Servicio | Puerto | Rol |
|---|---|---|---|
| 1 | `config-server` | 8888 | Sirve configuración centralizada (perfil `native`) a los servicios que la consultan |
| 2 | `config-repo` | — | Carpeta local con los archivos de configuración que sirve el Config Server (no es un servicio ejecutable) |
| 3 | `eureka-server` | 8761 | Registro de servicios (Service Discovery) |
| 4 | `auth-server` | 9000 | Spring Authorization Server — emite JWT vía OAuth2 (`client_credentials`) |
| 5 | `banco-central-xyz` | 8081 | Backend Central — datos de cuentas/transacciones/movimientos, login de usuario (JWT manual) y validación OAuth2 |
| 6 | `bff-cajeros` | 8082 | BFF con Resilience4j (Circuit Breaker, Retry, Rate Limiter) |
| 7 | `bff-mobile` | 8083 | BFF sin cambios esta semana (ver README de la Semana 5) |
| 8 | `bff-web` | 8084 | BFF sin cambios esta semana (ver README de la Semana 5) |

## 1–2. Config Server + config-repo

`config-server` expone la configuración de otros servicios sin que cada uno mantenga su propio `application.properties` con datos sensibles repartidos. Usa el perfil `native`, leyendo archivos locales desde una carpeta (`config-repo`) en vez de un repositorio Git remoto.

- `config-repo/banco-central-xyz.yml` contiene todo lo que antes vivía en el `application.properties` local de `banco-central-xyz`: puerto, datos de conexión a PostgreSQL, secreto JWT, URL de Eureka y `jwk-set-uri` del Authorization Server.
- `banco-central-xyz` consulta esta configuración con `spring.config.import=optional:configserver:http://localhost:8888` — el prefijo `optional:` evita que falle si el Config Server no está disponible al arrancar.
- Verificación: `GET http://localhost:8888/banco-central-xyz/default` devuelve el JSON con toda la configuración servida.

## 3. Eureka Server

Actúa como directorio de servicios: cada instancia se registra al arrancar y envía *heartbeats* periódicos para confirmar que sigue activa. `banco-central-xyz` es el servicio registrado, visible en `http://localhost:8761` (aplicación `BANCO-CENTRAL-XYZ`, estado `UP`).

Esta semana el registro es solo demostrativo — los demás servicios siguen usando URLs fijas para comunicarse entre sí, no consultan a Eureka para resolver direcciones.

## 4. auth-server (Authorization Server)

Microservicio sin código propio: toda su lógica es la librería `spring-security-oauth2-authorization-server`, configurada íntegramente por `application.yml`. Implementa el flujo `client_credentials` (autenticación servicio-a-servicio, sin usuario humano):

```yaml
client-id: ms-seguridad-client
client-secret: secret123
authorization-grant-types: client_credentials
scopes: cuentas.read, cuentas.write
```

Para obtener un token: `POST http://localhost:9000/oauth2/token` con Basic Auth (`ms-seguridad-client` / `secret123`) y body `x-www-form-urlencoded` (`grant_type=client_credentials&scope=cuentas.read`).

## 5. banco-central-xyz (actualizado)

Mantiene toda su funcionalidad de negocio de la Semana 5 (endpoints de cuentas, transacciones, movimientos, login de usuario JWT) y suma tres capacidades nuevas:

- **Config Server**: obtiene su configuración desde `config-server` en vez de un archivo local.
- **Eureka Client**: se registra como `BANCO-CENTRAL-XYZ`.
- **Segundo mecanismo de autenticación**: una `SecurityFilterChain` adicional (`@Order(1)`, `securityMatcher("/api/oauth2/**")`) valida tokens emitidos por `auth-server` con `oauth2ResourceServer`, exigiendo el scope `cuentas.read`. Convive sin conflicto con la cadena original (`@Order(2)`) que sigue protegiendo el resto de endpoints con el JWT manual de usuario — `JwtAuthFilter` excluye explícitamente las rutas `/api/oauth2/**` para no interceptar tokens que no sabe interpretar.

Endpoint de prueba: `GET /api/oauth2/cuentas` con `Authorization: Bearer <token de auth-server>`.

## 6. bff-cajeros (Resilience4j)

`CuentaBancariaClient.obtenerSaldo(...)` está protegido con tres patrones de tolerancia a fallos, aplicados en cadena (`@Retry` envuelve a `@CircuitBreaker`, que envuelve a `@RateLimiter`):

| Patrón | Qué resuelve | Configuración clave |
|---|---|---|
| **Retry** | Reintenta fallas transitorias con espera creciente | 3 intentos, backoff exponencial (1s, 2s) |
| **Circuit Breaker** | Deja de intentar tras fallas sostenidas, evitando saturar un servicio caído | Ventana de 10 llamadas, mínimo 5 para evaluar, abre con ≥50% de fallas o llamadas lentas (>2s) |
| **Rate Limiter** | Limita cuántas llamadas puede hacer el BFF en un período, como protección preventiva | 5 llamadas cada 10s |

Si el circuito se abre, el fallback (`obtenerSaldoFallback`) responde con `ServicioNoDisponibleException` (mismo `503` ya usado en la Semana 5), sin intentar la llamada real.

## Instrucciones de ejecución

Orden de arranque recomendado:

```
1. config-server     (8888)
2. eureka-server      (8761)
3. auth-server        (9000)
4. banco-central-xyz  (8081)
5. bff-cajeros        (8082)
6. bff-mobile / bff-web (8083 / 8084, sin cambios esta semana)
```

Variables de entorno necesarias en `banco-central-xyz`: `DB_PASSWORD`, `JWT_SECRET` (ambas se leen desde `config-repo`, pero los valores de entorno deben existir en la máquina donde corre el proceso).

## Limitaciones conocidas

- `config-server` usa el perfil `native` (carpeta local), no un repositorio Git — válido para desarrollo, no reproduce un entorno de configuración distribuida real.
- El registro en Eureka es demostrativo: ningún servicio consulta a Eureka para resolver la dirección de otro; todas las llamadas siguen usando URLs fijas (`localhost:puerto`).
- Resilience4j se aplicó únicamente en `bff-cajeros`, como microservicio representativo del requisito; `bff-mobile` y `bff-web` no lo implementan esta semana.
- Los dos mecanismos de autenticación en `banco-central-xyz` (JWT manual y OAuth2) son independientes entre sí: un usuario logueado con `auth-server` no tiene relación con los usuarios `CLIENTE`/`EMPLEADO` de la base de datos, y viceversa.
