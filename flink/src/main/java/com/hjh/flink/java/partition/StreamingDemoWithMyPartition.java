package com.hjh.flink.java.partition;

import com.hjh.flink.java.source.MyNoParallerSouce;
import com.hjh.flink.java.source.MyParallerSource1;
import com.hjh.flink.java.source.MyParallerSource2;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author: hjh
 * @Create: 2019/3/24
 * @Description: 根据数字奇偶分区
 */
public class StreamingDemoWithMyPartition {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Integer> text = env.addSource(new MyParallerSource1());

        DataStream<Tuple2<Integer,Integer>> tuple = text.map(new MapFunction<Integer, Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> map(Integer value) throws Exception {
                return new Tuple2<>(-1,value);
            }
        });

        // 用字段位置分区
        DataStream<Tuple2<Integer,Integer>> partitionData = tuple.partitionCustom(new MyPartition(),1);

        //用字段名字分区
//        text.partitionCustom(new MyPartition(),"f1");

        DataStream<Integer> result =  partitionData.map(new MapFunction<Tuple2<Integer, Integer>, Integer>() {
            @Override
            public Integer map(Tuple2<Integer, Integer> value) throws Exception {
                System.out.println("当前线程ID："+Thread.currentThread().getId());
                return value.getField(1);
            }
        });

        result.print().setParallelism(1);

        env.execute();

    }
}
