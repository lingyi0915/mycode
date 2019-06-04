package com.hjh.flink.java.source;

import com.hjh.flink.java.function.*;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.CoGroupedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author: hjh
 * @Create: 2019/3/27
 * @Description:
 */
public class StreamingDemoJoin {
    public static void main(String[] args) throws Exception {

//        String socketHostName;
////        int socketPort1;
////        int socketPort2;
////
////        try{
////            ParameterTool params = ParameterTool.fromArgs(args);
////            socketHostName = params.has("hostname") ? params.get("hostname") : "hadoop";
////            socketPort1 = params.getInt("port1");
////            socketPort2 = params.getInt("port2");
////        } catch (Exception e){
////            System.err.println("No port specified. Please run 'SocketWindowWordCount " +
////                    "--hostname <hostname> --port1 <port1> --port2 <port2>', where hostname (localhost by default) " +
////                    "and port is the address of the text server");
////            return;
////        }

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
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        //socketTextStream 继承SourceFunction 不能并行  默认 setParallelism(1)
//        DataStreamSource<String> dataStream1 = env.socketTextStream(socketHostName,socketPort1);
        DataStreamSource<String> dataStream1 = env.fromCollection(list1);
//        DataStreamSource<String> dataStream2 = env.socketTextStream(socketHostName,socketPort2);
        DataStreamSource<String> dataStream2 = env.fromCollection(list2);

        //这里把输入流通过map分发到多个流中
//        DataStream<Tuple2<String,String>> data1 = dataStream1.map(new DataMapFunction());
//        DataStream<Tuple2<String,String>> data2 = dataStream2.map(new DataMapFunction());

        //只有两个都匹配的才会join
        dataStream1.join(dataStream2)
                .where(new KeySelector<String, String>() {
                   @Override
                   public String getKey(String value) throws Exception {
                       String[] spt = value.split("\t");
                       return spt[0];
                   }
               }).equalTo(new KeySelector<String, String>() {
                    @Override
                    public String getKey(String value) throws Exception {
                        String[] spt = value.split("\t");
                        return spt[0];
                    }
               }).window(TumblingEventTimeWindows.of(Time.seconds(3)))
                .apply(new StreamJoinFunction()).print().name("stream join");

        env.execute("join test");

    }
}

