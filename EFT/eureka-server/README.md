# eureka-server

Servidor de registro y descubrimiento de servicios (Spring Cloud Netflix Eureka).
Cada microservicio y cada BFF se registra aquí al arrancar.

- **Puerto:** 8761
- **Tecnología:** Spring Cloud Netflix Eureka Server (Java 17)

## Uso

| Ruta | Descripción |
|---|---|
| `http://localhost:8761` | Panel con las instancias registradas |
| `/actuator/health` | Estado del servicio |

Con el sistema levantado, deben aparecer registrados los servicios de dominio y los tres BFF.