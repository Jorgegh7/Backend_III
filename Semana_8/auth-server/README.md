# auth-server

Servidor de autorización OAuth2 (Spring Authorization Server). No contiene código Java propio — toda su lógica está resuelta por configuración declarativa. Implementa el flujo `client_credentials`, pensado para autenticación servicio-a-servicio (sin usuario humano), emitiendo JWT a quien se autentique con credenciales de cliente válidas.

## Puerto
`9000`

## Tecnologías clave
- Spring Boot 3.5.10 + Spring Security OAuth2 Authorization Server
- Java 21 (única diferencia de versión respecto al resto de los microservicios, que usan Java 17)

## Configuración relevante
```yaml
client-id: ms-seguridad-client
client-secret: secret123 (sin hash, solo para entorno de desarrollo)
authorization-grant-types: client_credentials
scopes: cuentas.read, cuentas.write
```

## Dependencias con otros servicios
Ninguna — es independiente. `banco-central-xyz` lo consulta para validar tokens (vía JWK); los 3 BFF lo consultan para obtener tokens antes de llamar a `banco-central-xyz`.

## Verificación
```
POST http://localhost:9000/oauth2/token
Authorization: Basic (ms-seguridad-client / secret123)
Body (x-www-form-urlencoded): grant_type=client_credentials, scope=cuentas.read
```
Devuelve un `access_token` JWT válido por 299 segundos.
