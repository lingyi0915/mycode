package com.hjh.hadoop;

import org.apache.hadoop.io.Text;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-06
 * @Description:
 */
public class TextIterator {
  public static void main(String[] args) {
    Text t = new Text("\u0041\u00DF\u6771\uD801\uDC00");
    ByteBuffer buf = ByteBuffer.wrap(t.getBytes(), 0, t.getLength());
    System.out.println(t);
    int cp;
    while (buf.hasRemaining() && (cp = Text.bytesToCodePoint(buf)) != -1) {
      System.out.println(Integer.toHexString(cp));
    }
  }
}
