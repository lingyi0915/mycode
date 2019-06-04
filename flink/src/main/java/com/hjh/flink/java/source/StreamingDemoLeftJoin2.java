package com.hjh.flink.java.source;

import com.hjh.flink.java.function.*;
import org.apache.flink.api.common.ExecutionConfig;
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
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
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
/**
 *   {@link LocalStreamEnvironment}
 *
 */
public class StreamingDemoLeftJoin2 {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String socketHostName = "hadoop";
        int socketPort1 = 9000;
        int socketPort2 = 9001;


        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.getConfig().setAutoWatermarkInterval(2000);
        //处理时间为时间窗口
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//        env.setParallelism(1);

        DataStreamSource<String> dataStream1 = env.socketTextStream(socketHostName,socketPort1);
        DataStreamSource<String> dataStream2 = env.socketTextStream(socketHostName,socketPort2);

        //这里把输入流通过map分发到多个流中
        DataStream<Tuple2<String,String>> data1 = dataStream1.map(new DataMapFunction())
                .assignTimestampsAndWatermarks(new DataTimeAssignFunction());
//                .assignTimestampsAndWatermarks(new DataTimeAssignPeriodicFunction());
        DataStream<Tuple2<String,String>> data2 = dataStream2.map(new DataMapFunction())
                .assignTimestampsAndWatermarks(new DataTimeAssignFunction());
//                .assignTimestampsAndWatermarks(new DataTimeAssignPeriodicFunction());

        CoGroupedStreams.WithWindow<Tuple2<String, String>, Tuple2<String, String>, String, TimeWindow> data = data1.coGroup(data2)
                .where(new GetFirstKey())
                .equalTo(new GetFirstKey())
                .window(TumblingEventTimeWindows.of(Time.seconds(4)));

        DataStream<Tuple3<String,String,String>> innerjoin = data.apply(new InnerJoinCoGroupFuntion());
        DataStream<Tuple3<String,String,String>> leftjoin = data.apply(new LeftJoinCoGroupFuntion());
        DataStream<Tuple3<String,String,String>> rightjoin = data.apply(new RightJoinCoGroupFuntion());
        DataStream<Tuple3<String,String,String>> fulljoin = data.apply(new FullJoinCoGroupFuntion());

        innerjoin.print().name("inner join");
//        leftjoin.print().name("left join");
//        rightjoin.print().name("right join");
//        fulljoin.print().name("full join");

        env.execute("join test");

    }
}

