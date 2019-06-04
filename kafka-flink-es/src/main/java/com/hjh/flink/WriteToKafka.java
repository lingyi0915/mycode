package com.hjh.flink;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.HashMap;
import java.util.Map;

public class WriteToKafka {
    public static void main(String[] args) throws Exception {
        //准备配置信息
        Map props = new HashMap();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"hadoop:9092");
        props.put("topic","DemoTopic");
//        props.put(ProducerConfig.CLIENT_ID_CONFIG, "FlinkIntoKafkaDemo");
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //创建params
        ParameterTool params = ParameterTool.fromMap(props);
        // 本地环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //环境配置 flink的
        env.getConfig().disableSysoutLogging();
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));
        env.enableCheckpointing(5000); // create a checkpoint every 5 seconds
        env.getConfig().setGlobalJobParameters(params); // make parameters available in the web interface
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //设置消息模式
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        //增加一个输入流 由一个类生成string
        DataStream<String> messageStream = env.addSource(new SimpleStringGenerator());

        //设置输出在kafka
        messageStream.addSink(new FlinkKafkaProducer011<String>(
                params.getRequired(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG),
                params.getRequired("topic"),
                new SimpleStringSchema()));

        //序列化设置，中间结果会转存为本地，然后在下一个任务中反序列化出来
        messageStream.rebalance().map(new MapFunction<String, String>() {
           //序列化设置
           private static final long serialVersionUID = 1L;

            @Override
            public String map(String s) throws Exception {
                return s;
            }
        });

        messageStream.print();

        env.execute("flink kafka demo");

    }

    public static class SimpleStringGenerator implements SourceFunction<String> {
        //序列化设置
        private static final long serialVersionUID = 1L;
        private static long cnt = 0;
        boolean running = true;

        @Override
        public void run(SourceContext<String> ctx) throws Exception {
            while(running&&cnt<=2000) {
                ctx.collect(prouderJson());
            }
        }

        @Override
        public void cancel() {
            running = false;
        }

        public String prouderJson(){
            StringBuffer sb = new StringBuffer();
            long time = System.currentTimeMillis();
            cnt++;
            sb.append("{"+"\"time\":"+time+",\"info\":"+cnt+"}");
            return sb.toString();
        }
    }
}
