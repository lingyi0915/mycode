package com.hjh.flink.java.source;

import org.apache.flink.streaming.api.functions.source.SourceFunction;


/**
 * @Author: hjh
 * @Create: 2019/3/23
 * @Description: 实现并行度为1的source 不允许多并行度
 */
public class MyNoParallerSouce implements SourceFunction<Integer> {

    //用于停止任务线程
    private boolean isRunning = true;

    private Integer count = 0;

    /**
     * 主要的方法
     * 启动一个source
     * 大部分情况下，需要在这个方法中循环产生数据，用于获取数据，形成流
     * @param ctx 系统传进来的上下文信息
     * @throws Exception
     */
    @Override
    public void run(SourceContext ctx) throws Exception {
        while(isRunning){
            ctx.collect(count++);
            Thread.sleep(1000);
        }
    }


    /**
     * 任务停止的时候，调用这个方法
     */
    @Override
    public void cancel() {
        //这句话打印不出来 就终止了
        System.out.println("停止获取数据");
        isRunning = false;
    }
}
