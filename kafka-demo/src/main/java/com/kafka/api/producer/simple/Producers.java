package com.kafka.api.producer.simple;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Productor de mensajes:
 * 
 * 
 *  En el servidor:
 * 	cambio las particiones -> bin/kafka-topics.sh --zookeeper localhost:2181 --partitions 10 --alter --topic test-topic
 *  visualizo los mensajes -> bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic test-topic
 *  
 *   
 * @author Admin
 *
 */
public class Producers {
    public static String brokerList = "vb-dev-1:9092,vb-dev-2:9092,vb-dev-3:9092";
    public static String topic = "test-topic";

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.kafka.api.producer.SimplePartitioner");

        Producer<String, String> producer = new KafkaProducer<>(props);

        for (int id = 0; id < 5000; id++) {
            String key = String.format("key[%d]", id);
            String message = String.format("message[%d]", id);
            System.out.println("Sending message with: " + key);
            producer.send(new ProducerRecord<>(topic, key, message));
            Thread.sleep(1000);
        }

        producer.flush();
        producer.close();
    }
}
