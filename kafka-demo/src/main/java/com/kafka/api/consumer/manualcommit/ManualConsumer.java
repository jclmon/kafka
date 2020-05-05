package com.kafka.api.consumer.manualcommit;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * Commit manual ENABLE_AUTO_COMMIT_CONFIG=false
 * 
 * @author Admin
 *
 */
public class ManualConsumer {
    public static String KAFKA_HOST = "vb-dev-1:9092,vb-dev-2:9092,vb-dev-3:9092";
    public static String TOPIC = "test-topic-json";
    private static final AtomicBoolean closed = new AtomicBoolean(false);

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("Shutting down");
                closed.set(true);
            }
        });

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_HOST);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "manual-test-consummer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));

        while (!closed.get()) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("partition = %2d   offset = %5d   key = %7s   value = %12s\n",
                        record.partition(), record.offset(), record.key(), record.value());
                Thread.sleep(1500);
            }

            //cada vez que se hace esto asiente los mensajes, los offset que se han leído se indica que ya estan procesados
            //sin esto leera desde el último commit
            consumer.commitSync();
        }

        consumer.close();
    }
}
