# Sistema Bancario con Patrón BFF (Backend For Frontend)

## Objetivo

Implementar el patrón arquitectónico **Backend For Frontend (BFF)** sobre los datos del sistema legacy de Banco XYZ: un **Backend Central** que expone los datos crudos desde la base de datos, y **3 BFF** (Web, Móvil y Cajero Automático) que consumen ese Backend Central y adaptan la información según las necesidades específicas de cada canal.

## Arquitectura

```
Cliente Web    → BFF Web (8084)     ──┐
Cliente Mobile → BFF Mobile (8083)  ──┼──→ Backend Central (8081) → PostgreSQL (Neon)
Cliente Cajero → BFF Cajeros (8082) ──┘
```

**Backend Central**: única fuente de verdad. Expone los datos de `cuentas_bancarias`, `transacciones` y `movimientos_anuales` tal como están en la base de datos, sin adaptarlos a ningún canal específico. Maneja autenticación (JWT), autorización por rol (`CLIENTE`/`EMPLEADO`) y autorización por propiedad de cuenta.

**BFF**: cada uno consume los mismos endpoints del Backend Central, pero decide cuánto detalle exponer según el contexto de su canal:

| Canal | Nivel de detalle | Justificación |
|---|---|---|
| **Cajero** | Mínimo (solo saldo) | Espacio físico público, riesgo de exposición visual a terceros |
| **Mobile** | Intermedio (titular, saldo, tipo de cuenta) | Dispositivo personal, pero usado en espacios semi-públicos |
| **Web** | Completo (todos los campos) | Sesión en un entorno más controlado y privado |

Este diseño responde a la estrategia definida para el proyecto: un Backend Central neutral evita duplicar lógica de negocio en cada BFF, y cada BFF se limita a filtrar/transformar la respuesta según su canal — sin modificar datos (solo operaciones `GET` en esta entrega).

## Estructura del código

Cada servicio es un proyecto Spring Boot independiente, con la misma organización interna:

```
config/       → configuración de seguridad (Backend Central) o RestClient (BFF)
client/       → llamadas HTTP puras hacia el Backend Central (solo en los BFF)
controller/   → endpoints expuestos
service/      → lógica de negocio y transformación de datos
dto/          → objetos de transferencia (en los BFF, separados en dto/ y dto/central/)
exception/    → excepciones propias y su manejo centralizado
entity/, repository/ → solo en el Backend Central (acceso a base de datos)
```

## Manejo de excepciones

Cada servicio implementa un `GlobalExceptionHandler` (`@RestControllerAdvice`) que traduce errores técnicos a respuestas JSON consistentes, sin exponer detalles internos del sistema:

| Código | Causa | Mensaje |
|---|---|---|
| `404` | Recurso no encontrado | `"La cuenta solicitada no existe"` |
| `401` | Token inválido, expirado o ausente | `"Debes iniciar sesión para acceder a este recurso"` |
| `403` | Rol insuficiente, o cuenta que no pertenece al usuario | `"No tienes permisos para acceder a esta cuenta"` |
| `503` | Backend Central no disponible | `"El servicio no está disponible en este momento"` |

En los BFF, `CuentaBancariaClient` captura las excepciones HTTP (`HttpClientErrorException.NotFound/Unauthorized/Forbidden`, `ResourceAccessException`) y las traduce a excepciones propias del BFF — el `Controller` nunca queda expuesto a errores de red o HTTP crudos.

## Puntos de conexión (puertos)

| Servicio | Puerto | URL base |
|---|---|---|
| Backend Central | `8081` | `http://localhost:8081/api` |
| BFF Cajeros | `8082` | `http://localhost:8082/bff-cajero` |
| BFF Mobile | `8083` | `http://localhost:8083/bff-mobile` |
| BFF Web | `8084` | `http://localhost:8084/bff-web` |

Los 3 BFF apuntan al Backend Central mediante la propiedad `banco.central.url=http://localhost:8081`, configurada en cada `application.properties`.

## Cómo ejecutar

1. Configurar la variable de entorno `DB_PASSWORD` (contraseña de Neon) y `JWT_SECRET` en el Backend Central.
2. Levantar los 4 servicios, en cualquier orden, cada uno en su puerto correspondiente.
3. Autenticarse contra `POST http://localhost:8081/api/auth/login` para obtener un JWT.
4. Usar ese token (header `Authorization: Bearer <token>`) en las peticiones a cualquiera de los 3 BFF.

## Limitaciones conocidas

- El dataset de origen presenta `cuenta_id_legacy` duplicado entre distintas personas en `cuentas_bancarias`; por ello, los endpoints usan el `id` autogenerado de la tabla como identificador confiable.
- La validación de "propiedad de cuenta" (usuario ↔ cuenta) se basa en `cuenta_id_legacy`, heredando parcialmente esa limitación: puede permitir acceso a cuentas distintas que compartan el mismo valor legacy.
- Conforme a las indicaciones de la actividad, esta entrega implementa únicamente operaciones de lectura (`GET`); no se exponen operaciones de escritura desde los BFF.
