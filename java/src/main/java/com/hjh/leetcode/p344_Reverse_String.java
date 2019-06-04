package com.hjh.leetcode;

public class p344_Reverse_String {
  public String reverseString(String s) {
    int len = s.length()-1;
    int len1 = s.length()/2;
    char[] res = s.toCharArray();
    char tc;
    for(int i = 0 ; i < len1 ; i++){
      tc=res[i];
      res[i]=res[len-i];
      res[len-i]=tc;
    }
    return new String(res);
  }
  public static void main(String[] args) {
    p344_Reverse_String rs = new p344_Reverse_String();
    System.out.println(rs.reverseString(""));
  }
}
