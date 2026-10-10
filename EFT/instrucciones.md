# Instrucciones de ejecucion

Este documento explica como levantar el sistema Banco XYZ y como probarlo con Postman.

## 1. Requisitos

- Docker Desktop en ejecucion.
- Postman.
- Git.
- El cluster de Kafka en AWS EC2 encendido (ver `despliegue.md`).
- Una base de datos PostgreSQL vacia a la que se pueda conectar por JDBC (por ejemplo, un proyecto gratuito en Neon). El sistema no incluye una base compartida: cada persona usa la suya.

## 2. Preparacion

1. Clonar el repositorio y entrar a la carpeta `EFT`. Esta es la carpeta raiz del proyecto: contiene el `docker-compose.yml` y las carpetas de todos los servicios. Todos los comandos de este documento se ejecutan desde ahi.
2. Crear un archivo `.env` en la misma carpeta que `docker-compose.yml` con estas variables:

```
DB_URL=jdbc:postgresql://<host>/<nombre-de-la-base>?sslmode=require
DB_USER=
DB_PASSWORD=
JWT_SECRET=
```

Los valores no se incluyen en el repositorio.

| Variable | Descripcion |
|---|---|
| `DB_URL` | URL JDBC de la base PostgreSQL propia |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASSWORD` | Clave de la base de datos |
| `JWT_SECRET` | Clave con la que se firman los tokens de usuario |

La base debe existir y estar vacia. Las tablas de usuarios se crean cuando arranca `banco-central-clientes` (script `schema-postgresql.sql`) y los datos de cuentas y transacciones se cargan al ejecutar el batch (seccion 4). Por eso hay que ejecutar el batch al menos una vez antes de las pruebas de la seccion 6.

3. Verificar que Kafka UI abre en `http://<IP-de-la-EC2>:8090`. Si no abre, encender la instancia EC2 y esperar 1 a 2 minutos. La IP de la EC2 es la que se configura en los servicios (ver `despliegue.md`).

## 3. Levantar los servicios

Desde la carpeta raiz del proyecto (`EFT`):

```
docker compose up -d --build
```

Esperar entre 1 y 2 minutos. Algunos servicios arrancan antes de que el config-server termine de iniciar, fallan y se reinician solos; es normal.

Revisar el estado:

