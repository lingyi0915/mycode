package com.hjh.interview.chapter1;

import java.util.HashSet;
import java.util.Set;

//实现一个算法，确定一个字符串的所有字符都不同，
// 允许使用额外空间
// 不允许使用额外空间
public class problem1 {

    /**
     * 使用额外空间 使用utf-8
     * @param str
     */
    public static Character solution1(String str){
        //空字符串
        if(str==null || str.length() == 0){
//            return true;
        }
        //ascii码和utf8编码的字符数
        if(str.length() > 256 || str.length() > 65536){
         //   return false
        }
        Set<Character> set = new HashSet<>();
        for(int i = 0 ; i < str.length() ; i++){
            Character c = str.charAt(i);
            if(set.contains(c)){
                return c;
            }else{
                set.add(c);
            }
        }
        return null;
    }

    /**
     * 不适用额外空间
     * @param str
     * @return
     */
    public static Character solution2(String str){
        //空字符串
        if(str==null || str.length() == 0){
//            return true;
        }
        //ascii码和utf8编码的字符数 看情况使用
        if(str.length() > 256 || str.length() > 65536){
            //   return false
        }
        for(int i = 0 ; i < str.length() - 1 ; i++){
            Character c = str.charAt(i);
            for(int j = i+1 ; j < str.length() ; j++){
                Character d = str.charAt(j);
                if(c==d){
                    return d;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {

        Character c = solution1("abbb");
        println(c.toString());
    }

    public static void println(String ctx){
        System.out.println(ctx);
    }

}
