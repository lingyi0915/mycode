package com.hjh.kafka.serializer;

import com.hjh.kafka.entry.FileMessage;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public class CustomerFileSerializer implements Serializer<FileMessage> {
    // 私有变量存在线程栈中，但是可能存在常量池中，这里只是一个引用
    private String encoding = "UTF8";

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String topic, FileMessage fileMessage) {
        if(fileMessage.getSize() == 0){
            return null;
        }
        StringBuffer sb = new StringBuffer();
        List<String> list = fileMessage.getFileList();
        for(String str : list){
            sb.append(str+",");
        }
        sb.setLength(sb.length()-1);
        byte [] message;
        try {
            message=sb.toString().getBytes(this.encoding);
            return message;
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException("Error when serializing string to byte[] due to unsupported encoding " + this.encoding);
        }
    }

    @Override
    public void close() {

    }
}
