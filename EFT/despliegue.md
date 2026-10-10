# Pasos de despliegue

Este documento describe como se despliega Banco XYZ: el cluster de Kafka en AWS EC2, los microservicios con Docker Compose y la base de datos en Neon.

## 1. Vista general

| Componente | Donde corre |
|---|---|
| Cluster Kafka (3 Zookeeper, 3 brokers) y Kafka UI | AWS EC2, con Docker Compose |
| Microservicios, BFF, config, eureka y auth | Docker Compose, red `banco-net` |
| Proceso batch | Docker Compose, perfil `batch` (a demanda) |
| Base de datos | Neon PostgreSQL, base `bancoxyz_eft` |

Los servicios se conectan a Kafka por la IP elastica de la instancia EC2.

## 2. Base de datos (Neon)

- Motor: PostgreSQL. En el desarrollo se uso Neon (servicio gestionado), pero sirve cualquier PostgreSQL accesible por JDBC.
- Cada persona debe crear su propia base de datos vacia; el repositorio no incluye una base compartida.
- La conexion se entrega a los contenedores con variables de entorno del archivo `.env` (ver seccion 4.1): `DB_URL`, `DB_USER` y `DB_PASSWORD`. El `.env` tiene en total cuatro variables; la cuarta es `JWT_SECRET`.
- El script `schema-postgresql.sql` de `banco-central-clientes` crea la tabla de usuarios y sus datos de prueba.
- Los datos de cuentas y transacciones se cargan ejecutando el proceso batch (seccion 4.6).

## 3. Kafka en AWS EC2

El cluster se define en `kafka-ec2/docker-compose.yml`.

### 3.1 Crear la instancia

1. En la consola de AWS, lanzar una instancia EC2 con Amazon Linux 2. Tipo de instancia recomendado: `c7i-flex.large`. Kafka requiere bastantes recursos, porque en la misma instancia corren 7 contenedores Java (3 Zookeeper y 3 brokers) mas Kafka UI; un tipo mas pequeno puede dejar los contenedores sin memoria o muy lentos.
2. Asignar una IP elastica a la instancia (EC2, Elastic IPs, Allocate, Associate). Esa IP queda fija aunque la instancia se apague y se encienda, y es la que anuncian los brokers a los clientes.
3. Configurar el security group con las siguientes reglas de entrada:

| Puerto | Uso |
|---|---|
| 22 | Acceso SSH |
| 29092 | broker kafka-1 |
| 39092 | broker kafka-2 |
| 49092 | broker kafka-3 |
| 8090 | Kafka UI |

Los puertos de Zookeeper (22181, 32181, 42181) no necesitan quedar abiertos al exterior.

### 3.2 Instalar Docker y Docker Compose

Conectarse por SSH a la instancia (usuario `ec2-user`). El comando para instalar Docker depende de la distribucion de la instancia:

| Distribucion | Gestor de paquetes | Comando |
|---|---|---|
| Amazon Linux 2 | `yum` | `sudo yum install -y docker` |
| Amazon Linux 2023 | `dnf` | `sudo dnf install -y docker` |

En este proyecto la instancia usa Amazon Linux 2, por lo que se ejecuta:

```
sudo yum install -y docker
sudo systemctl enable --now docker
```

Para saber que distribucion tiene una instancia: `cat /etc/os-release`.

Agregar el usuario al grupo `docker` para no usar `sudo` con cada comando:

```
sudo usermod -aG docker ec2-user
```

Cerrar la sesion SSH y volver a conectarse para que el cambio de grupo tenga efecto. Comprobar con:

```
docker ps
```

Descargar el binario de Docker Compose y dejarlo ejecutable:

```
sudo curl -SL "https://github.com/docker/compose/releases/download/v2.24.6/docker-compose-linux-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose version
```

### 3.3 Copiar el compose y definir la IP

