package com.hjh.java.algorithm;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description: 纯cpu执行指令的速度，一秒千亿级别
 */
public class Accumulation {
    public static void main(String[] args) {
        int n =  1000000000;
        long st1 = System.currentTimeMillis();
        int j = 0 ;
        for(int i = 0 ; i < n ; i++){
            j++;
        }
        System.out.println("j="+j);
        j=0;
        long et1 = System.currentTimeMillis();
        for(int i = 0 ; i < n ; i ++){
            j+=1;
        }
        System.out.println("j="+j);
        long et2 = System.currentTimeMillis();
        j=0;
        for(int i = 0 ; i < n ; i ++){
            j=j+1;
        }
        System.out.println("j="+j);
        long et3 = System.currentTimeMillis();

        System.out.println("i++耗时:"+(et1-st1));
        System.out.println("i+=1耗时:"+(et2-et1));
        System.out.println("i=i+1耗时:"+(et3-et2));
    }
}
