package com.hjh.java.java5;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-04-20
 * @Description:可变参数
 */
public class Varargs {
  public void test(String... objs){
    for(String obj:objs){
      System.out.println(obj);
    }
  }

  public static void main(String[] args) {
    Varargs v = new Varargs();
    v.test("abc","bcd","efg");
  }
}
