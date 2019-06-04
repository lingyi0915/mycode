package com.hjh.flink.java.source;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * @Author: hjh
 * @Create: 2019/3/23
 * @Description: union demo
 */
public class StreamingDemoUnion {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //这里设置的并行度是任务运行并行度 当前source不支持并行
        DataStreamSource<Integer> msg1 = env.addSource(new MyNoParallerSouce()).setParallelism(1);
        DataStreamSource<Integer> msg2 = env.addSource(new MyNoParallerSouce()).setParallelism(1);
        DataStreamSource<Integer> msg3 = env.addSource(new MyNoParallerSouce()).setParallelism(1);

        DataStream<Integer> msg = msg1.union(msg2).union(msg3);

        /** MapFunction 两个泛型，接收参数类型， 返回参数类型
         */
        DataStream<Long> res = msg.map(new MapFunction<Integer, Long>() {
            @Override
            public Long map(Integer aLong) throws Exception {
                System.out.println("接收数据:"+aLong);
                return new Long(aLong);
            }
        }).filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long aLong) throws Exception {
                return aLong%2==0;
            }
        }).timeWindowAll(Time.seconds(2)).sum(0);

        //这里设置的是 几个线程打印结果
        res.print().setParallelism(1);

        env.execute();
    }
}
