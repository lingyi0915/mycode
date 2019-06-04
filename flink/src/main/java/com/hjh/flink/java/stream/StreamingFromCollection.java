package com.hjh.flink.java.stream;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: hjh
 * @Create: 2019/3/23
 * @Description:
 */
public class StreamingFromCollection {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        DataStreamSource<Integer> collection = env.fromCollection(list);

        DataStream<Integer> source = collection.map(new MapFunction<Integer, Integer>() {
            @Override
            public Integer map(Integer integer) throws Exception {
                return integer+1;
            }
        });

        source.print().setParallelism(1);

        env.execute("from collection");


    }
}
