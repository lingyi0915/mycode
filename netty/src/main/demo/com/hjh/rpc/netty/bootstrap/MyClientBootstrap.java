package com.hjh.rpc.netty.bootstrap;

import com.hjh.rpc.netty.consumer.RpcConsumer;
import com.hjh.rpc.netty.service.Service;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description:
 */
public class MyClientBootstrap {
    public static final String providerName = "HelloService#hello#";

    public static void main(String[] args) throws InterruptedException {

        //创建于一个消费者
        RpcConsumer consumer = new RpcConsumer();
        // 创建一个代理对象
        Service service = (Service) consumer
                .createProxy(Service.class, providerName);
        System.out.println(service.getClass().getName());
        while(true) {
            service.start("niu jun ?"+",等待1000ms");
            Thread.sleep(1000);
//            System.out.println("线程等待结束");
        }
    }
}
