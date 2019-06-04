package com.hjh.rpc.netty.consumer;

import com.hjh.rpc.netty.handle.HelloClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description:
 */
public class RpcConsumer {

    //创建一个和cpu核数相同的fixed线程池
    private static ExecutorService executor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    //创建client
    private static HelloClientHandler client;

    /**
     * 创建一个代理对象
     */
    public Object createProxy(final Class<?> serviceClass,
                              final String providerName) {
        /**
         * 类加载器
         * class对象数组，需要实现的接口
         * 调用处理器
         */
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{serviceClass}, (proxy, method, args) -> {
//                    System.out.println("调用proxy的"+method.getName()+"方法");
                    if (client == null) {
                        System.out.println("client为null,初始化");
                        initClient();
                    }
                    // 设置参数
                    client.setPara(providerName + args[0]);
                    return executor.submit(client).get();
                });
    }

    /**
     * 初始化客户端
     */
    private static void initClient() {
        client = new HelloClientHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new StringEncoder());
                        p.addLast(client);
                    }
                });
        try {
            //连接server
            System.out.println("异步连接localhost:8088");
            b.connect("localhost", 8088).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}