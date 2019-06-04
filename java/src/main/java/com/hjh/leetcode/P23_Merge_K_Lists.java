package com.hjh.leetcode;

import com.hjh.leetcode.helper.ListNode;
import com.hjh.leetcode.helper.Tools;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @Author: hjh
 * @Create: 2019/5/28
 * @Description:
 */
public class P23_Merge_K_Lists {

    public ListNode mergeKLists(ListNode[] lists){
        ListNode res;
//        res = mergeKLists1(lists);
//        res = mergeKLists2(lists);
//        res = mergeKLists3(lists);
        res = mergeKLists4(lists);
        return res;
    }

    /**
     * 每次从k个队列中获取最小值，插入到链表最后。循环直到结束
     * @param lists
     * @return  489ms
     */
    public ListNode mergeKLists1(ListNode[] lists) {
        ListNode head = new ListNode(-1),curr = head,min = null;
        int len = lists.length,j=0;
        while(true){
            min = null;
            for(int i = 0 ; i < len ; i++){
                if(lists[i] == null){
                    continue;
                }
                if(min == null && lists[i] != null){
                    min = lists[i];
                    j = i;
                    continue;
                }
                if(min.val > lists[i].val){
                    j = i;
                    min = lists[i];
                }
            }
            if(min == null){
                break;
            }
            curr.next = min;
            curr = min;
            lists[j] = lists[j].next;
        }
        return head.next;
    }

    /**
     * 优先级队列小顶堆 17ms
     * @param lists
     * @return
     */
    public ListNode mergeKLists2(ListNode[] lists) {
        if(lists == null || lists.length == 0){
            return null;
        }
        PriorityQueue<ListNode> queue = new PriorityQueue<>(new Comparator<ListNode>() {
            @Override
            public int compare(ListNode o1, ListNode o2) {
                return o1.val - o2.val;
            }
        });
        for(int i = 0; i < lists.length; i++){
            if(lists[i] != null){
                queue.offer(lists[i]);
            }
        }
        ListNode head = null;
        ListNode cur = null;
        while (!queue.isEmpty()){
            ListNode node = queue.poll();
            if(head == null){
                head = node;
                cur = head;
            }else {
                cur.next = node;
                cur = cur.next;
            }
            if(node.next != null){
                queue.offer(node.next);
            }
        }
        return head;
    }

    /**
     * 自己写堆 效率高点
     * @param lists
     * @return  13ms
     */
    public ListNode mergeKLists3(ListNode[] lists) {
        if(lists == null || lists.length == 0){
            return null;
        }

        MinHeap minHeap = new MinHeap(lists.length);
        for(int i = 0; i < lists.length; i++){
            if(lists[i] != null){
                minHeap.insert(lists[i]);
            }
        }
        ListNode head = null;
        ListNode cur = null;
        while (!minHeap.isEmpty()){
            ListNode node = minHeap.remove();
            if(head == null){
                head = node;
                cur = head;
            }else {
                cur.next = node;
                cur = cur.next;
            }
            if(node.next != null){
                minHeap.insert(node.next);
            }
        }
        return head;
    }

    /**
     * 实现一个简易的堆看看效果
     */
    public class MinHeap{
        private ListNode[] lists;
        private int index = 1;      //索引开始为1 根据子节点查找父节点只需要除二 不用判断奇偶

        public MinHeap(int len){
            lists = new ListNode[len + 1];
        }

        public ListNode insert(ListNode node){
            if(index == lists.length){
                return lists[1];
            }
            int pos = index;
            lists[index++] = node;
            //堆化
            while (pos > 1){
                int midPos = pos >> 1;
                if(lists[pos].val < lists[midPos].val){
                    ListNode tmp = lists[midPos];
                    lists[midPos] = lists[pos];
                    lists[pos] = tmp;
                    pos = midPos;
                }else {
                    break;
                }
            }
            return lists[1];
        }

        public ListNode remove(){
            ListNode result = lists[1];
            lists[1] = lists[index - 1];
            lists[index - 1] = null;
            index--;
            int pos = 1;
            while (pos <= (index - 1)/2){
                int minPos = pos;
                int minValue = lists[pos].val;
                if(lists[pos].val > lists[pos * 2].val){
                    minPos = pos * 2;
                    minValue = lists[pos * 2].val;
                }
                if(index - 1 >= 2 * pos + 1){
                    //右节点存在
                    if(minValue > lists[2 * pos + 1].val){
                        minPos = 2 * pos + 1;
                        minValue = lists[2 * pos + 1].val;
                    }
                }
                //和minPos互换
                if(pos != minPos){
                    ListNode tmp = lists[pos];
                    lists[pos] = lists[minPos];
                    lists[minPos] = tmp;
                    pos = minPos;
                }else {
                    break;
                }
            }
            return result;
        }

        public boolean isEmpty(){
            return index <= 1;
        }
    }

    /**
     * 分冶
     * @param lists
     * @return  6ms
     */
    public ListNode mergeKLists4(ListNode[] lists){
        if(lists.length == 0)
            return null;
        if(lists.length == 1)
            return lists[0];
        if(lists.length == 2){
            return mergeTwoLists(lists[0],lists[1]);
        }

        int mid = lists.length/2;
        ListNode[] l1 = new ListNode[mid];
        for(int i = 0; i < mid; i++){
            l1[i] = lists[i];
        }

        ListNode[] l2 = new ListNode[lists.length-mid];
        for(int i = mid,j=0; i < lists.length; i++,j++){
            l2[j] = lists[i];
        }

        return mergeTwoLists(mergeKLists4(l1),mergeKLists4(l2));

    }
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null) return l2;
        if (l2 == null) return l1;

        ListNode head = null;
        if (l1.val <= l2.val){
            head = l1;
            head.next = mergeTwoLists(l1.next, l2);
        } else {
            head = l2;
            head.next = mergeTwoLists(l1, l2.next);
        }
        return head;
    }

    public static void main(String[] args) {
        ListNode n11 = new ListNode(1);
        ListNode n12 = new ListNode(4);
        ListNode n13 = new ListNode(5);
        n11.next = n12;
        n12.next = n13;

        ListNode n21 = new ListNode(1);
        ListNode n22 = new ListNode(3);
        ListNode n23 = new ListNode(4);
        n21.next = n22;
        n22.next = n23;

        ListNode n31 = new ListNode(2);
        ListNode n32 = new ListNode(6);
        n31.next=n32;

        ListNode[] lists = new ListNode[3];
        lists[0] = n11;
        lists[1] = n21;
        lists[2] = n31;

        Tools.printListArray(lists);

        ListNode node = new P23_Merge_K_Lists().mergeKLists(lists);

        Tools.printList(node);
    }
}
