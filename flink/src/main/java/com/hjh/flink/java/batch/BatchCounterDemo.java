package com.hjh.flink.java.batch;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: hjh
 * @Create: 2019/3/24
 * @Description: 计算map函数中处理了多少条数据
 */
public class BatchCounterDemo {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        List<String> list = new ArrayList<>();

        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");

        DataSource<String> text = env.fromCollection(list);

        DataSet<String> map =  text.map(new RichMapFunction<String, String>() {

            //内部类不能做static变量，另外，集群中运行任务，static变量无效
            int sum = 0 ;

            //需要一个全局累加器
            IntCounter intCounter = new IntCounter();

            @Override
            public String map(String value) throws Exception {
//                sum++;
//                System.out.println("sum="+sum);
                this.intCounter.add(1);
                return value;
            }

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                //注册累加器
                getRuntimeContext().addAccumulator("num-data-lines",intCounter);
            }
        }).setParallelism(4);
        //没有sink，但是execute，不可以
//        map.print();

        //这有一个sink，ok
        map.writeAsText("D:\\1");

        //返回累加器
        //输出到sink需要execute,print不需要
        JobExecutionResult res = env.execute("counter demo");
        int num = res.getAccumulatorResult("num-data-lines");
        System.out.println("累加器的值:"+num);

    }
}
