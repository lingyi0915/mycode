package com.hjh.flink.java.source;

import com.hjh.flink.java.function.*;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.CoGroupedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @Author: hjh
 * @Create: 2019/3/27
 * @Description:
 */
public class StreamingDemoLeftJoin3 {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        List<String> list1 = new ArrayList();
        list1.add("2016-07-28 13:00:01	102	a");
        list1.add("2016-07-28 13:00:01	101	a");
        list1.add("2016-07-28 13:00:02	101	a");
        list1.add("2016-07-28 13:00:03	103	a");
        list1.add("2016-07-28 13:00:04	100	a");
        list1.add("2016-07-28 13:00:05	103	a");
        list1.add("2016-07-28 13:00:06	103	a");
        list1.add("2016-07-28 13:00:14	101	a");

        List<String> list2 = new ArrayList();
        list2.add("2016-07-28 13:00:01	102 b");
        list2.add("2016-07-28 13:00:04	103 b");
        list2.add("2016-07-28 13:00:07	100 b");
        list2.add("2016-07-28 13:00:16	101 b");

        //LocalStreamEnvironment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //处理时间为时间窗口
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        DataStreamSource<String> dataStream1 = env.fromCollection(list1);
        DataStreamSource<String> dataStream2 = env.fromCollection(list2);

        //这里把输入流通过map分发到多个流中
        DataStream<Tuple3<String, String,String>> data1 = dataStream1.map(new DataMapFunction3())
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple3<String, String,String>>(Time.milliseconds(3000)) {
                    @Override
                    public long extractTimestamp(Tuple3<String, String,String> element) {
                        long time = 0L;
                        try {
                            time = sdf.parse(element.f0).getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
//                        System.out.println("stream1 获得水位时间:"+time);
                        return time;
                    }
                });
        DataStream<Tuple3<String, String,String>> data2 = dataStream2.map(new DataMapFunction3())
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple3<String, String,String>>(Time.milliseconds(3000)) {
                    @Override
                    public long extractTimestamp(Tuple3<String, String,String> element) {
                        long time = 0L;
                        try {
                            time = sdf.parse(element.f0).getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        System.out.println("stream2 获得水位时间:"+time);
                        return time;
                    }
                });

        CoGroupedStreams.WithWindow<Tuple3<String, String,String>, Tuple3<String, String,String>, String, TimeWindow> data = data1.coGroup(data2)
                .where(new KeySelector<Tuple3<String, String,String>, String>() {
                   @Override
                   public String getKey(Tuple3<String, String,String> value) throws Exception {
//                       System.out.println("stream1 返回值:"+value.f0);
                       return value.f1;
                   }
               }).equalTo(new KeySelector<Tuple3<String, String,String>, String>() {
                    @Override
                    public String getKey(Tuple3<String, String,String> value) throws Exception {
//                        System.out.println("stream2 返回值:"+value.f0);
                        return value.f1;
                    }
               }).window(TumblingEventTimeWindows.of(Time.seconds(3)));

        DataStream<Tuple5<String,String,String,String,String>> leftjoin = data.apply(new CoGroupFunction<Tuple3<String, String, String>, Tuple3<String, String, String>, Tuple5<String,String,String,String,String>>() {
            @Override
            public void coGroup(Iterable<Tuple3<String, String, String>> first, Iterable<Tuple3<String, String, String>> second, Collector<Tuple5<String, String, String, String, String>> out) throws Exception {
                int size1=0,size2=0;
                if (first instanceof Collection<?>) {
                    size1 = ((Collection<?>)first).size();
                }
                if (second instanceof Collection<?>) {
                    size2 = ((Collection<?>)second).size();
                }
                String name = Thread.currentThread().getName();

                System.out.println(name+"同一批次");
                first.forEach((t1)-> System.out.println(t1));
                second.forEach((t2)-> System.out.println(t2));
                System.out.println(name+"同一批次結束");

                if(size1 == 0){
                    return;
                } else if(size1 != 0 && size2 != 0){
                    first.forEach((t1)->{
                        second.forEach((t2)->{
                            if(t2.f0.equals(t1.f0)){
                                Tuple5<String,String,String,String,String> t5 = new Tuple5<>(t1.f1,t1.f0,t1.f2,t2.f1,t2.f2);
//                        System.out.println("out:"+t3);
                                out.collect(t5);
                            }
                        });
                    });
                } else if(size2 == 0){
                    first.forEach((t1)->{
                        Tuple5<String,String,String,String,String> t5 = new Tuple5<>(t1.f1,t1.f0,t1.f2,null,null);
//                System.out.println("out:"+t3);
                        out.collect(t5);
                    });
                }
            }
        });
        leftjoin.print().name("left join");
        env.execute("join test 3");
    }
}

