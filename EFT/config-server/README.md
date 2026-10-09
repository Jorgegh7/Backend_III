# config-server

Servidor de configuración centralizada (Spring Cloud Config) en modo `native`. Entrega a los microservicios
la configuración común guardada en la carpeta `config-repo/`.

- **Puerto:** 8888
- **Tecnología:** Spring Cloud Config Server (Java 17)

## Qué entrega

`config-repo/application.yml` contiene la configuración compartida por todos los servicios:
conexión a la base de datos, URL del `jwk-set-uri`, conexión a Kafka (brokers, serializadores y paquetes de confianza),
registro en Eureka y exposición de Actuator. Las contraseñas y secretos no están en el archivo: se leen
de variables de entorno (`DB_PASSWORD`, `JWT_SECRET`).

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/{aplicacion}/default` | Configuración que recibe un servicio, por ejemplo `/banco-central-cuentas/default` |
| GET | `/actuator/health` | Estado del servicio |

## Notas

Los demás servicios arrancan después de que el config-server esté sano; si arrancan antes fallan y
se reinician hasta que responde (ver `instrucciones.md`).