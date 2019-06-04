package com.hjh.flink;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaToES {
//    public static RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost,Integer.parseInt(esPort))));

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

        Map config= new HashMap();
        config.put("cluster.name", "estest");
        //该配置表示批量写入ES时的记录条数
//        config.put("bulk.flush.max.actions", "1");

        List<InetSocketAddress> transportAddresses = new ArrayList<>();

        HttpHost httpHost = new HttpHost("hadoop", 9200, "http");
        List<HttpHost> httpHosts = new ArrayList<>();
        httpHosts.add(httpHost);
        ElasticsearchSink.Builder<Tuple2<String,String>> esSink = new ElasticsearchSink.Builder<Tuple2<String,String>>(httpHosts,
                new ElasticsearchSinkFunction<Tuple2<String,String>>() {
                    public IndexRequest createIndexRequest(String element) {
                        Map<String, String> json = new HashMap<>();
                        //将需要写入ES的字段依次添加到Map当中
                        json.put("data", element);

                        return Requests.indexRequest()
                                .index("kafka-flink-index")
                                .type("doc")
                                .source(json);
                    }

                    @Override
                    public void process(Tuple2<String,String> tuple2, RuntimeContext ctx, RequestIndexer indexer) {
                        indexer.add(createIndexRequest(tuple2.f1));
                    }
                });

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
