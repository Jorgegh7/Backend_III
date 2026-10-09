# banco-central-clientes

Microservicio de **clientes y autenticación de usuarios** de Banco XYZ.
Valida credenciales, emite el JWT de usuario, detecta intentos de acceso sospechosos y
notifica transacciones completadas.

- **Puerto:** 8085
- **Registro:** Eureka (`eureka-server:8761`)
- **Configuración:** `config-server` (`config-repo/application.yml`)
- **Base de datos:** PostgreSQL (Neon), base `bancoxyz_eft`, tabla `usuarios`

## Endpoints

| Método | Ruta | Seguridad | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | Pública | Autentica un usuario y devuelve un JWT |
| GET | `/api/oauth2/clientes/{username}` | Token OAuth2, scope `clientes.read` | Consulta de un cliente por su nombre de usuario |
| GET | `/actuator/health`, `/actuator/info` | Pública | Estado del servicio |

### POST /api/auth/login

Cuerpo:

```json
{ "username": "steve", "password": "1234", "canal": "WEB" }
```

Respuesta correcta:

```json
{ "token": "<JWT>" }
```

El JWT incluye el nombre de usuario, su rol y la cuenta asociada (`cuentaIdLegacy`).
`canal` es un texto libre que identifica desde dónde se intenta el acceso (por ejemplo `WEB`, `MOBILE`
o `CAJERO`); se usa en la alerta de seguridad. Con credenciales incorrectas el servicio responde con error
de autenticación.

## Seguridad

Dos cadenas de filtros en `SecurityConfig`:
1. `/api/oauth2/**`: Resource Server OAuth2, valida el token contra el JWK del `auth-server`.
2. Resto de rutas: JWT de usuario (jjwt), firmado con `jwt.secret`.

El secreto se entrega por la variable de entorno `JWT_SECRET`; no está en el repositorio.
Las contraseñas se almacenan como hash (`PasswordEncoder`).

## Mensajería (Kafka)

| Rol | Tópico | Evento | Cuándo |
|---|---|---|---|
| Productor | `alertas-seguridad` | `AlertaSeguridadEvent` | Tras **3 intentos fallidos consecutivos** de login de un mismo usuario |
| Consumidor | `transacciones-completadas` | `TransaccionCompletadaEvent` | Registra la notificación de una transacción aprobada |

`AlertaSeguridadEvent` contiene: usuario, canal, número de intentos, motivo y fecha.
El contador de intentos fallidos se reinicia con un login correcto o al publicar la alerta.
Se mantiene **en memoria**: se pierde al reiniciar el servicio (limitación documentada en el informe).

## Estructura

```
banco-central-clientes/
├── src/main/java/com/duoc/banco_central_clientes/
│   ├── controller/   AuthController, OAuth2ClienteController
│   ├── service/      AuthService
│   ├── security/     JwtService
│   ├── entity/       Usuario
│   ├── repository/   UsuarioRepository
│   ├── dto/          LoginRequestDto, LoginResponseDto
│   └── kafka/        NotificacionTransaccionListener
├── src/main/resources/application.properties
└── Dockerfile
```

## Resiliencia

Este servicio no invoca a otros microservicios, por lo que no usa Circuit Breaker.
La resiliencia del flujo de retiros está en `banco-central-pagos` y en los BFF.