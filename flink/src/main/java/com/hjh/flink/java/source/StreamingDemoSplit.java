package com.hjh.flink.java.source;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: hjh
 * @Create: 2019/3/23
 * @Description: split算子demo
 * 将一个数据流拆分为多个数据流,(流里包含各种需要不同处理的数据)
 */
public class StreamingDemoSplit {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //这里设置的并行度是任务运行并行度 当前source不支持并行
        DataStreamSource<Integer> msg = env.addSource(new MyNoParallerSouce()).setParallelism(1);

        //对流切分
        SplitStream<Integer> splitStream =  msg.split(new OutputSelector<Integer>() {
            @Override
            public Iterable<String> select(Integer value) {
                List<String> out = new ArrayList<>();
                if(value%2==0){
                    out.add("even");
                }else{
                    out.add("odd");
                }
                return out;
            }
        });

        //获得切分后的流
        DataStream<Integer> even = splitStream.select("even");
        DataStream<Integer> odd = splitStream.select("odd");

        DataStream<Integer> more = splitStream.select("odd","even");

        /** MapFunction 两个泛型，接收参数类型， 返回参数类型
         */
        DataStream<Long> res1 = even.map(new MapFunction<Integer, Long>() {
            @Override
            public Long map(Integer aLong) throws Exception {
                System.out.println("even接收数据:"+aLong);
                return new Long(aLong);
            }
        }).timeWindowAll(Time.seconds(2)).sum(0);

        DataStream<Long> res2 = odd.map(new MapFunction<Integer, Long>() {
            @Override
            public Long map(Integer aLong) throws Exception {
                System.out.println("odd接收数据:"+aLong);
                return new Long(aLong);
            }
        }).timeWindowAll(Time.seconds(2)).sum(0);

        //这里设置的是 几个线程打印结果
        res1.print().setParallelism(1);
        res2.print().setParallelism(1);

        env.execute();
    }
}
