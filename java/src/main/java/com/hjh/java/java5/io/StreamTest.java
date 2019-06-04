package com.hjh.java.java5.io;

import java.io.*;

public class StreamTest {

    public byte[] inputStreamRead(InputStream in) throws FileNotFoundException,IOException{
        int available = in.available();
        //获取文件可读的字节数量
        byte[] bytes = null;
        if(available > 0){
            bytes = new byte[available];
            //多态，调用实现类的read方法
            in.read(bytes);
        }
        return bytes;
    }

    public void FileStreamRead(File file){
        try {
            InputStream in = new FileInputStream(file);
            byte[] bytes = inputStreamRead(in);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
