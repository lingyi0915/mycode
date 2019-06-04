package com.hjh.socket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description: netty的好处
 *
 * 使用JDK自带的NIO需要了解太多的概念，编程复杂，一不小心bug横飞
 * Netty底层IO模型随意切换，而这一切只需要做微小的改动，改改参数，Netty可以直接从NIO模型变身为IO模型
 * Netty自带的拆包解包，异常检测等机制让你从NIO的繁重细节中脱离出来，让你只需要关心业务逻辑
 * Netty解决了JDK的很多包括空轮询在内的bug
 * Netty底层对线程，selector做了很多细小的优化，精心设计的reactor线程模型做到非常高效的并发处理
 * 自带各种协议栈让你处理任何一种通用协议都几乎不用亲自动手
 * Netty社区活跃，遇到问题随时邮件列表或者issue
 * Netty已经历各大rpc框架，消息中间件，分布式通信中间件线上的广泛验证，健壮性无比强大
 *
 * 作者：简书闪电侠
 * 链接：https://www.jianshu.com/p/a4e03835921a
 * 来源：简书
 * 简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
 */
public class NettyServer {

    public static void startServer(String hostname,int port){
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        //.boos对应，IOServer.java中的接受新连接线程，主要负责创建新连接
        NioEventLoopGroup boos = new NioEventLoopGroup();
        //2.worker对应 IOClient.java中的负责读取数据的线程，主要用于读取数据以及业务逻辑处理
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap
                .group(boos, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                .bind(port);
    }

    public static void main(String[] args) {
        startServer("localhost",8088);
    }

}
