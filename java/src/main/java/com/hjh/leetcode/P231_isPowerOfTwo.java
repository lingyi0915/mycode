package com.hjh.leetcode;

/**
 * @Author: hjh
 * @Create: 2019/5/27
 * @Description:
 */
public class P231_isPowerOfTwo {

    public static boolean isPowerOfTwo(int n) {
        if(n == 1 ){
            return true;
        }
        int x = 1;
        for(int i = 1 ; i < 32 ; i++){
            x<<=1;
            if(x == n) {
                return true;
            } else if(x > n){
                return false;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        isPowerOfTwo(4);
    }

}
