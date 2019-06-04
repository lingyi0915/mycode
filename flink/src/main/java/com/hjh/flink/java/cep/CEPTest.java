package com.hjh.flink.java.cep;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import java.util.List;
import java.util.Map;

public class CEPTest {
    public static void main(String[] args) throws Exception {
        //声明本地环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        //使用事件时间
//        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStreamSource<String> dataStream = env.socketTextStream("hadoop", 9000);

//        //定义kafka source使用的时间字段
//        consumer011.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<String>() {
//            @Override
//            public long extractAscendingTimestamp(String s) {
//                long res = -1L;
//                //从字符串中获得时间
//                res = JSONObject.parseObject(s).getLong("time");
//                return res;
//            }
//        });
        //给模式设置一个名字
        Pattern<String,String> cepPattern  = Pattern.<String>begin("begin")
                .where(new SimpleCondition<String>() {
            @Override
            public boolean filter(String s) throws Exception {
                System.out.println("begin :"+s);
                //字符串中包含5的
                return s.contains("5");
            }
        }).next("next").where(new SimpleCondition<String>() {
            @Override
            public boolean filter(String s) throws Exception {
                System.out.println("next :"+s);
                //上一个模式的结果包含2的
                return s.contains("2");
            }
        }).next("end").where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String s) throws Exception {
                        System.out.println("end :"+s);
                        //上一个模式的结果包含2的
                        return s.endsWith("0");
                    }
                }).within(Time.seconds(5));

        PatternStream<String> ps = CEP.pattern(dataStream,cepPattern );

        ps.select(new PatternSelectFunction<String, String>() {
            @Override
            public String select(Map<String, List<String>> pattern) throws Exception {
                List<String> first = pattern.get("begin");
                List<String> second = pattern.get("next");
                List<String> end = pattern.get("end");
                System.out.println("select =========>>>>>>>>>> begin: " + first);
                System.out.println("select =========>>>>>>>>>> next: " + second);
                System.out.println("select =========>>>>>>>>>> end: " + end);
                return "";
            }
        });

//        ps.select((Map<String,List<String>> pattern)->{
//            List<String> first = pattern.get("begin");
//            List<String> second = pattern.get("next");
//            System.out.println("select =========>>>>>>>>>> first: " + first);
//            System.out.println("select =========>>>>>>>>>> second: " + second);
//            return null;
//        });

        //输出到屏幕
        dataStream.print();
        env.execute("test cep pattern");
    }
}
