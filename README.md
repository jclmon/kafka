# APACHE KAFKA

# ZOOKEEPER

ZooKeeper servicio de coordinación centralizada para aplicaciones distribuidas.
Soluciona problemas como:
 - Almacenamiento de configuración.
 - Bloqueos distribuidos.
 - Elección de líder.

Proporciona una estructura similar a la que se encuentra en un sistemas de ficheros. Cada elemento de la estructura es conocido como ZNode.
Los ZNodes pueden contener información en su interior. Por ejemplo un JSON String.
El servicio esta formado por distintos nodos cada uno con un servidor.
Uno de los servidores actúa como leader. Encargado de la coordinación interna. Siempre tiene que existir Quórum, para validar las escrituras.

## INSTALACIÓN ZOOKEEPER

Descargar el paquete de zookeeper: http://zookeeper.apache.org/releases.html

Extraer el paquete:
```
$ cd opt/
$ tar -zxf zookeeper-3.4.6.tar.gz
```
Arranque de zookeeper
La primera vez hay que renombrar el fichero de configuración
```
[root@vb-dev-1 apache-zookeeper-3.6.0-bin]# cd conf/
[root@vb-dev-1 conf]# ls
configuration.xsl  log4j.properties  zoo_sample.cfg
[root@vb-dev-1 conf]# cp zoo_sample.cfg zoo.cfg
```
Cambio el puerto si está ocupado
```
admin.enableServer=true
admin.serverPort=9990
```
Abro el puerto de zookeeper
```
Last login: Thu Apr 16 06:49:00 2020 from 192.168.2.33
[root@vb-dev-1 ~]# firewall-cmd --permanent --add-port=2181/tcp
success
[root@vb-dev-1 ~]# firewall-cmd --reload
success
[root@vb-dev-1 ~]# firewall-cmd --list-ports
2181/tcp
```
Si quiero borrar logs anteriores
```
rm -rf /tmp/kafka-logs
```
Arrancar zookeeper
```
./bin/zkServer.sh start
```
Compruebo con el cliente
```
bin/zkCli.sh
[zk: localhost:2181(CONNECTED) 3] ls /zookeeper/quota
[]
[zk: localhost:2181(CONNECTED) 4] get /zookeeper/quota
```
Si se arrancan varios brokers, se configura en server.properties el zookeeper arrancado en la primera y el resto se conectan a esta, además se identifican los broker.id

# KAFKA

- Sistema distribuido de paso de mensajes.
- Desarrollado por LinkedIn y adoptado por la fundación Apache.
- Desarrolladores de Kafka fundan una empresa llamada Confluent, especializada en Kafka.
- Existen productores y consumidores.

Modelos
 - Modelo de colas: Los mensajes son repartidos.
 - Modelo publicador/subscriptor: Los mensajes se difunden en broadcast.

# INSTALACIÓN KAFKA
Descargar el paquete de kafka desde: https://www.apache.org/dyn/closer.cgi?path=/kafka/2.5.0/kafka_2.12-2.5.0.tgz
```
$ cd opt/
$ tar -zxf kafka_2.12-2.5.0.tgz
$ cd kafka_2.12-2.5.0
```
Arranque de kafka
```
[root@srvdev kafka_2.11-2.4.1]# bin/kafka-server-start.sh -daemon config/server.properties
```
Una vez arrancado se ve lo que hace kafka sobre zookeeper entrando con el cliente
```
WatchedEvent state:SyncConnected type:None path:null
[zk: localhost:2181(CONNECTED) 0] ls /
[admin, brokers, cluster, config, consumers, controller, controller_epoch, isr_change_notification, latest_producer_id_block, log_dir_event_notification, zookeeper]
```

# KAFKA BROKERS
- Servicio principal de Kafka.
- Almacena las distintas colas de mensajes (topics).
- Utilizados para crear clusters.
- Sincronización utilizando Apache Zookeeper.

