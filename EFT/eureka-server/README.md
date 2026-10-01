# eureka-server

Servidor de descubrimiento de servicios (Netflix Eureka). Actúa como directorio central: cada microservicio se registra al arrancar y confirma periódicamente que sigue activo mediante *heartbeats*.

## Puerto
`8761`

## Tecnologías clave
- Spring Boot 4.1.1 + Spring Cloud Netflix Eureka Server

## Configuración relevante
```yaml
eureka.client.register-with-eureka: false
eureka.client.fetch-registry: false
eureka.server.enable-self-preservation: false
```
Como este proyecto **es** el servidor, no se registra a sí mismo como cliente. `enable-self-preservation: false` es apropiado para desarrollo: elimina instancias inmediatamente al dejar de recibir *heartbeats*, sin esperar a confirmar que no sea un problema de red.

## Dependencias con otros servicios
Ninguna — recibe registros de `banco-central-xyz`, `bff-cajeros`, `bff-mobile` y `bff-web`, pero no depende de ellos para arrancar.

## Verificación
```
http://localhost:8761
```
Dashboard web mostrando las instancias registradas y su estado (`UP`).