En la instancia, crear el archivo del compose con un editor de texto (por ejemplo `nano`) y pegar el contenido de `kafka-ec2/docker-compose.yml` del repositorio:

```
nano docker-compose.yml
```

Para guardar y salir de nano: `Ctrl+O`, `Enter` y `Ctrl+X`.

El compose usa la variable `${KAFKA_PUBLIC_IP}` en el listener externo de los tres brokers (`KAFKA_ADVERTISED_LISTENERS`). Debe ser la IP elastica de la instancia y no la IP privada. Hay dos formas de definirla; se elige una.

**Opcion A: con un archivo `.env`.** Se deja el compose tal como esta y se crea un `.env` en la misma carpeta:

```
nano .env
```

Contenido:

```
KAFKA_PUBLIC_IP=<IP-de-la-EC2>
```

Docker Compose lee ese archivo automaticamente y reemplaza la variable en los tres brokers. La IP se escribe una sola vez.

**Opcion B: sin `.env`.** En el compose, reemplazar `${KAFKA_PUBLIC_IP}` por la IP elastica en las tres lineas `KAFKA_ADVERTISED_LISTENERS` (una por broker), por ejemplo:

```
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-1:9092,PLAINTEXT_HOST://<IP-de-la-EC2>:29092
```

Con esta opcion no hace falta crear ningun otro archivo, pero hay que editar las tres lineas (puertos 29092, 39092 y 49092).

### 3.4 Cluster

El compose define:

- 3 nodos Zookeeper (`confluentinc/cp-zookeeper:7.4.4`) con healthcheck `srvr`.
- 3 brokers Kafka (`confluentinc/cp-kafka:7.4.4`), cada uno con dos listeners: uno interno (`kafka-N:9092`) para la comunicacion entre brokers y uno externo (`PLAINTEXT_HOST`) anunciado con la IP elastica y el puerto 29092, 39092 o 49092.
- Replicacion de los topics internos con factor 2 y `min.insync.replicas=2`.
- Creacion automatica de topics desactivada (`KAFKA_AUTO_CREATE_TOPICS_ENABLE=false`). Los topics los crea el servicio `kafka-init` (ver seccion 3.6).
- Kafka UI (`provectuslabs/kafka-ui`) en el puerto 8090.
- Volumenes de Docker para los datos de Zookeeper y de los brokers.

### 3.5 Puesta en marcha

En la instancia:

```
docker-compose up -d
```

Esperar 1 a 2 minutos y abrir `http://<IP-de-la-EC2>:8090` para comprobar que los tres brokers aparecen en linea.

Si la instancia estaba detenida, primero se enciende desde la consola de AWS. Los datos se conservan en los volumenes de Docker.

### 3.6 Topics

Como la creacion automatica esta desactivada, los topics se crean con el servicio `kafka-init` del mismo compose. Este servicio espera a que los tres brokers esten saludables, crea los cinco topics con 3 particiones y factor de replicacion 3 (`--if-not-exists`, por lo que se puede ejecutar mas de una vez sin problema), imprime `Topicos creados correctamente.` y termina. Es normal que `kafka-init` aparezca luego con estado `Exited (0)`.

Para comprobarlo:

```
docker logs kafka-init
```

o abrir Kafka UI (`http://<IP-de-la-EC2>:8090`, seccion Topics).

El sistema usa cinco topics:

| Topic | Productor | Consumidor |
|---|---|---|
| `retiros-solicitados` | bff-cajeros | banco-central-pagos |
| `retiros-aprobados` | banco-central-pagos | bff-cajeros |
| `retiros-rechazados` | banco-central-pagos | bff-cajeros |
| `transacciones-completadas` | banco-central-pagos | banco-central-clientes |
| `alertas-seguridad` | banco-central-clientes | banco-central-pagos |

## 4. Microservicios (Docker Compose)

### 4.1 Variables de entorno

Se definen en un archivo `.env` junto al `docker-compose.yml`. Este archivo no se sube al repositorio.

