package com.hjh.socket.netty.discard.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @Author: hjh
 * @Create: 2019/4/10
 * @Description:
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {// (1)

    enum WRITE{
        DISCARD,//消息直接丢弃
        RECEIVED,//接收数据并打印
        ECHO_SERVER,//返回到服务器
        TIME_SERVER,//输出到定时服务器

    }

    private WRITE WRITE_TYPE = WRITE.DISCARD;



    /**
     * 读到数据时
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {// (2)

        if(WRITE_TYPE == WRITE.DISCARD) {
            /**
             * 直接丢弃数据采用这一段代码
             */
            // Discard the received data silently.
            ((ByteBuf) msg).release(); // (3)
        }else if(WRITE_TYPE == WRITE.RECEIVED) {
            /**
             * 读取数据，并打印到屏幕
             */
            ByteBuf in = (ByteBuf) msg;
            try {
                while (in.isReadable()) { // (1)
                    System.out.print((char) in.readByte());
                    System.out.flush();
                }
            } finally {
                //可以调用 in.release();
                ReferenceCountUtil.release(msg); // (2)
            }
        }else if(WRITE_TYPE == WRITE.ECHO_SERVER) {
            /**
             * 到目前为止，我们一直在使用数据而没有响应。 但是，服务器通常应该响应请求。
             * 让我们学习如何通过实现ECHO协议向客户端写入响应消息，其中任何接收的数据都被发回。
             *
             * 与我们在前面部分中实现的丢弃服务器的唯一区别在于，
             * 它将接收到的数据发回，而不是将接收到的数据打印到控制台。
             * 因此，再次修改channelRead（）方法就足够了：
             */
            /**
             * ChannelHandlerContext对象提供各种操作，使您能够触发各种I / O事件和操作。
             * 在这里，我们调用write（Object）来逐字写入接收到的消息。
             * 请注意，我们没有发布收到的消息，这与我们在DISCARD示例中的操作不同。
             * 这是因为Netty在写入线路时会为您发布。
             */
            ctx.write(msg); // (1)
            /**
             * ctx.write（Object）不会将消息写入线路。
             * 它在内部缓冲，然后通过ctx.flush（）刷新到线路。
             * 或者，您可以调用ctx.writeAndFlush（msg）以简洁起见。
             */
            ctx.flush(); // (2)
        }else if(WRITE_TYPE == WRITE.TIME_SERVER) {



        }






    }

    /**
     * 异常捕获时的处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {// (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
