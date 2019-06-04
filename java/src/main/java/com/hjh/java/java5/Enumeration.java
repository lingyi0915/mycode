package com.hjh.java.java5;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-04-20
 * @Description:
 */
public enum Enumeration {
   RED_INT("红色",1), GREEN_INT("绿色", 2), BLANK_INT("白色", 3), YELLO_INT("黄色", 4);
  // 成员变量
  private String name;
  private int index;
  // 构造方法
  private Enumeration(String name, int index) {
    this.name = name;
    this.index = index;
  }
}



