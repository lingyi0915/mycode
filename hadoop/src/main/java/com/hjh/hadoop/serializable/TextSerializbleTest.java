package com.hjh.hadoop.serializable;

import org.apache.hadoop.io.*;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-13
 * @Description:
 */
public class TextSerializbleTest {
  public static void main(String[] args) throws IOException{
    DataOutputBuffer dob = new DataOutputBuffer();

    Text t = new Text("开始");
    TextPair tp = new TextPair("黄俊辉","男");
    DoubleWritable d = new DoubleWritable(1.99);
    System.out.println(t+","+tp+","+d);
    t.write(dob);
    tp.write(dob);
    d.write(dob);

    DataInputBuffer dib = new DataInputBuffer();
    dib.reset(dob.getData(), dob.getLength());

    System.out.println(dob.getLength());// 得到的是实际长度
    System.out.println(dob.getData().length);// byteArray的buf 长度为2的n次幂 会自动扩展 最大4g数据

    Writable t2 = (Writable)ReflectionUtils.newInstance(
            t.getClass(), null);
    Writable tp2 = (Writable)ReflectionUtils.newInstance(
            tp.getClass(), null);
    Writable d2 = (Writable)ReflectionUtils.newInstance(
            d.getClass(), null);

    t2.readFields(dib);
    tp2.readFields(dib);
    d2.readFields(dib);
    System.out.println(t2+","+tp2+","+d2);
  }

  public static DataOutputBuffer serializble(Writable obj) throws IOException{
    String className = obj.getClass().getName();
    DataOutputBuffer dob = new DataOutputBuffer();
    return dob;
  }

  public static DataInputBuffer deserializble(DataOutputBuffer dob) {
    DataInputBuffer dib = new DataInputBuffer();
    dib.reset(dob.getData(), dob.getLength());
    return dib;
  }
}
