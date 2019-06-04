package com.hjh.flink.java.source;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * @Author: hjh
 * @Create: 2019/3/23
 * @Description: union demo
 */
public class StreamingDemoConnect {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //这里设置的并行度是任务运行并行度 当前source不支持并行
        DataStreamSource<Integer> msg1 = env.addSource(new MyNoParallerSouce()).setParallelism(1);
        DataStreamSource<Integer> msg2 = env.addSource(new MyNoParallerSouce()).setParallelism(1);

        SingleOutputStreamOperator<String> msg2Str= msg2.map(new MapFunction<Integer, String>() {
            @Override
            public String map(Integer integer) throws Exception {
                return integer.toString();
            }
        });

        ConnectedStreams<Integer,String> msg = msg1.connect(msg2Str);

        /** MapFunction 两个泛型，接收参数类型， 返回参数类型
         */
        DataStream<Object> res = msg.map(new CoMapFunction<Integer, String, Object>() {
            @Override
            public Object map1(Integer value) throws Exception {
                return value;
            }

            @Override
            public Object map2(String value) throws Exception {
                return "字符串:"+value;
            }
        });

        //这里设置的是 几个线程打印结果
        res.print().setParallelism(1);

        env.execute();
    }
}
