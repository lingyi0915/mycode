package com.hjh.leetcode;

import com.hjh.leetcode.helper.ListNode;
import com.hjh.leetcode.helper.Tools;

/**
 * @Author: hjh
 * @Create: 2019/5/28
 * @Description:
 */
public class P237_Delete_Node {

   public static void deleteNode(ListNode node) {
       ListNode next = node.next;
       node.val = next.val;
       node.next = next.next;
   }

    public static void main(String[] args) {
        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(3);
        n1.next = n2;
        n2.next = n3;

        ListNode t = n1;

        Tools.printList(n1);
        deleteNode(n2);
        Tools.printList(n1);
    }
}
