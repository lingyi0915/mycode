package com.hjh.leetcode;

import com.hjh.leetcode.helper.ListNode;
import com.hjh.leetcode.helper.Tools;
import javafx.collections.transformation.SortedList;

import java.util.List;

public class P148_Sort_List {
    public ListNode sortList(ListNode head) {
        if (head == null || head.next == null) return head;
//        List

        return null;
    }

    public static void main(String[] args) {
        ListNode n11 = new ListNode(9);
        ListNode n12 = new ListNode(5);
        ListNode n13 = new ListNode(4);
        n11.next = n12;
        n12.next = n13;

        Tools.printList(n11);
        new P148_Sort_List().sortList(n11);
        Tools.printList(n11);
    }
}