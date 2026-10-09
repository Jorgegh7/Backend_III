# auth-server

Servidor de autorización OAuth2 del sistema. Emite los tokens que los servicios y los BFF usan para
comunicarse entre sí (flujo `client_credentials`).

- **Puerto:** 9000
- **Tecnología:** Spring Authorization Server (Java 21)

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/oauth2/token` | Emite un access token (Basic Auth del cliente + `grant_type=client_credentials`) |
| GET | `/oauth2/jwks` | Claves públicas para validar los tokens; los servicios las usan como `jwk-set-uri` |

## Cliente registrado

| Cliente | Scopes |
|---|---|
| `ms-seguridad-client` | `cuentas.read`, `cuentas.write`, `clientes.read`, `pagos.read` |

Las credenciales del cliente son de desarrollo y están en la configuración del servicio.
El token tiene una vigencia de 5 minutos.

## Notas

El endpoint `/actuator/health` queda protegido por la autenticación del propio servidor (decisión documentada en el informe);
el estado del auth-server se verifica obteniendo un token.