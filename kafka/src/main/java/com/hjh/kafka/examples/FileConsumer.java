package com.hjh.kafka.examples;

import com.hjh.kafka.examples.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class FileConsumer implements Runnable{
    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    public static volatile boolean isStop = false;

    public FileConsumer(String topic) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.42.201:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "FileConsumer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(props);
        this.topic = topic;
    }

    public void run() {
        consumer.subscribe(Collections.singletonList(this.topic));
        while(!isStop){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
                String[] files = record.value().split(",");
                long totalSize = 0;
                for(int i = 0 ; i < files.length ; i++){
                    long size = new File(files[i]).length();
                    totalSize+=size;
                }
                long cnt = files.length;
                System.out.println(record.key()+":"+"总大小:"+totalSize/1024/1024+"m,文件数:"+cnt);
            }
        }
    }
}
