package com.hjh.leetcode;

import com.hjh.leetcode.helper.Tools;

/**
 * @Author: hjh
 * @Create: 2019/5/27
 * @Description:
 */
public class P88_Merge_Sorted_Array {

    /**
     * 要求在原数组上改动，建立新的改变指针没有用，，，
     * @param nums1
     * @param m
     * @param nums2
     * @param n
     */
    public static void merge(int[] nums1, int m, int[] nums2, int n) {
        if(nums2.length == 0 ){
            return ;
        }
        int[] nums3 = new int[nums1.length];
        int len = m+n;
        int a = 0 , b = 0;
        for (int i = 0 ; i < len ; i++) {
            if (a<m && b < n) {
                nums3[i] = nums1[a] < nums2[b] ? nums1[a++] : nums2[b++];
            } else if (a < m && b == n) {
                nums3[i] = nums1[a++];
            } else if (a >= m && b < n) {
                nums3[i] = nums2[b++];
            }
        }
        for(int i = 0 ; i < len ; i++){
            nums1[i] = nums3[i];
        }
    }


    public static void main(String[] args) {
        int [] nums1 = {1,2,3,0,0,0};
        int [] nums2 = {2,5,6};

        merge(nums1,3,nums2,3);

        Tools.<Integer>printArray(nums1);

    }

}
