package com.hjh.rpc.netty.bootstrap;

import com.hjh.rpc.netty.handle.HelloServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description:
 */
public class MyServerBootstrap {

    public static void startService(String hostname,int port){
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new StringDecoder());
                        p.addLast(new HelloServerHandler());
                    }
                });
        try {
            //绑定端口，长链接
            System.out.println("监听localhost:8088");
            bootstrap.bind(hostname, port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        startService("localhost", 8088);
    }
}
