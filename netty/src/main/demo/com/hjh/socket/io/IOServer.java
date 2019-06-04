package com.hjh.socket.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description: 通过socket实现的服务端
 *
 * 存在问题 while(true) 非常浪费
 * IO编程模型在客户端较少的情况下运行良好，但是对于客户端比较多的业务来说，
 * 单机服务端可能需要支撑成千上万的连接，IO模型可能就不太合适了，我们来分析一下原因。
 *
 * 线程资源受限：线程是操作系统中非常宝贵的资源，同一时刻有大量的线程处于阻塞状态是非常严重的资源浪费，操作系统耗不起
 * 线程切换效率低下：单机cpu核数固定，线程爆炸之后操作系统频繁进行线程切换，应用性能急剧下降。
 * 除了以上两个问题，IO编程中，我们看到数据读写是以字节流为单位，效率不高。
 *
 * 为了解决这三个问题，JDK在1.4之后提出了NIO。
 *
 * 作者：简书闪电侠
 * 链接：https://www.jianshu.com/p/a4e03835921a
 * 来源：简书
 * 简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
 *
 */
public class IOServer {

    public static void startSocket(int port){
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("创建socket连接");
            // (1) 接收新连接线程
            Thread socketThread = new Thread(()->{
                while(true){
                    try {
                        System.out.println("运行中，尝试获得新的连接");
                        // (1) 阻塞方法获取新的连接
                        Socket socket = serverSocket.accept();

                        // (2) 每一个新的连接都创建一个线程，负责读取数据
                        new Thread(() -> {
                            try {
                                byte[] data = new byte[1024];
                                InputStream inputStream = socket.getInputStream();
                                while (true) {
                                    int len;
                                    // (3) 按字节流方式读取数据
                                    while ((len = inputStream.read(data)) != -1) {
                                        System.out.println(new String(data, 0, len));
                                    }
                                }
                            } catch (IOException e) {
                            }
                        }).start();
                        //优化while true，不至于无限死循环，线程切换降低cpu使用率，但是消耗上下文切换时间
                        Thread.sleep(1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            });

            socketThread.start();
            System.out.println(socketThread.getName());
//            try {
//                socketThread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            System.out.println("主线程结束");
//            Thread.currentThread().join(socketThread);
//            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        startSocket(8088);
    }
}
