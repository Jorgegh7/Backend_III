# config-server

Servidor de configuración centralizada (Spring Cloud Config Server). Sirve el `application.yml`/`.properties` de los demás microservicios desde la carpeta `config-repo`, evitando que cada uno mantenga su configuración de forma local y dispersa.

## Puerto
`8888`

## Tecnologías clave
- Spring Boot 4.1.1 + Spring Cloud Config Server
- Perfil `native` (lee archivos locales, no un repositorio Git remoto)

## Configuración relevante
```yaml
spring.cloud.config.server.native.search-locations: file:///config-repo
```
En Docker, `config-repo` se monta como volumen (`./config-repo:/config-repo`) en el contenedor.

## Dependencias con otros servicios
Ninguna — es el primero en arrancar; los demás servicios dependen de él, no al revés.

## Verificación
```
GET http://localhost:8888/banco-central-xyz/default
```
Devuelve la configuración completa servida a `banco-central-xyz`.