### Configuración Broker
Fichero de configuración básico: server.properties
Server Basics
```
broker.id=0
Socket Server Settings
listeners=PLAINTEXT://0.0.0.0:9092
Log Basics
log.dirs=/tmp/kafka-logs
num.partitions=1
Log Retention Policy
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
log.cleaner.enable=false
Zookeeper
zookeeper.connect=localhost:2181
zookeeper.connection.timeout.ms=600
```
### ID Automático
El broker.id se puede generar de manera aleatoria.
Gran utilidad para autocreación de broker en entornos cloud como AWS.
```
broker.id.generation.enable=true
reserved.broker.max.id=1000
```

# KAFKA TOPICS
Los topics son las distintas colas de mensajes que se encuentran en Kafka:

## Particiones
Los topics están formados por particiones
- Cada partición es un fichero que se encuentra en el disco.
- Los ficheros son denominados logs.
- Internamente cada mensaje dentro de log es identificado por un offset.
- Las particiones se distribuyen entre los brokers.
- Las particiones son utilizadas para balancear los mensajes a través de los brokers.
		Más brokers y particiones == mayor througput
		---
		
## Replicas
- Las topic de Kafka tiene la posibilidad de configurar replicas.
- Las replicas son utilizadas para conseguir alta disponibilidad del servicio.
- Las replicas se consiguen mediante la duplicación de las particiones en distintos brokers.
		Máximas replicas == Numero de brokers.
		---
		
Al incrementar las replicas hacemos el sistema mas robusto ante caídas.
Las replicas implican un incremento del uso del ancho de banda entre los brokers.
Las replicas provocan la disminución de la tasa de producción de mensajes por segundos a un topic, si se activa el asentimiento por replica.

## Mensajes

Los mensajes están formados por:
• TIMESTAMP
• CLAVE
• VALOR
La clave suele ser usada para decidir a que partición se envía cada mensaje.


## CREAR TOPICS
num.partitions : Número de particiones por defecto cuando se crea una topic.
default.replication.factor : Valor del factor de replicación por defecto. 
Ambos valores son de utilidad cuando esta activa la creación automática de topics: auto.create.topics.enable
```
[root@srvdev kafka]# cd kafka_2.11-2.4.1
[root@srvdev kafka_2.11-2.4.1]# bin/kafka-topics.sh --create --partitions 1 --replication-factor 1 --topic test1 --zookeeper localhost:2181
Created topic test1.
[root@srvdev kafka_2.11-2.4.1]# bin/kafka-topics.sh --list --zookeeper localhost:2181
test1
[root@srvdev kafka_2.11-2.4.1]# bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic test1
Topic: test1    PartitionCount: 1       ReplicationFactor: 1    Configs:
        Topic: test1    Partition: 0    Leader: 0       Replicas: 0     Isr: 0

bin/kafka-topics.sh --create --partitions 3 --topic test --zookeeper localhost:2181 --replication-factor 3
bin/kafka-topics.sh --describe --topic test --zookeeper localhost:2181
```

## PRODUCTOR CONSUMIDOR
Los topics están formados por particiones
Modelo de colas: Los mensajes son repartidos.
Modelo publicador/subscriptor: Los mensajes se difunden en broadcast.

### Productores
Los productores son los encargados de enviar los mensajes a los distintos topics.
Los mensajes se envían directamente al broker que tiene el leader de la partición.
	1.	El productor pregunta a cualquier broker.
	2.	El broker devuelve el leader de la partición.
Los mensajes se pueden producir utilizando un particionado o de manera aleatoria.
•	Normalmente se utiliza la clave de los mensajes para realizar el particionado.
•	Los mensajes se pueden enviar por lotes de manera asíncrona:
	o	Basado en tiempo.
	o	Basado en número.

