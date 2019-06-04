package com.hjh.kafka.store;

import java.io.IOException;

/**
 * 自定义序列化字节输入
 */
public abstract class DataInput implements Cloneable {
    private static final int SKIP_BUFFER_SIZE = 1024;

    private byte[] skipBuffer;

    //根据不同的输入源具体实现不同
    public abstract byte readByte() throws IOException;

    // 字节数组读取 从offset开始读len长度 ，写入到b中
    public abstract void readBytes(byte[] b, int offset, int len)
            throws IOException;

    //
    public void readBytes(byte[] b, int offset, int len, boolean useBuffer)
            throws IOException
    {
        // Default to ignoring useBuffer entirely
        readBytes(b, offset, len);
    }

    public short readShort() throws IOException {
        return (short) (((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF));
    }

    public int readInt() throws IOException {
        return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
                | ((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF);
    }

    public int readVInt() throws IOException {
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

    /**
     * google 的 Protocol Buffers 压缩int 和 long使用的zigZag压缩编码
     * avro和thrift都采用这种方式
     *
     * @see
     *
     * @return
     * @throws IOException
     */
    public int readZInt() throws IOException {
//        return BitUtil.zigZagDecode(readVInt());
        return 0;
    }

}