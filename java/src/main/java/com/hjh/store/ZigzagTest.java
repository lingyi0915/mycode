package com.hjh.store;

import java.io.IOException;
import java.util.Random;

public class ZigzagTest {

    private static int size = 10;
    private static byte[] vIntegers = new byte[5*size];
    private static int readOffset = 0;
    private static int writeOffset = 0;

    public static int zigZagDecode(int i) {
        return ((i >>> 1) ^ -(i & 1));
    }
    public static int zigZagEncode(int i) {
        return (i >> 31) ^ (i << 1);
    }

    public static void writeZInt(int i) throws IOException {
        writeVInt(zigZagEncode(i));
    }

    public static void writeVInt(int i) throws IOException {
        while ((i & ~0x7F) != 0) {
            writeByte((byte)((i & 0x7F) | 0x80));
            i >>>= 7;
        }
        writeByte((byte)i);
    }

    public static int readVInt() throws IOException {
        byte b = readByte();
        if (b >= 0) return b;
        int i = b & 0x7F;
        b = readByte();
        i |= (b & 0x7F) << 7;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7F) << 14;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7F) << 21;
        if (b >= 0) return i;
        b = readByte();
        // Warning: the next ands use 0x0F / 0xF0 - beware copy/paste errors:
        i |= (b & 0x0F) << 28;
        if ((b & 0xF0) == 0) return i;
        throw new IOException("Invalid vInt detected (too many bits)");
    }

    public static byte readByte() throws IOException{
        return vIntegers[readOffset++];
    };

    public static void writeByte(byte b) throws IOException {
        vIntegers[writeOffset++] = b;
    }

    public static void main(String[] args) {


        int[] randomIntegers = new int[size];
        int[] resultIntegers = new int[size];

        Random r = new Random();

        for(int i = 0 ; i < size ; i++){
            randomIntegers[i] = r.nextInt(Integer.MAX_VALUE);
        }

        for(int i = 0 ; i < size ; i++){
            try {
                writeVInt(randomIntegers[i]);
                resultIntegers[i] = readVInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(writeOffset);

        for(int i = 0 ; i < size ; i++){
            System.out.print(randomIntegers[i]+",");
        }
        System.out.println();
        for(int i = 0 ; i < size ; i++) {
            System.out.print(resultIntegers[i] + ",");
        }


    }
}
