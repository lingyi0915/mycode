package org.apache.lucene.test;

import org.junit.Test;

import java.io.*;
import java.util.Queue;
import java.util.zip.CheckedOutputStream;

public class TestVInt {

    private BufferedOutputStream os;
    private long bytesWritten = 0L;

    public TestVInt(){
        try {
            os = new BufferedOutputStream(new FileOutputStream(new File("D:\\1.txt")), 8192);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public final void writeByte(byte b) throws IOException {
        System.out.println(b);
    }

    public final void writeVInt(int i) throws IOException {
        // i & -128
        while ((i & ~0x7F) != 0) {
            // i & 127 | 128
            writeByte((byte)((i & 0x7F) | 0x80));
            i >>>= 7;
        }
        writeByte((byte)i);
    }

    public static void main(String[] args) {
        int value = 16384;
        TestVInt tv = new TestVInt();
        try {
            tv.writeVInt(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1(){
        System.out.println(1<<15);
    }

    public void test2(){
        Queue queue = null;
        Queue deleteQueue = null;
        // assert语法 exp1, 或者 exp1:exp2  exp2表示一个基本类型、表达式或者是一个Object，用于在失败时输出错误信息。
        assert queue == null
                || deleteQueue == queue : "expected: "
                + queue + "but was: " + deleteQueue;
    }

    @Test
    public void test3(){
        boolean b = true;
        b |= true;
        System.out.println(b);
    }

}
