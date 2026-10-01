# banco-central-xyz

Backend Central del sistema bancario. Única fuente de verdad: expone los datos de `cuentas_bancarias`, `transacciones` y `movimientos_anuales` desde PostgreSQL (Neon), sin adaptarlos a ningún canal específico. Gestiona dos mecanismos de autenticación independientes, consume configuración centralizada, se registra en el directorio de servicios, y consume/produce eventos Kafka para el flujo de retiros.

## Puerto
`8081`

## Tecnologías clave
- Spring Boot 4.1.1, Spring Data JPA, Spring Security
- Spring Cloud Config Client, Eureka Client
- OAuth2 Resource Server (validación de JWT emitidos por `auth-server`)
- JWT manual (login de usuario, con `jjwt`)
- Spring Kafka (consumidor de `retiros-solicitados`, productor de `retiros-aprobados`/`retiros-rechazados`)

## Dos mecanismos de autenticación conviviendo
- **JWT manual** (`@Order(2)`): login de usuario (`POST /api/auth/login`), usado por clientes finales a través de los BFF.
- **OAuth2 Resource Server** (`@Order(1)`, rutas `/api/oauth2/**`): valida tokens `client_credentials` emitidos por `auth-server`, usado por los BFF cuando actúan como clientes de servicio (`GET /api/oauth2/cuentas/{id}/saldo`), exigiendo el scope `cuentas.read`.

## Configuración centralizada
Toda la configuración (puerto, credenciales de base de datos, secreto JWT, URL de Eureka, `jwk-set-uri` de `auth-server`) se obtiene de `config-server` al arrancar (`spring.config.import=optional:configserver:http://config-server:8888`), no de un `application.properties` local.

## Variables de entorno
`DB_PASSWORD`, `JWT_SECRET`

## Dependencias con otros servicios
- `config-server` — configuración (obligatoria para arrancar correctamente)
- `eureka-server` — registro de servicio
- `auth-server` — validación de JWT (`jwk-set-uri`)
- Kafka (EC2, IP pública) — consumidor/productor de eventos de retiro

## Verificación
```
GET http://localhost:8888/banco-central-xyz/default   (config servida)
http://localhost:8761                                  (registrado como BANCO-CENTRAL-XYZ)
GET /api/cuentas/{id}/saldo con JWT manual
GET /api/oauth2/cuentas/{id}/saldo con token OAuth2
```
