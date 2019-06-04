package com.hjh.leetcode.helper;

/**
 * @Author: hjh
 * @Create: 2019/5/27
 * @Description:
 */
public class Tools {
    public static <T> void printArray(T[] a){
        for(int i = 0 ; i < a.length ; i++){
            System.out.print(a[i]+",");
        }
        System.out.println();
    }

    public static void printArray(int[] a){
        for(int i = 0 ; i < a.length ; i++){
            System.out.print(a[i]+",");
        }
        System.out.println();
    }

    public static void printList(ListNode t){
        while(t!=null){
            System.out.print(t.val+",");
            t = t.next;
        }
        System.out.println();
    }

    public static void printListArray(ListNode[] t){
        System.out.println("{");
        for(int i = 0 ; i < t.length ; i++){
            printList(t[i]);
        }
        System.out.println("}");
    }

}