```
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Los 9 servicios deben aparecer en estado `Up`:

| Servicio | Puerto |
|---|---|
| config-server | 8888 |
| eureka-server | 8761 |
| auth-server | 9000 |
| banco-central-clientes | 8085 |
| banco-central-cuentas | 8086 |
| banco-central-pagos | 8087 |
| bff-cajeros | 8082 |
| bff-mobile | 8083 |
| bff-web | 8084 |

Si algun servicio queda en `Exited`, ejecutar `docker compose down` y luego `docker compose up -d` otra vez.

## 4. Ejecutar el proceso batch

El batch no arranca con el resto. Se ejecuta a demanda:

```
docker compose --profile batch run --rm banco-xyz-batch
```

Corre tres jobs en secuencia: `transaccionJob`, `cuentaBancariaJob` y `movimientoAnualJob`. Al terminar, el codigo de salida es 0 si todos completaron.

Si un job falla, el proceso lo reintenta hasta 3 veces con 5 segundos de espera. Si se agotan los intentos, termina con codigo de salida 1.

### Prueba de falla y reintento automatico

Para provocar una falla controlada se cambia el nombre del archivo de entrada de transacciones, de modo que el batch no lo encuentre.

1. Cambiar el nombre del archivo:

```
Rename-Item ".\banco-xyz-batch\src\main\resources\data\semana_3\movimientos_financieros_diarios.csv" "m.csv"
```

2. Reconstruir la imagen del batch (el archivo va dentro de la imagen) y ejecutarlo:

```
docker compose --profile batch build banco-xyz-batch
docker compose --profile batch run --rm banco-xyz-batch
echo "Exit code: $LASTEXITCODE"
```

3. Resultado esperado: el job de transacciones falla, se reintenta 3 veces con 5 segundos de espera entre intentos (cada intento crea una nueva instancia del job) y el proceso termina con el mensaje `Proceso batch abortado` y codigo de salida 1.

4. Restaurar el nombre original y reconstruir:

```
Rename-Item ".\banco-xyz-batch\src\main\resources\data\semana_3\m.csv" "movimientos_financieros_diarios.csv"
docker compose --profile batch build banco-xyz-batch
docker compose --profile batch run --rm banco-xyz-batch
echo "Exit code: $LASTEXITCODE"
```

La ejecucion normal debe terminar con los tres jobs en estado `COMPLETED` y codigo de salida 0.

Importante: cada ejecucion del batch vuelve a generar las cuentas, por lo que los ids de `cuentas_bancarias` cambian. Despues de correr el batch hay que consultar los ids nuevos en la base de datos del proyecto, antes de usar Postman:

```
SELECT id, cuenta_id_legacy, tipo, saldo_final
FROM cuentas_bancarias
WHERE cuenta_id_legacy IN (101, 102)
ORDER BY cuenta_id_legacy, tipo, id;
```

De esa lista se eligen las cuentas para las pruebas (ver seccion 5). Conviene ejecutar el batch una sola vez antes de probar y no volver a ejecutarlo mientras se prueba.

## 5. Consultar ids de cuentas

Cada vez que se ejecuta el batch cambian los ids. En la base de datos del proyecto (la misma de `DB_URL`):

```
SELECT id, cuenta_id_legacy, tipo, saldo_final
FROM cuentas_bancarias
WHERE cuenta_id_legacy IN (101, 102)
ORDER BY cuenta_id_legacy, tipo, id;
```

El usuario `steve` esta asociado a `cuenta_id_legacy = 102`. Para las pruebas se usa:

| Variable de Postman | Cuenta a elegir de la consulta |
|---|---|
| `cuenta_propia` | una cuenta de legacy 102 de tipo ahorro |
| `cuenta_hipoteca` | una cuenta de legacy 102 de tipo hipoteca o prestamo |
| `cuenta_ajena` | una cuenta de legacy 101 (cualquier tipo) |

El campo que devuelve la API como saldo es `saldo_final` (saldo mas interes menos retiros), no la columna `saldo`.

## 6. Pruebas con Postman

Crear una coleccion con estas variables:

| Variable | Valor |
|---|---|
| `auth_url` | `http://localhost:9000` |
| `clientes_url` | `http://localhost:8085` |
| `cuentas_url` | `http://localhost:8086` |
| `pagos_url` | `http://localhost:8087` |
| `cajeros_url` | `http://localhost:8082` |
| `mobile_url` | `http://localhost:8083` |
| `web_url` | `http://localhost:8084` |
| `config_url` | `http://localhost:8888` |
| `eureka_url` | `http://localhost:8761` |
| `jwtUsuario` | JWT del login de usuario |
| `token` | token OAuth2 del auth-server |
| `cuenta_propia` | id de una cuenta de **ahorro** de legacy 102 (cuenta de `steve`) |
| `cuenta_hipoteca` | id de una cuenta de **hipoteca o prestamo** de legacy 102 (tambien de `steve`) |
| `cuenta_ajena` | id de una cuenta de **legacy 101** (de otro cliente) |

Los ids de las tres ultimas variables se obtienen con la consulta de la seccion 5, despues de ejecutar el batch. Cambian cada vez que se ejecuta.

### Infraestructura

Metodo `GET`, sin autenticacion.

| Endpoint | Resultado esperado |
|---|---|
| `{{clientes_url}}/actuator/health` (igual para cuentas, pagos, cajeros, mobile, web y config) | `{"status":"UP"}` |
| `{{eureka_url}}/eureka/apps` con header `Accept: application/json` | Lista de los servicios registrados |

El health del auth-server requiere autenticacion y no se incluye.

### Seguridad

| Endpoint | Entrada | Resultado esperado |
|---|---|---|
| `POST {{auth_url}}/oauth2/token` | Basic Auth con el client `ms-seguridad-client` y su secret de desarrollo. Body `x-www-form-urlencoded`: `grant_type=client_credentials`, `scope=cuentas.read` | 200 con `access_token` (dura unos 5 minutos). Copiarlo a la variable `token` |
| `POST {{clientes_url}}/api/auth/login` | Body JSON `{"username":"steve","password":"<clave de desarrollo>"}` | 200 con `token` (dura 1 hora). Copiarlo a la variable `jwtUsuario` |
| `POST {{clientes_url}}/api/auth/login` | Clave incorrecta | Error de credenciales. Al enviarlo 3 veces seguidas (Runner con 3 iteraciones) se publica un evento en el topic `alertas-seguridad`, visible en Kafka UI |

