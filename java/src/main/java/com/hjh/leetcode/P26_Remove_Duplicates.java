package com.hjh.leetcode;

/**
 * @Author: hjh
 * @Create: 2019/5/26
 * @Description:
 */
public class P26_Remove_Duplicates {
    public static int removeDuplicates(int[] nums) {
        int len = nums.length;
        if(len == 0){
            return 0;
        }
        if(nums[len-1] == nums[0]){
            return 1;
        }
        int newLen = 0;
        for(int i = 1 ; i < len ; i++){
            if(nums[i] == nums[i-1]){
                continue;
            } else{
                nums[++newLen] = nums[i];
                if(nums[newLen] == nums[len-1]){
//                    System.out.println(newLen+1);
                    return newLen+1;
                }
            }
        }
//        for (int i = 0; i < len; i++) {
//            System.out.print(nums[i]+",");
//        }
//        System.out.println();
//        System.out.println(newLen+1);
        return newLen+1;
    }

    public static int removeDuplicatesPretty(int[] nums) {
        int len = nums.length;
        if(len == 0){
            return 0;
        }
        if(nums[len-1] == nums[0]){
            return 1;
        }
        int newLen = 0;
        for(int i = 1 ; i < len ; i++){
            if(nums[i] != nums[i-1]){
                nums[++newLen] = nums[i];
                if(nums[newLen] == nums[len-1]){
                    return newLen+1;
                }
            }
        }
        return newLen+1;
    }

    public static int removeDuplicatesPretty2(int[] nums) {
        int len = nums.length;
        if(len == 0) return 0;
        int newLen = 0;
        for(int i = 1 ; i < len ; i++){
            if(nums[i] != nums[i-1]){
                nums[++newLen] = nums[i];
            }
        }
        return newLen+1;
    }

    public static void main(String[] args) {
//        int [] nums = {0,0,1,1,1,2,2,3,3,4};
        int [] nums = {0,0,1,1,1,2,2,3,3,100};
        removeDuplicates(nums);
    }
}
