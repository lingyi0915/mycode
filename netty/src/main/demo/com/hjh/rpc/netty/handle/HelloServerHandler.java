package com.hjh.rpc.netty.handle;

import com.hjh.rpc.netty.bootstrap.MyClientBootstrap;
import com.hjh.rpc.netty.service.HelloService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description: 用于处理请求数据
 */
public class HelloServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        // 如何符合约定，则调用本地方法，返回数据
//        System.out.println("msg.toString:"+msg.toString());
//        System.out.println("true/false:"+msg.toString().startsWith(MyClientBootstrap.providerName));
        if (msg.toString().startsWith(MyClientBootstrap.providerName)) {
            String result = new HelloService()
                    .start(msg.toString().substring(msg.toString().lastIndexOf("#") + 1));
            System.out.println(result);
            //写回去
            ctx.writeAndFlush(result);
        }
    }
}
