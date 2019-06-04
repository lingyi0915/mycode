package com.hjh.flink.java.source;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

/**
 * @Author: hjh
 * @Create: 2019/3/23
 * @Description: 实现多并行度的source
 * RichParallelSourceFunction提供 open close方法
 * 如何需要读取其他链接，可以在open和close中获取，关闭
 */
public class MyParallerSource2 extends RichParallelSourceFunction<Integer> {

    private static String[] addressPool = {"1.1.1.6","1.1.1.7","1.1.1.8"};
    private static int currAddress = 0;
    private String address;

    //用于停止任务线程
    private boolean isRunning = true;

    private Integer count = 0;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        int pos = currAddress%addressPool.length;
        address = addressPool[pos];
        System.out.println("获得地址:"+address+",currAddress="+currAddress);
        currAddress++;
    }

    @Override
    public void close() throws Exception {
        super.close();
        System.out.println("释放地址:"+address);
    }

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
