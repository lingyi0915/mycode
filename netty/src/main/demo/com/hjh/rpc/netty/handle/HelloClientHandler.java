package com.hjh.rpc.netty.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description:
 */
public class HelloClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext context;
    private String result;
    private String para;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("调用channelActive");
        context = ctx;
    }

    /**
     * 收到服务端数据，唤醒等待线程
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("调用channelRead:"+msg.toString()+",notify");
        result = msg.toString();
    }

    /**
     * 写出数据，开始等待唤醒
     */
    @Override
    public synchronized Object call() throws InterruptedException {
        context.writeAndFlush(para);
        return result;
    }

    public void setPara(String para) {
        this.para = para;
    }
}
