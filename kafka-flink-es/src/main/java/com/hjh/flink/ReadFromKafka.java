package com.hjh.flink;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

public class ReadFromKafka {
    public static void main(String[] args) throws Exception {
        //声明本地环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置kafka参数到map中
        Map props= new HashMap();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"hadoop:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "FlinkIntoKafkaDemo");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");
        props.put("session.timeout.ms", "30000");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put("topic", "DemoTopic");

        //设置flink参数
        ParameterTool params = ParameterTool.fromMap(props);
        //创建消费者
        FlinkKafkaConsumer011<String> consumer011 = new FlinkKafkaConsumer011<>(
                params.getRequired("topic"),
                new SimpleStringSchema(),
                params.getProperties());

        DataStream<String> messageStream = env.addSource(consumer011);

        props.put("cluster.name", "");
        //该配置表示批量写入ES时的记录条数
        props.put("bulk.flush.max.actions", "1");

        messageStream.rebalance().map(new MapFunction<String, String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String map(String value) throws Exception {
                return value;
            }
        });

        messageStream.print();
        env.execute("flink kafka demo");

    }
}
