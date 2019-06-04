package com.hjh.kafka.examples;

import com.hjh.kafka.entry.FileMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.*;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class FileAvroProducer implements Runnable{
    private final KafkaProducer<String,FileMessage> fileProducer;
    private final String topic;
    private final boolean isAsync;
    private final String filePath;

    public FileAvroProducer(String filePath , String topic,Boolean isAsync){

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.42.201:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "FileProducer");
        props.put("acks", "all");//所有partiiton的副本都响应了才认为消息提交成功，即"committed"
        props.put("retries", 0);//retries = MAX 无限重试，直到你意识到出现了问题:)
        props.put("batch.size", 16384);//producer将试图批处理消息记录，以减少请求次数.默认的批量处理消息字节数
        //batch.size当批量的数据大小达到设定值后，就会立即发送，不顾下面的linger.ms
        /**
         * 延迟1ms发送，这项设置将通过增加小的延迟来完成--即，
         * 不是立即发送一条记录，producer将会等待给定的延迟时间以允许其他消息记录发送，
         * 这些消息记录可以批量处理
         */
        props.put("linger.ms", 10);
        props.put("buffer.memory", 33554432);//producer可以用来缓存数据的内存大小。
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "com.hjh.kafka.serializer.CustomerFileSerializer");
        fileProducer = new KafkaProducer<String, FileMessage>(props);
        this.topic = topic;
        this.isAsync = isAsync;
        this.filePath = filePath;
    }

    public void run(){
        File rootDir = new File(filePath);
        if(rootDir.isDirectory()){

            File[] fileList = rootDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith("log");
                }
            });
            Map<String , FileMessage> map = new HashMap<>();

            for(int i = 0 ; i < fileList.length ; i++){
                String logPath = fileList[i].getAbsolutePath();
                String logType = fileList[i].getName().split("\\.")[0];
                if(map.containsKey(logType)){
                    map.get(logType).add(logPath);
                }else{
                    FileMessage message = new FileMessage(logType);
                    message.add(logPath);
                    map.put(logType,message);
                }
            }

            for(String logType:map.keySet()){
                FileMessage message = map.get(logType);
                ProducerRecord<String,FileMessage> record = new ProducerRecord<>(topic,logType,message);
                if (isAsync) {
                    long startTime = System.currentTimeMillis();
                    fileProducer.send(record, new DemoAvroCallback(startTime, logType, message));
                    System.out.println("Async sent message: (" + logType + ", " + message + ")");
                }else{
                    try {
                        RecordMetadata result =  fileProducer.send(record).get();
                        System.out.println("partition:"+result.partition()
                                +",offset:"+result.offset()
                                +",serializedKeySize:"+result.serializedKeySize()
                                +",serializedValueSize:"+result.serializedValueSize()
                                +",timestamp:"+result.timestamp()
                                +",topic:"+result.topic());
                        System.out.println("Sent message: (" + logType + ", " + message + ")");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else{
            System.out.println("必须输入目录");
            return ;
        }
    }
}

class DemoAvroCallback implements Callback {
    private final long startTime;
    private final String key;
    private final FileMessage message;

         public DemoAvroCallback(long startTime, String key, FileMessage message) {
             this.startTime = startTime;
             this.key = key;
             this.message = message;
         }
          public void onCompletion(RecordMetadata metadata, Exception exception) {
              long elapsedTime = System.currentTimeMillis() - startTime;
              if (metadata != null) {
                  System.out.println("message(" + key + ", " + message + ") sent to partition(" + metadata.partition() +
                            "), " +
                            "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
              } else {
                  exception.printStackTrace();
              }
    }
}