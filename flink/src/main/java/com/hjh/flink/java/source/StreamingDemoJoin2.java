package com.hjh.flink.java.source;

import com.hjh.flink.java.function.StreamJoinFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.functions.NullByteKeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author: hjh
 * @Create: 2019/3/27
 * @Description:
 */
public class StreamingDemoJoin2 {
    public static void main(String[] args) throws Exception {

        List<String> list1 = new ArrayList();
        list1.add("1");
        list1.add("2");
        list1.add("4");

        List<String> list2 = new ArrayList();
        list2.add("1");
        list2.add("2");
        list2.add("3");

        //LocalStreamEnvironment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        SingleOutputStreamOperator<String> dataStream1 = env.fromCollection(list1).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<String>(Time.milliseconds(5000)) {
            @Override
            public long extractTimestamp(String element) {
                long time = System.currentTimeMillis();
                System.out.println("stream1 获得水位时间:"+time);
                return time;
            }
        });
        SingleOutputStreamOperator<String> dataStream2 = env.fromCollection(list2).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<String>(Time.milliseconds(5000)) {
            @Override
            public long extractTimestamp(String element) {
                long time = System.currentTimeMillis();
                System.out.println("stream2 获得水位时间:"+time);
                return time;
            }
        });

        //只有两个都匹配的才会join
        dataStream1.join(dataStream2)
                .where(new NullByteKeySelector<>())
                .equalTo(new NullByteKeySelector<>())
                .window(TumblingEventTimeWindows.of(Time.seconds(3)))
                .apply(new JoinFunction<String, String, String>(){
                    @Override
                    public String join(String first, String second) {
                        System.out.println("进入apply");
                        return first + "," + second;
                    }
                }).print().name("stream join");

        env.execute("join test");

    }
}

