package com.hjh.flink.java.source;

import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

/**
 * @Author: hjh
 * @Create: 2019/3/23
 * @Description: 实现ParallelSourceFunction接口实现自定义Source
 */
public class MyParallerSource1 implements ParallelSourceFunction<Integer> {

    //用于停止任务线程
    private boolean isRunning = true;

    private Integer count = 0;

    @Override
    public void run(SourceContext<Integer> ctx) throws Exception {
        while(isRunning){
            ctx.collect(count++);
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        //这句话打印不出来 就终止了
        System.out.println("停止获取数据");
        isRunning = false;
    }
}