productor
---
```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test1
```
consumidor
---
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test1
```
productor
---
```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test1 --property parse.key=true --property key.separator=,
```
consumidor
---
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test1 --from-beginning --property print.key=true 
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test1 --from-beginning --property print.key=true --consumer.config config/consumer.properties
[root@srvdev kafka_2.11-2.4.1]# ls /tmp/kafka-logs/
cleaner-offset-checkpoint  __consumer_offsets-20  __consumer_offsets-33  __consumer_offsets-46
__consumer_offsets-0       __consumer_offsets-21  __consumer_offsets-34  __consumer_offsets-47
__consumer_offsets-1       __consumer_offsets-22  __consumer_offsets-35  __consumer_offsets-48
__consumer_offsets-10      __consumer_offsets-23  __consumer_offsets-36  __consumer_offsets-49
__consumer_offsets-11      __consumer_offsets-24  __consumer_offsets-37  __consumer_offsets-5
__consumer_offsets-12      __consumer_offsets-25  __consumer_offsets-38  __consumer_offsets-6
__consumer_offsets-13      __consumer_offsets-26  __consumer_offsets-39  __consumer_offsets-7
__consumer_offsets-14      __consumer_offsets-27  __consumer_offsets-4   __consumer_offsets-8
__consumer_offsets-15      __consumer_offsets-28  __consumer_offsets-40  __consumer_offsets-9
__consumer_offsets-16      __consumer_offsets-29  __consumer_offsets-41  log-start-offset-checkpoint
__consumer_offsets-17      __consumer_offsets-3   __consumer_offsets-42  meta.properties
__consumer_offsets-18      __consumer_offsets-30  __consumer_offsets-43  recovery-point-offset-checkpoint
__consumer_offsets-19      __consumer_offsets-31  __consumer_offsets-44  replication-offset-checkpoint
__consumer_offsets-2       __consumer_offsets-32  __consumer_offsets-45  test1-0
[root@srvdev kafka_2.11-2.4.1]# ls /tmp/kafka-logs/test1-0/
00000000000000000000.index  00000000000000000000.log  00000000000000000000.timeindex  leader-epoch-checkpoint
[root@srvdev kafka_2.11-2.4.1]# more /tmp/kafka-logs/test1-0/00000000000000000000.log
```
## Log Compaction
Esta propiedad asegura que se almacena al menos el último valor para cada clave en una partición.
CASO DE USO: Escenario donde se necesita recuperar un estado antes fallo, o restaurar caches.
Gran utilidad en Kafka Streams y Apache Samza.

## Gestión de espacio
- El disco duro no es infinito.
- Hay que realizar gestión del espacio que ocupan los datos.
- Kafka proporciona Log Retention Policy para controlar el espacio.

Existen dos formas de configurar las políticas de retención de datos:
- Tamaño de datos a almacenar.
- Tiempo que se desea almacenar los datos.

En primer lugar debemos de saber cuantos topics, particiones y que factor de realización vamos a tener.

Considerando:
• 4 Topics.
• 20 particiones cada topic.
• Factor de replicación de 2.

En primer lugar debemos de saber cuantos topics, particiones y que factor de realización vamos a tener.
Considerando:
• 4 Topics.
• 20 particiones cada topic.
• Factor de replicación de 2. (Mínimo 2 brokers)
4 topics * 20 particiones * 2 replicación = 160 kafka logs.
- Mínimo tenemos 2 brokers: 160 / 2 = 80 logs por broker.
- Ahora podemos realizar dos suposiciones:
	1. Tengo X tamaño de disco.
	2. Quiero almacenar X unidades de tiempo.

