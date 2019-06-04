package com.hjh.socket.netty.discard.server;

import com.hjh.socket.netty.discard.handle.DiscardServerHandler;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Author: hjh
 * @Create: 2019/4/10
 * @Description:
 */
public class DiscardServer {
    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        /**
         * NioEventLoopGroup是一个处理I / O操作的多线程事件循环。
         * Netty为不同类型的传输提供各种EventLoopGroup实现。
         * 我们在此示例中实现了服务器端应用程序，因此将使用两个NioEventLoopGroup。
         * 第一个，通常称为“老板”，接受传入连接。
         * 第二个，通常称为“工人”，一旦老板接受连接并将接受的连接注册到工作人员，就处理被接受连接的流量。
         * 使用了多少个线程以及它们如何映射到创建的Channels取决于EventLoopGroup实现，甚至可以通过构造函数进行配置。
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            /**
             * ServerBootstrap是一个设置服务器的帮助程序类。
             * 您可以直接使用Channel设置服务器。
             * 但请注意，这是一个繁琐的过程，在大多数情况下您不需要这样做。
             */
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    /**
                     * 在这里，我们指定使用NioServerSocketChannel类，该类用于实例化新Channel以接受传入连接。
                     */
                    .channel(NioServerSocketChannel.class) // (3)
                    /**
                     * 此处指定的处理程序将始终由新接受的Channel评估。
                     * ChannelInitializer是一个特殊的处理程序，旨在帮助用户配置新的Channel。
                     * 您最有可能希望通过添加一些处理程序（如DiscardServerHandler）来配置新Channel的ChannelPipeline，
                     * 以实现您的网络应用程序。随着应用程序变得复杂，您可能会向管道添加更多处理程序，
                     * 并最终将此匿名类提取到顶级类中。
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    /**
                     * 您还可以设置特定于Channel实现的参数。我们正在编写TCP / IP服务器，
                     * 因此我们可以设置套接字选项，如tcpNoDelay和keepAlive。
                     * 请参阅ChannelOption的apidocs和特定的ChannelConfig实现，
                     * 以获得有关受支持的ChannelOptions的概述
                     */
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    /**
                     *你注意到option（）和childOption（）吗？ option（）用于接受传入连接的NioServerSocketChannel。
                     * childOption（）用于父ServerChannel接受的Channels，在这种情况下是NioServerSocketChannel。
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            /**
             * 我们现在准备好了。 剩下的就是绑定到端口并启动服务器。
             * 在这里，我们绑定到机器中所有NIC（网络接口卡）的端口8080。
             * 您现在可以根据需要多次调用bind（）方法（使用不同的绑定地址。）
             */
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new DiscardServer(port).run();
    }
}
