package com.hjh.javaapi.jni;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-13
 * @Description:
 */
public class HelloWorld {
  public native void sayHello(String name);
  static{
    System.loadLibrary("hello");
  }
  public static void main(String[] args){
    new HelloWorld().sayHello("jni");
  }
}
