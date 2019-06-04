package com.hjh.interview.chapter1;

import java.util.Arrays;

public class problem3 {

    public static boolean solution1(String str1,String str2){
        if(str1.length() != str2.length()){
            return false;
        }
        char[] chars1 = str1.toCharArray();
        char[] chars2 = str2.toCharArray();
        Arrays.sort(chars1);
        Arrays.sort(chars2);
        int i = 0;
        int len = str1.length();
        while(i<len && chars1[i] == chars2[i++]);
        return i==len;
    }

    public static boolean solution2(String str1,String str2){
        if(str1.length() != str2.length()){
            return false;
        }
        int len = str1.length();
        int[] arr = new int[256];
        Arrays.fill(arr,0);
        for(int i = 0 ; i < len ; i++){
            arr[str1.charAt(i)]++;
        }
        for(int i = 0 ; i < len ; i++){
            if(arr[str2.charAt(i)] == 0){
                return false;
            }else{
                arr[str2.charAt(i)]--;
            }
        }
        return true;
    }

    public static void main(String[] args) {

    }
}
