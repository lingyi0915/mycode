package com.hjh.socket.io;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description: 通过socket实现的客户端
 */
public class IOClient {
    public static void main(String[] args) {
        Thread clientThread = new Thread(() -> {
            try {
                Socket socket = new Socket("127.0.0.1", 8088);
                System.out.println("连接server");
                while (true) {
                    try {
                        socket.getOutputStream().write((new Date() + ": hello world").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("写入信息，等待2000ms");
                        Thread.sleep(2000);
                    } catch (Exception e) {
                    }
                }
            } catch (IOException e) {
            }
        });
        clientThread.start();
//        try {
//            clientThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
