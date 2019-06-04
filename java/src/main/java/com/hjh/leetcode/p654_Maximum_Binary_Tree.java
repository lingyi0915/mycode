package com.hjh.leetcode;

public class p654_Maximum_Binary_Tree {
  public TreeNode MaximumBinaryTree(int[] nums,int start,int end){
    TreeNode treeNode = null;
    if(start<=end) {
      int len = nums.length;
      int max = Integer.MIN_VALUE, pos = -1;
      for (int i = start; i <= end; i++) {
        if (nums[i]>max){
          max=nums[i];
          pos=i;
        }
      }
      treeNode = new TreeNode(max);
      treeNode.left=MaximumBinaryTree(nums,start,pos-1);
      treeNode.right=MaximumBinaryTree(nums,pos+1,end);
//      if(treeNode.left!=null){
//        System.out.println("left:"+treeNode.left.val+",");
//      }
//      System.out.println(treeNode.val);
//      if(treeNode.right!=null){
//        System.out.println(",right:"+treeNode.right.val);
//      }
    }
    return treeNode;
  }
  public TreeNode constructMaximumBinaryTree(int[] nums) {
    return MaximumBinaryTree(nums,0,nums.length-1);
  }
  public static void main(String[] args) {
    int [] num = {3,2,1,6,0,5};
    p654_Maximum_Binary_Tree mbt = new p654_Maximum_Binary_Tree();
    mbt.constructMaximumBinaryTree(num);
  }

  class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int x) { val = x; }
  }
}