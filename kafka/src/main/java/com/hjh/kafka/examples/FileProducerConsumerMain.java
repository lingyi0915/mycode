package com.hjh.kafka.examples;

public class FileProducerConsumerMain {
    public static void main(String[] args) {
        String filePath = "D:\\workspace\\测试数据";
        if(args.length == 1){
            filePath = args[0];
        }

        Thread producer = new Thread(new FileProducer(filePath,"topic1",false));
        Thread consumer = new Thread(new FileConsumer("topic1"));

        consumer.start();
        producer.start();

        try {
            Thread.sleep(5000);
            FileConsumer.isStop = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
