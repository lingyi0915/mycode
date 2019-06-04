package com.hjh.main;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-08-01
 * @Description:
 */
public class Main2 {
  public static void main(String[] args) {
    try {
      for(int i = 0 ; i < 10000 ; i++){
        System.out.print(i+",");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
