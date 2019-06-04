package com.hjh.leetcode;

import java.util.regex.Pattern;

public class p65_Valid_Number {
  public boolean isNumber(String s) {
    s=s.trim();
    int len = s.length();
    if(len==0){
      return false;
    }
    boolean isNum = Pattern.compile("^[-|+]?(\\d+\\.?\\d*|\\d*\\.?\\d+)([E|e][-|+]?\\d+)?$").matcher(s).find();
    return isNum;
  }
  public static void main(String[] args) {
    p65_Valid_Number vn = new p65_Valid_Number();
    System.out.println(vn.isNumber(" +3 "));
    System.out.println(vn.isNumber("1 a"));
    System.out.println(vn.isNumber("1"));
    System.out.println(vn.isNumber("1.1"));
    System.out.println(vn.isNumber("1.1.1"));
    System.out.println(vn.isNumber(".2e81"));
    System.out.println(vn.isNumber("-1."));
    System.out.println(vn.isNumber("-1.e10"));
    System.out.println(vn.isNumber("2e0"));
    System.out.println(vn.isNumber("-0.0"));
    System.out.println(vn.isNumber("-01"));
    System.out.println(vn.isNumber(".1"));
    System.out.println(vn.isNumber("."));
    System.out.println(vn.isNumber("+..110"));
    System.out.println(vn.isNumber("5-e95"));
  }
}
