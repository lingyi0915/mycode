package com.hjh.flink.java.batch;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

/**
 * @Author: hjh
 * @Create: 2019/3/22
 * @Description:
 */
public class BatchWordCount {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        String inputPath = "D:\\workspace\\测试数据\\pcac.access_2017-03-06.log";

        String outPath = "D:\\2";

        //获取文件内容
        DataSource<String> text = env.readTextFile(inputPath);

        AggregateOperator<Tuple2<String, Long>> count = text.flatMap(new Tokenizer())
                .groupBy(0)
                .sum(1);

        count.writeAsCsv(outPath,"\n"," ").setParallelism(1);

        env.execute("batch demo");

    }

    public static class Tokenizer implements FlatMapFunction<String, Tuple2<String,Long>>{
        @Override
        public void flatMap(String s, Collector<Tuple2<String, Long>> collector) throws Exception {
            String[] tokenizer = s.toLowerCase().split("\\W+");
            for(String token : tokenizer){
                if(token.length()>0){
                    collector.collect(new Tuple2<>(token,1L));
                }
            }
        }
    }
}
