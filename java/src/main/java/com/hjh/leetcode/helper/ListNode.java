package com.hjh.leetcode.helper;

/**
 * @Author: hjh
 * @Create: 2019/5/28
 * @Description:
 */
public class ListNode {
    public int val;
    public ListNode next;
    public ListNode(int x) { val = x; }

    @Override
    public String toString() {
        return val+"->";
    }
}
