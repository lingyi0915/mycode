package com.hjh.flink.java.source;

import com.hjh.flink.java.function.*;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.functions.NullByteKeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.CoGroupedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author: hjh
 * @Create: 2019/3/27
 * @Description:
 */
public class StreamingDemoLeftJoin {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//        String socketHostName;
//        int socketPort1;
//        int socketPort2;
//
//        try{
//            ParameterTool params = ParameterTool.fromArgs(args);
//            socketHostName = params.has("hostname") ? params.get("hostname") : "hadoop";
//            socketPort1 = params.getInt("port1");
//            socketPort2 = params.getInt("port2");
//        } catch (Exception e){
//            System.err.println("No port specified. Please run 'SocketWindowWordCount " +
//                    "--hostname <hostname> --port1 <port1> --port2 <port2>', where hostname (localhost by default) " +
//                    "and port is the address of the text server");
//            return;
//        }

        List<String> list1 = new ArrayList();
        list1.add("2016-07-28 13:00:01	a10.2");
        list1.add("2016-07-28 13:00:02	a10.1");
        list1.add("2016-07-28 13:00:03	a10.1");
        list1.add("2016-07-28 13:00:04	a10.0");
        list1.add("2016-07-28 13:00:05	a10.0");
        list1.add("2016-07-28 13:00:05	a10.0");
        list1.add("2016-07-28 13:00:14	a10.1");
        list1.add("2016-07-28 13:00:20	a10.2");

        List<String> list2 = new ArrayList();
        list2.add("2016-07-28 13:00:01	b10.2");
        list2.add("2016-07-28 13:00:04	b10.1");
        list2.add("2016-07-28 13:00:07	b10.0");
        list2.add("2016-07-28 13:00:16	b10.1");

        //LocalStreamEnvironment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //处理时间为时间窗口
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);
        //socketTextStream 继承SourceFunction 不能并行  默认 setParallelism(1)
//        DataStreamSource<String> dataStream1 = env.socketTextStream(socketHostName,socketPort1);
//        DataStreamSource<String> dataStream2 = env.socketTextStream(socketHostName,socketPort2);

        DataStreamSource<String> dataStream1 = env.fromCollection(list1);
        DataStreamSource<String> dataStream2 = env.fromCollection(list2);

        //这里把输入流通过map分发到多个流中
        DataStream<Tuple2<String,String>> data1 = dataStream1.map(new DataMapFunction())
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple2<String, String>>(Time.milliseconds(3000)) {
                    @Override
                    public long extractTimestamp(Tuple2<String, String> element) {
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
//                .assignTimestampsAndWatermarks(new DataTimeAssignFunction());
        DataStream<Tuple2<String,String>> data2 = dataStream2.map(new DataMapFunction())
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple2<String, String>>(Time.milliseconds(5000)) {
                    @Override
                    public long extractTimestamp(Tuple2<String, String> element) {
                        long time = 0L;
                        try {
                            time = sdf.parse(element.f0).getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return time;
                    }
                });
//                .assignTimestampsAndWatermarks(new DataTimeAssignFunction());

        //只有两个都匹配的才会join
//        data1.join(data2)
//                .where(new NullByteKeySelector<>())
//                .equalTo(new NullByteKeySelector<>())
//                .window(TumblingEventTimeWindows.of(Time.seconds(3)))
//                .apply(new JoinFunction<Tuple2<String, String>, Tuple2<String, String>, Tuple3<String, String, String>>() {
//                            @Override
//                            public Tuple3<String, String, String> join(Tuple2<String, String> first, Tuple2<String, String> second) {
//                                return new Tuple3<>(first.f0, first.f1, second.f1);
//                            }
//                        }).print();


        CoGroupedStreams.WithWindow<Tuple2<String, String>, Tuple2<String, String>, String, TimeWindow> data = data1.coGroup(data2)
                .where(new KeySelector<Tuple2<String, String>, String>() {
                   @Override
                   public String getKey(Tuple2<String, String> value) throws Exception {
//                       System.out.println("stream1 返回值:"+value.f0);
                       return value.f0;
                   }
               }).equalTo(new KeySelector<Tuple2<String, String>, String>() {
                    @Override
                    public String getKey(Tuple2<String, String> value) throws Exception {
//                        System.out.println("stream2 返回值:"+value.f0);
                        return value.f0;
                    }
               }).window(TumblingEventTimeWindows.of(Time.seconds(3)));

//        System.out.println(data.getClass().getName());

        DataStream<Tuple3<String,String,String>> leftjoin = data.apply(new LeftJoinCoGroupFuntion());
        DataStream<Tuple3<String,String,String>> rightjoin = data.apply(new RightJoinCoGroupFuntion());
        DataStream<Tuple3<String,String,String>> fulljoin = data.apply(new FullJoinCoGroupFuntion());

        leftjoin.print().name("left join");
//        rightjoin.print().name("right join");
//        fulljoin.print().name("full join");

        env.execute("join test");

    }
}

