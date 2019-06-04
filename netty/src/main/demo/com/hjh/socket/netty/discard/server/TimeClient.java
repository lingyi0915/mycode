package com.hjh.socket.netty.discard.server;

import com.hjh.socket.netty.discard.handle.TimeClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author: hjh
 * @Create: 2019/4/10
 * @Description:
 * 与DISCARD和ECHO服务器不同，我们需要TIME协议的客户端，
 * 因为人类无法将32位二进制数据转换为日历上的日期。
 * 在本节中，我们将讨论如何确保服务器正常工作并学习如何使用Netty编写客户端。
 *
 * Netty中服务器和客户端之间最大和唯一的区别是使用了不同的Bootstrap和Channel实现。 请看下面的代码：
 */
public class TimeClient {
    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