```
DB_URL=jdbc:postgresql://<host>/<nombre-de-la-base>?sslmode=require
DB_USER=
DB_PASSWORD=
JWT_SECRET=
```

| Variable | Descripcion | La usan |
|---|---|---|
| `DB_URL` | URL JDBC de la base PostgreSQL | clientes, cuentas, pagos y batch |
| `DB_USER` | Usuario de la base | clientes, cuentas, pagos y batch |
| `DB_PASSWORD` | Clave de la base | clientes, cuentas, pagos y batch |
| `JWT_SECRET` | Clave de firma de los tokens de usuario | clientes, cuentas y pagos |

La IP de Kafka que usan los servicios esta en la configuracion de `config-repo`; si cambia la IP elastica, debe actualizarse alli.

### 4.2 Servicios y puertos

| Servicio | Puerto | Imagen |
|---|---|---|
| config-server | 8888 | build `./config-server` |
| eureka-server | 8761 | build `./eureka-server` |
| auth-server | 9000 | build `./auth-server` |
| banco-central-clientes | 8085 | build `./banco-central-clientes` |
| banco-central-cuentas | 8086 | build `./banco-central-cuentas` |
| banco-central-pagos | 8087 | build `./banco-central-pagos` |
| bff-cajeros | 8082 | build `./bff-cajeros` |
| bff-mobile | 8083 | build `./bff-mobile` |
| bff-web | 8084 | build `./bff-web` |
| banco-xyz-batch | sin puerto | build `./banco-xyz-batch`, perfil `batch` |

Todos usan la red `banco-net`. El config-server monta la carpeta `config-repo` como volumen.

### 4.3 Orden de arranque

El compose declara estas dependencias:

1. `config-server`: debe estar saludable (healthcheck sobre `/actuator/health`).
2. `eureka-server`: arranca despues del config-server.
3. `auth-server`.
4. `banco-central-clientes` y `banco-central-cuentas`: esperan al config-server (saludable), a eureka y al auth-server.
5. `banco-central-pagos`: ademas espera a cuentas.
6. `bff-cajeros`: espera a eureka.
7. `bff-mobile`: espera a config, eureka, auth y cuentas.
8. `bff-web`: espera a config, eureka, auth, cuentas y pagos.

Los servicios tienen `restart: unless-stopped`. Si alguno arranca antes de que la configuracion este disponible, falla y se reinicia hasta quedar operativo; esto puede tardar uno o dos minutos.

### 4.4 Despliegue

Desde la carpeta `EFT`:

```
docker compose up -d --build
```

Para ver el estado:

```
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Para ver los logs de un servicio:

```
docker logs <nombre-del-servicio> --tail 50
```

### 4.5 Actualizar un servicio

Para reconstruir y reiniciar un solo servicio despues de cambiar su codigo:

```
docker compose up -d --build <nombre-del-servicio>
```

### 4.6 Proceso batch

No se levanta con `docker compose up`. Se ejecuta a demanda:

```
docker compose --profile batch run --rm banco-xyz-batch
```

Usa `restart: "no"`. Al terminar devuelve codigo de salida 0 si todos los jobs completaron y 1 si se agotaron los reintentos.

## 5. Verificacion del despliegue

1. Los 9 servicios en estado `Up`.
2. Respuesta `UP` en `/actuator/health` de cada servicio (excepto auth-server, que lo protege con autenticacion).
3. Servicios registrados en `http://localhost:8761`.
4. Kafka UI muestra los tres brokers y los cinco topics.
5. Una solicitud de retiro desde el BFF cajeros queda en estado `APROBADO` o `RECHAZADO`, lo que confirma la comunicacion con Kafka (ver `instrucciones.md`).

## 6. Detener y limpiar

Microservicios:

```
docker compose down
```

Kafka (en la instancia EC2):

```
docker-compose down
```

Los volumenes de Kafka se conservan mientras no se use la opcion `-v`.
