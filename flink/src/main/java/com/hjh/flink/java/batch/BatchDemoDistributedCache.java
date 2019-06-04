package com.hjh.flink.java.batch;

import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.configuration.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: hjh
 * @Create: 2019/3/24
 * @Description: 分布式缓存
 */
public class BatchDemoDistributedCache {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //注册一个文件 最好是hdfs s3文件 本地运行 用本地文件做测试
        env.registerCachedFile("D:\\1\\4", "hdfsFile");

        DataSource<String> text = env.fromElements("1","2","3","4");



        DataSet<String> map =  text.map(new RichMapFunction<String, String>() {

            //内部类不能做static变量，另外，集群中运行任务，static变量无效
            int sum = 0 ;

            //需要一个全局累加器
            IntCounter intCounter = new IntCounter();
            List<String> list;
            @Override
            public String map(String value) throws Exception {
//                sum++;
//                System.out.println("sum="+sum);
                this.intCounter.add(1);
                return list.get(0)+","+value;
            }

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                //注册累加器
                getRuntimeContext().addAccumulator("num-data-lines",intCounter);
                //获得分布式缓存中的文件
                File file = getRuntimeContext().getDistributedCache().getFile("hdfsFile");
                list = FileUtils.readLines(file);
                for(String str :list){
                    System.out.println("line:"+str);
                }
            }
        }).setParallelism(3);
        //没有sink，但是execute，不可以
//        map.print();

        //这有一个sink，ok
        map.writeAsText("D:\\3");

        //返回累加器
        //输出到sink需要execute,print不需要
        JobExecutionResult res = env.execute("counter demo");
        int num = res.getAccumulatorResult("num-data-lines");
        System.out.println("累加器的值:"+num);

    }
}
