package com.hjh.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description:
 *
 * IO模型中，一个连接来了，会创建一个线程，对应一个while死循环，
 * 死循环的目的就是不断监测这条连接上是否有数据可以读，
 * 大多数情况下，1w个连接里面同一时刻只有少量的连接有数据可读，
 * 因此，很多个while死循环都白白浪费掉了，因为读不出啥数据。
 *
 * 而在NIO模型中，他把这么多while死循环变成一个死循环，这个死循环由一个线程控制
 * 这就是NIO模型中selector的作用，一条连接来了之后，
 * 现在不创建一个while死循环去监听是否有数据可读了，
 * 而是直接把这条连接注册到selector上，
 * 然后，通过检查这个selector，就可以批量监测出有数据可读的连接，
 * 进而读取数据，
 *
 * 线程切换效率低下
 * 由于NIO模型中线程数量大大降低，线程切换效率因此也大幅度提高
 *
 * IO读写以字节为单位
 * NIO解决这个问题的方式是数据读写不再以字节为单位，而是以字节块为单位。IO模型中，每次都是从操作系统底层一个字节一个字节地读取数据，而NIO维护一个缓冲区，每次可以从这个缓冲区里面读取一块的数据，
 * 这就好比一盘美味的豆子放在你面前，你用筷子一个个夹（每次一个），肯定不如要勺子挖着吃（每次一批）效率来得高。
 *
 * 作者：简书闪电侠
 * 链接：https://www.jianshu.com/p/a4e03835921a
 * 来源：简书
 * 简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
 */
public class NIOServer {

    public static void startServer(int port){

        try {
            //负责轮询是否有新的连接
            Selector serverSelector = Selector.open();
            //负责轮询连接是否有数据可读
            Selector clientSelector = Selector.open();

            //服务端监测到新的连接之后，不再创建一个新的线程，而是直接将新连接绑定到
            Thread serverThread = new Thread(()->{
                try {
                    // 对应IO编程中服务端启动
                    ServerSocketChannel listenerChannel = ServerSocketChannel.open();
                    //绑定端口
                    listenerChannel.socket().bind(new InetSocketAddress(port));
                    //是否阻塞
                    listenerChannel.configureBlocking(false);
                    listenerChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

                    while(true){
                        // 监测是否有新的连接，这里的1指的是阻塞的时间为1ms
                        if(serverSelector.select(1)>0){
                            Set<SelectionKey> set = serverSelector.selectedKeys();
                            Iterator<SelectionKey> keyIterator = set.iterator();

                            while(keyIterator.hasNext()){
                                SelectionKey key = keyIterator.next();
                                if(key.isAcceptable()){
                                    try{
                                        // (1) 每来一个新连接，不需要创建一个线程，而是直接注册到clientSelector
                                        SocketChannel clientChannel = ((ServerSocketChannel)key.channel()).accept();
                                        clientChannel.configureBlocking(false);
                                        clientChannel.register(clientSelector,SelectionKey.OP_READ);
                                    } finally{
                                        keyIterator.remove();
                                    }
                                }
                            }
                        }
                        try {
                            //用上线文切换的代价替换死循环的代价 减小开销
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

            serverThread.start();

            Thread clientThread = new Thread(()->{
                while(true){
                    try {
                        // (2) 批量轮询是否有哪些连接有数据可读，这里的1指的是阻塞的时间为1ms
                        if(clientSelector.select(1) > 0) {
                            Set<SelectionKey> set = clientSelector.selectedKeys();
                            Iterator<SelectionKey> keyIterator = set.iterator();

                            while (keyIterator.hasNext()) {
                                SelectionKey key = keyIterator.next();

                                if (key.isReadable()) {
                                    try {
                                        SocketChannel clientChannel = (SocketChannel) key.channel();
                                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                        // (3) 读取数据以块为单位批量读取
                                        clientChannel.read(byteBuffer);
                                        byteBuffer.flip();
                                        System.out.println(Charset.defaultCharset().newDecoder().decode(byteBuffer)
                                                .toString());
                                    } finally {
                                        keyIterator.remove();
                                        key.interestOps(SelectionKey.OP_READ);
                                    }
                                }

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            clientThread.start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        startServer(8088);
    }
}
