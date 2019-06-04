package com.hjh.java.algorithm;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description: cpu 分支预测 效率测试 if语句性能测试
 */
public class CPUBrachPediction {
    public static void main(String[] args) {
        int n =  1000000000;
        long st1 = System.currentTimeMillis();
        int j = 0 ;
        for(int i = 0 ; i < n ; i ++){
            // 空循环被优化掉了
            // 一直为true的if语句，会被cpu分支预测优化，以后每次都按照true执行，为false时倒退，很耗时
            // 分支预测根据cpu不同，应用场景不同，有很多不同实现。 静态分支预测，动态分支预测等
            if(i%1 == 0){
                j++;
            }
        }
        System.out.println("j="+j);
        j=0;
        long et1 = System.currentTimeMillis();
        for(int i = 0 ; i < n ; i ++){
            if(i%2 == 0){
                j++;
            }
        }
        System.out.println("j="+j);
        long et2 = System.currentTimeMillis();
        j=0;
        for(int i = 0 ; i < n ; i ++){
            j++;
        }
        System.out.println("j="+j);
        long et3 = System.currentTimeMillis();

        System.out.println("分支预测优化耗时:"+(et1-st1));
        System.out.println("分支预测失败耗时:"+(et2-et1));
        System.out.println("纯累加耗时耗时:"+(et3-et2));
    }
}
