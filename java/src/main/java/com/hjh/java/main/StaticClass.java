package com.hjh.java.main;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-02-06
 * @Description:
 */
public class StaticClass {
  String str = "";
  static{
    init();
  }
  public static void init(){

    System.out.println("123456");
  }

  public static void main(String[] args) {

  }
}
