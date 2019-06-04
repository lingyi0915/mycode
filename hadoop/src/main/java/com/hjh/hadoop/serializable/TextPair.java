package com.hjh.hadoop.serializable;

import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-13
 * @Description:
 */
public class TextPair implements WritableComparable{
  private Text key;
  private Text value;

  public TextPair() {
    set(new Text(),new Text());
  }

  public TextPair(String key,String value){
    set(new Text(key), new Text(value));
  }

  public TextPair(Text key,Text value) {
    set(key,value);
  }

  public void set(Text key, Text value){
    this.key = key;
    this.value = value;
  }

  public Text getKey() {
    return key;
  }

  public void setKey(Text key) {
    this.key = key;
  }

  public Text getValue() {
    return value;
  }

  public void setValue(Text value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return key + ":" + value;
  }

  @Override
  public void write(DataOutput dataOutput) throws IOException {
    key.write(dataOutput);
    value.write(dataOutput);
  }

  @Override
  public void readFields(DataInput dataInput) throws IOException {
    key.readFields(dataInput);
    value.readFields(dataInput);
  }

  @Override
  public int compareTo(Object obj) {
    if (obj instanceof TextPair) {
      TextPair tp = (TextPair) obj;
      int cmp = key.compareTo(tp.getKey());
      if (cmp != 0) {
        return cmp;
      }
      return value.compareTo(tp.getValue());
    }
    return 1;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TextPair) {
      TextPair tp = (TextPair) obj;
      return key.equals(tp.getKey()) && value.equals(tp.getValue());
    }
    return false;
  }
}
