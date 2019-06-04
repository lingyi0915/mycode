package com.hjh.flink.java.stream;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * 模拟socket window 统计wordcount
 * 统计窗口
 */
public class MySockerWindowWordCount {

    public static void main(String[] args) throws Exception {
        String socketHostName;
        int socketPort;
        String delimiter = "\n";
        try {
            ParameterTool params = ParameterTool.fromArgs(args);
            socketHostName = params.has("hostname") ? params.get("hostname") : "hadoop";
            socketPort = params.has("port")?params.getInt("port"):9000;
        } catch (Exception e) {
            System.err.println("no port set. use default port 9000");
            return;
        }

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> in = env.socketTextStream(socketHostName,socketPort,delimiter);

        DataStream<SocketWindowWordCount.WordWithCount> windowCounts = in
            .flatMap(new FlatMapFunction<String, SocketWindowWordCount.WordWithCount>() {
                @Override
                public void flatMap(String value, Collector<SocketWindowWordCount.WordWithCount> out) throws Exception {
                    for (String word : value.split("\\s")) {
                        out.collect(new SocketWindowWordCount.WordWithCount(word, 1L));
                    }
                }
            })
            .keyBy("word")
            //每隔一秒，统计最近2s的窗口 滑动窗口
            .timeWindow(Time.seconds(2),Time.seconds(1))
//                .sum("count")
            .reduce(
                    new ReduceFunction<SocketWindowWordCount.WordWithCount>() {
                        @Override
                        public SocketWindowWordCount.WordWithCount reduce(SocketWindowWordCount.WordWithCount t1, SocketWindowWordCount.WordWithCount t2) throws Exception {
                            return new SocketWindowWordCount.WordWithCount(t1.word, t1.count + t2.count);
                        }
                    }
            );
        //设置并行度1
        windowCounts.print().setParallelism(1);
        //这句话开始执行
        env.execute("Socket Window WordCount");

    }
}