### BFF Cajeros

Authorization: Bearer Token con `{{jwtUsuario}}`, salvo donde se indica.

| Endpoint | Valor | Resultado esperado |
|---|---|---|
| `GET {{cajeros_url}}/bff-cajero/cuentas/{{cuenta_propia}}/saldo` | cuenta propia | 200 con el saldo |
| `GET {{cajeros_url}}/bff-cajero/cuentas/{{cuenta_ajena}}/saldo` | cuenta de otro cliente | 403 |
| `GET {{cajeros_url}}/bff-cajero/cuentas/{{cuenta_propia}}/saldo` | Bearer `{{token}}` (OAuth2) en vez del JWT de usuario | 401 |
| `POST {{cajeros_url}}/bff-cajero/cuentas/{{cuenta_propia}}/retiros` con body `{"monto": 100.00}` | cuenta de ahorro propia | 202 con `solicitudId` y estado `PENDIENTE` |
| `POST {{cajeros_url}}/bff-cajero/cuentas/{{cuenta_hipoteca}}/retiros` con body `{"monto": 100.00}` | cuenta propia que no es de ahorro | 202 `PENDIENTE`; el estado final es `RECHAZADO` (solo se permiten retiros desde cuentas de ahorro) |
| `POST {{cajeros_url}}/bff-cajero/cuentas/{{cuenta_ajena}}/retiros` con body `{"monto": 100.00}` | cuenta de otro cliente | 403, no se crea la solicitud |
| `GET {{cajeros_url}}/bff-cajero/cuentas/retiros/<solicitudId>` | `solicitudId` devuelto por el retiro | Resultado del retiro: `APROBADO` con saldo inicial y final, o `RECHAZADO` con el motivo |
| `GET {{cajeros_url}}/bff-cajero/cuentas/{{cuenta_propia}}/saldo-oauth2` | Bearer `{{token}}` | 200 con el saldo |

Despues de un retiro aprobado, volver a consultar el saldo: baja el monto retirado.

### BFF Web

Authorization: Bearer Token con `{{jwtUsuario}}`, salvo la ruta OAuth2.

| Endpoint | Valor | Resultado esperado |
|---|---|---|
| `GET {{web_url}}/bff-web/cuentas/{{cuenta_propia}}` | cuenta propia | 200 con el detalle de la cuenta |
| `GET {{web_url}}/bff-web/cuentas/{{cuenta_propia}}/dashboard` | cuenta propia | 200 con cuenta, transacciones y movimientos |
| `GET {{web_url}}/bff-web/cuentas/{{cuenta_ajena}}/dashboard` | cuenta de otro cliente | 403 |
| `GET {{web_url}}/bff-web/cuentas/999999/dashboard` | cuenta inexistente | 404 |
| `GET {{web_url}}/bff-web/cuentas/{{cuenta_propia}}/saldo-oauth2` | Bearer `{{token}}` | 200 con el saldo |

### BFF Mobile

| Endpoint | Valor | Resultado esperado |
|---|---|---|
| `GET {{mobile_url}}/bff-mobile/cuentas/{{cuenta_propia}}/saldo` con Bearer `{{jwtUsuario}}` | cuenta propia | 200 con el saldo |
| `GET {{mobile_url}}/bff-mobile/cuentas/{{cuenta_ajena}}/saldo` con Bearer `{{jwtUsuario}}` | cuenta de otro cliente | 403 |
| `GET {{mobile_url}}/bff-mobile/cuentas/{{cuenta_propia}}/saldo-oauth2` con Bearer `{{token}}` | cuenta propia | 200 con el saldo |

### Resiliencia

1. Detener el servicio de cuentas:

```
docker stop banco-central-cuentas
```

2. Enviar varias veces seguidas `GET {{cajeros_url}}/bff-cajero/cuentas/{{cuenta_propia}}/saldo` con Bearer `{{jwtUsuario}}`. Las primeras respuestas tardan mas por los reintentos y el timeout; despues el circuit breaker se abre y la respuesta es inmediata, con un error controlado.
3. Volver a levantar el servicio:

```
docker start banco-central-cuentas
```

4. Esperar unos 40 segundos y repetir la request. Debe volver a responder 200.

## 7. Detener el sistema

```
docker compose down
```