Tengo 450 GB de disco por broker:
- Recomendable dejar algo de espacio reservado para el sistema. Por ejemplo: 50GB
- (450GB - 50GB) / 80 logs por broker = 5GB por log.
```
log.cleaner.enable=true
log.retention.bytes=5368709120
log.segment.bytes=1073741824
log.retention.check.interval.ms=30000
```
`
Con la configuración que hemos aplicado conseguiremos:
- Activar el servicio de política de limpiado.
- Almacenar hasta 5 ficheros de log por partición de 1GB cada uno.
- Verificar si existe algún log para borrar cada 30 segundos

Quiero almacenar 1 semana de datos.
En esta suposición tenemos que saber la cantidad de datos que esperamos recibir: 100 eventos / segundo.
- Sabemos que 1 evento de media tiene 2048 bytes.
- 1 segundo = 2048 bytes * 100 eventos = 204800 bytes
- 1 semana = 7 días * 24 horas * 3600 seg = 604800 seg
- En una semana generamos 204800 * 604800 = 123.863.040.000 bytes ≈ 120GB

Después del calculo sabemos que para almacenar una semana de nuestros datos necesitaremos 120GB por cada broker.
- Necesitamos 120GB por cada broker debido a que habíamos dicho que queríamos un factor de replicación de 2.
```
log.cleaner.enable=true
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=30000
```
Se pueden configurar ambas:
• Tiempo deseado.
• Tamaño máximo.
- Configurando ambas conseguimos que si se tiene un disco pequeño y se reciben más datos de la cuenta el disco no se quede sin espacio.

## Configuración: Topics

### Creación de topics:
```
kafka-topics.sh --zookeeper localhost:2181 --create --topic topicTest --partitions 4 --replication-factor 2 —config x=y
```
En la creación se indica:
- Numero de particiones: 4
- Numero de replicas: 2
- Opcional: Configuraciones de topic

### Eliminar topics:
```
kafka-topics.sh --zookeeper localhost:2181 --delete --topic topicTest
```
NOTA: Para poder eliminar topics es necesario habilitar la siguiente propiedad en el fichero server.properties
delete.topic.enable=true

### Añadir configuración:
```
kafka-topics.sh --zookeeper localhost:2181 --alter --topic topicTest --config x=y
```
### Eliminar configuración:
```
kafka-topics.sh --zookeeper localhost:2181 --alter --topic topicTest --deleteConfig x
```
### Configuración: Particiones
### Añadir particiones:
```
root@kschool:~# kafka-topics.sh --zookeeper localhost:2181 --alter --topic topicTest --partitions 40
```
NOTA: Las particiones no se pueden disminuir.

### Configuración: Replicas
Para añadir replicas se usa el siguiente comando:
```
bin/kafka-reassign-partitions.sh --zookeeper localhost:2181 --reassignment-json-file incremento-replicas.json --execute 
```

El contenido del fichero incremento-replicas.json es:
```
{"version":1, "partitions":[{"topic":"topicTest", "partition":0, “replicas":[1,2,3]}]}
```
Mediante el fichero JSON, se indica que particiones (0) de que topic (topicTest) están replicas en que brokers (1, 2, 3).

Para verificar que se ha aplicado correctamente podemos usar:
```
bin/kafka-reassign-partitions.sh --zookeeper localhost:2181 --reassignment-json-file incremento-replicas.json --verify
```

### Configuración: Log Compaction
Por defecto el limpiador esta desactivado. Para activar en el server.properties:
```
log.cleaner.enable=true
```
Log Compaction se habilita por topics:
```
kafka-topics.sh --zookeeper localhost:2181 --create --topic topicTest --config cleanup.policy=compact --partitions 4 --replication-factor 2
kafka-topics.sh --zookeeper localhost:2181 --alter --topic topicTest --config cleanup.policy=compact
```

## Configuración: Productor y Consumidor

## Configuración: Productor Básica
Las principales propiedades para el productor son:
- bootstrap.servers : Lista de brokers separados con comas indicando sus puertos.
- key.serializer : Clase utilizada para serializar las claves de los mensajes.
- value.serializer : Clase utilizada para serializar el payload del mensaje.

### Serializadores
Existen multiples serializadores:
- String org.apache.kafka.common.serialization.StringSerializer
- Long org.apache.kafka.common.serialization.LongSerializer
- Integer org.apache.kafka.common.serialization.IntegerSerializer
- Double org.apache.kafka.common.serialization.DoubleSerializer
- Bytes org.apache.kafka.common.serialization.BytesSerializer
- ByteArray org.apache.kafka.common.serialization.ByteArraySerializer
- ByteBuffer org.apache.kafka.common.serialization.ByteBufferSerializer

Se pueden crear serializadores propios implementando la interfaz org.apache.kafka.common.serialization.Serializer
Serializador que convierte un java.util.Map en un JSON String:
```
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import java.util.Map;
public class JsonSerializer implements Serializer<Map<String, Object>> {
ObjectMapper mapper = new ObjectMapper();
public void configure(Map<String, ?> configs, boolean isKey) {}
public byte[] serialize(String topic, Map<String, Object> data) {
byte[] raw = null;
try {
raw = mapper.writeValueAsBytes(data);
} catch (JsonProcessingException e) {
e.printStackTrace();
}
return raw;
}
public void close() {}
}
```
### Entrega de mensajes
Existen configuración que influyen en el envío y entrega de mensajes:
- retries : Número de intento que se envía un mensaje si se produce algún fallo.
Nota: Puede provocar que se produzcan mensajes duplicados, pero conseguimos entrega: At least one.
• ack :
 0 : El productor no espera ningún asentimiento.
 1 : El productor espera asentimiento por la partición líder.
 all : El productor espera el asentimiento por la partición líder y por las replicas.

Kafka también nos permite configurar si deseamos enviar los mensajes utilizando algún tipo de compresión mediante la propiedad:
compression.type
- Los valores existentes son: none, gzip, snappy, o lz4.
- Por defecto el valor es none.

A la hora d e enviar los mensajes debemos decidir a que partición enviar el mensaje, tenemos dos opciones:
1. Indicar la partición a la hora de enviar cada mensaje.
2. Utilizar un sistema d e particionado

Para implementar un sistema de particionado debemos implementar la interfaz org.apache.kafka.clients.producer.Partitioner.
```
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import java.util.Map;
public class SimplePartitioner implements Partitioner {
@Override
public int partition(String topic, Object key, byte[] keyBytes,
Object value, byte[] valueBytes, Cluster cluster) {
return Math.abs(key.hashCode() % cluster.partitionCountForTopic(topic));
}
@Override
public void close() {}
@Override
public void configure(Map<String, ?> conf) {}
}
```

Los mensajes en Kafka normalmente son enviados por lotes d e manera asíncrona para conseguir un mayor rendimiento. 
Configuración:
- batch.size : Número de mensajes que conforma un lote antes de enviarlo.
- linger.ms : Tiempo máximo de espera mientras se forma el lote antes de enviarlo. Si el valor es alcanzado el lote se envía con los mensajes que tenga.

### Identificación & Quotas
Para identificar a un cliente produciendo se puede configurar la siguiente propiedad: client.id 
Esta configuración es util si queremos definir Quotas.
Si deseamos configurar una cuenta de 1024 bytes por segundo para el cliente con id igual a cliente
```
bin/kafka-configs.sh --zookeeper localhost:2181 --alter --add-config 'producer_byte_rate=1024' --entity-name clientA --entity-type clients
```

## Configuración Consumidor Básica
Las principales propiedades para el consumidor son:
- bootstrap.servers : Lista de brokers separados con comas indicando sus puertos.
- key.deserializer : Clase utilizada para deserializar las claves de los mensajes.
- value.deserializer : Clase utilizada para deserializar el payload del mensaje.
- group.id : Se utiliza para indicar que un consumidor pertenece a un grupo de consumidores.

### Offsets Control
El consumidor controla los offsets de los mensajes que va leyendo esta configuración puede ser automática o manual.
Configuración automatica:
- enable.auto.commit : Activación del control automático.
- auto.commit.interval.ms : Interval cada cuando tiempo se actualizan los offsets leídos.
Nota: La activación manual consiste en desactivar el control automático y indicar cuando guardar los offsets desde la aplicación.

Cuando un consumidor se inicia y no tienen ningún commit de los offsets guardado previamente, tiene dos opciones:
- Leer el topic entero.
- Comenzar a leer el topic desde el día.

La propiedad auto.offset.reset nos permite configurar donde queremos comenzar a leer del topic:
- smallest
- largest

### Identificación & Quotas

Para identificar a un cliente consumiendo se puede configurar la siguiente propiedad: client.id Esta configuración es util si queremos definir Quotas.
Si deseamos configurar una cuenta de 1024 bytes por segundo para el cliente con id igual a clientA
```
bin/kafka-configs.sh --zookeeper localhost:2181 --alter --add-config 'consumer_byte_rate=1024' --entity-name clientA --entity-type clients
```

# Operaciones
## Operaciones: Graceful Shutdown

Esta configuración asegura un apagado correcto, sin provocar perdidas de datos y previene errores.
- Reduce considerablemente el tiempo de indisponibilidad de las particiones.
- Añadir esta propiedad al fichero server.properties controlled.shutdown.enable=true
	1. Primero escribe todos los mensajes pendientes en los logs del disco.
	2. Asegura que otro nodo consigue el leader para sus particiones.

## Operaciones: Balancing Leadership

En ocasiones puede ocurrir que los líderes de las particiones no estén balanceados a través de los brokers:
- Al añadir nuevos brokers.
- Al apagar o romperse algún broker.
```
bin/kafka-preferred-replica-election.sh --zookeeper
localhost:2181
```
Este procedimiento se puede automatizar con la siguiente propiedad:
```
auto.leader.rebalance.enable=true
```

## Operaciones: Mirroring data
En ocasiones se pued e d esear copiar datos desd e multiples Kafka clusters a uno en concreto. Importancia para entornos de backup.
```
bin/kafka-mirror-maker.sh --consumer.config consumer.properties --producer.config producer.properties --whitelist testTopic
```
• consumer.config: Fichero d e propied ad es d e los clusters d esd e los que se desea copiar.
• producer.config: Fichero d e propied ad es d el cluster al que se d esea copiar.
• whitelist: es una expresión regular indicando los topics que se desean copiar.
• num.streams: número d e hilos para los consumidores.

## Operaciones: Replay log

Esta herramienta nos permite reproducir los mensajes de un topic en otro topic.
Una utilid ad pued e ser para verificar el correcto funcionamiento d e una nueva versión d e nuestro software de procesamiento en streaming.
```
bin/kafka-replay-log-producer.sh —broker-list localhost:9092 --inputtopic input --outputtopic output --zookeeper localhost:2181 --threads 1
```
Operaciones: Verificación de replicas
- Esta herramienta nos permite valid ad que tod as las replicas de un conjunto de topics tienen los mismos d atos.
bin/kafka-replica-verification.sh --broker-list localhost:9092 --topic-white-list testing-*-topic
- Esta herramienta nos ofrece una opción que es --time que nos permite decir desde que timestamp queremos realizar la verificación.
- Gran utilidad cuando queremos verificar que las replicas contienen los mismos datos después de un periodo de posible perdidas

## Crecimiento del Cluster
Una vez creado un cluster, en ocasiones es necesario su crecimiento. Para ampliar un cluster hay que añadir más brokers con un ID único. Aunque los 
nuevo broker no recibirán d atos para eso hay que asignar particiones/ replicas a los nuevos nodos.
Esta operación se realiza igualmente con el script kafka-reassign-partitions.sh y un fichero en formato JSON.

En esta ocasiones lo que haremos es mover los topics que ya tenemos a los nuevos brokers.
1. Creamos un JSON indicando los topics que deseamos mover.
```
{"topics":[{"topic":"topic1"},{"topic":"topic2"}],"version":1}
```
2. Utilizando el JSON anterior podemos generar un nuevo JSON del mismo formato que usamos para crear nuevas replicas. Pero en este caso no creamos replicas nuevas.
En esta ocasiones lo que haremos es mover los topics que ya tenemos a los nuevos brokers.
1. Creamos un JSON indicando los topics que deseamos mover.
```
{"topics":[{"topic":"topic1"},{"topic":"topic2"}],"version":1}
```
2. Utilizando el JSON anterior podemos generar un nuevo JSON del mismo formato que usamos para crear nuevas replicas. Pero en este caso no creamos replicas nuevas.
bin/kafka-reassign-partitions.sh --zookeeper localhost:2181 --topics-to-move-json-file topics-move.json --broker-list “5,6” --generate
Generando algo como:
```
{"version":1, 
 "partitions":[
	{"topic":"topic1", "partition":0, “replicas”:[5,6]},
	{"topic":"topic1", "partition":1, “replicas”:[5,6]},
	{"topic":"topic2", "partition":0, “replicas”:[5,6]},
	{"topic":"topic2", "partition":1, “replicas”:[5,6]}
]}
```
Una vez generado podemos cambiar y aplicar el JSON, igual que hacíamos con las particiones: 
```
bin/kafka-reassign-partitions.sh --zookeeper localhost:2181 --reassignment-json-file mover-replicas.json --execute
```
