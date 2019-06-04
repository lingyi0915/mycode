package com.hjh.socket.netty.discard.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author: hjh
 * @Create: 2019/4/10
 * @Description:
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    /**
     *
     * @param ctx
     * 当建立连接并准备生成流量时，将调用channelActive（）方法。
     * 让我们写一个32位整数来表示这个方法中的当前时间。
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        {
            /**
             * 本节中要实现的协议是TIME协议。
             * 它与前面的示例的不同之处在于，它发送包含32位整数的消息，而不接收任何请求，并在发送消息后关闭连接。
             * 在此示例中，您将学习如何构造和发送消息，以及在完成时关闭连接。
             *
             * 因为我们将忽略任何接收的数据，但是一旦建立连接就发送消息，这次我们不能使用channelRead（）方法。
             * 相反，我们应该覆盖channelActive（）方法。 下面是实现：
             */

            /**
             * 要发送新消息，我们需要分配一个包含消息的新缓冲区。
             * 我们要写一个32位整数，因此我们需要一个容量至少为4个字节的ByteBuf。
             * 通过ChannelHandlerContext.alloc（）获取当前的ByteBufAllocator并分配一个新的缓冲区
             */
            final ByteBuf time = ctx.alloc().buffer(4); // (2)
            time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

            /**
             * 像往常一样，我们编写构造的消息。
             * 但等等，flip()的地方在哪里？在NIO中发送消息之前，我们不习惯调用java.nio.ByteBuffer.flip（）吗？
             * ByteBuf没有这样的方法，因为它有两个指针;一个用于读操作，另一个用于写操作。
             * 当您向ByteBuf写入内容而读取器索引未更改时，写入器索引会增加。
             * reader索引和writer索引分别表示消息的开始和结束位置。
             *
             * 注: java.nio.Buffer flip() 调换这个buffer的当前位置，并且设置当前位置是0。
             * 说的意思就是：将缓存字节数组的指针设置为数组的开始序列即数组下标0。这样就可以从buffer开头，
             * 对该buffer进行遍历（读取）了
             *
             * 相比之下，NIO缓冲区没有提供一种明确的方式来确定消息内容的开始和结束位置，而无需调用flip方法。
             * 当您忘记翻转缓冲区时，您将遇到麻烦，因为不会发送任何数据或不正确的数据。
             * 在Netty中不会发生这样的错误，因为我们对不同的操作类型有不同的指针。
             * 你会发现它让你的生活变得更加轻松，因为你已经习惯了 - 没有翻身的生活！
             *
             * 另一点需要注意的是ChannelHandlerContext.write（）（和writeAndFlush（））方法返回一个ChannelFuture。
             * ChannelFuture表示尚未发生的I / O操作。
             * 这意味着，任何请求的操作可能尚未执行，因为所有操作在Netty中都是异步的。
             * 例如，以下代码可能会在发送消息之前关闭连接：
             *
             * Channel ch = ...;
             * ch.writeAndFlush(message);
             * ch.close();
             *
             * 因此，您需要在完成ChannelFuture之后调用close（）方法，
             * 该方法由write（）方法返回，并在写入操作完成时通知其侦听器。
             * 请注意，close（）也可能不会立即关闭连接，并返回ChannelFuture。
             */
            final ChannelFuture f = ctx.writeAndFlush(time); // (3)

            /**
             * 当写请求完成后我们如何得到通知？
             * 这就像向返回的ChannelFuture添加ChannelFutureListener一样简单。
             * 在这里，我们创建了一个新的匿名ChannelFutureListener，它在操作完成时关闭Channel。
             */
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    assert f == future;
                    ctx.close();
                }
            }); // (4)

            /**
             * 或者，您可以使用预定义的侦听器简化代码：
             */
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
