package com.hjh.leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class p763_Partition_Labels {
  /**
   * 理解错了题意，判断的是每一part的最大值都比下一个part的最小值要小，即每个part都比之前所有part要大
   * @param S
   * @return
   */
  public List<Integer> partitionLabels2(String S) {
    List<Character> max = new ArrayList<Character>();//存放之前一段分区的最大字符
    List<Integer> pos = new ArrayList<Integer>();//存放之前一段分区结尾的位置
    List<Integer> res = new ArrayList<Integer>();//存放之前一段分区的长度
    int len = S.length();//字符串长度
    int size = 0;//分区数
    for(int i = 0 ; i < len ; i++){
      char c = S.charAt(i);
      //默认有1个分区
      max.add(c);
      pos.add(i);
      res.add(1);
      //如果当前读取的字符比上一个分区的最大值要小，说明，当前分区需要合并到上一个分区中
      //合并之前的节点，到上一个分区为止
      while (size>0&&c <= max.get(size - 1)) {
        max.set(size-1,max.get(size)<max.get(size-1)?max.get(size-1):max.get(size));
        max.remove(size);
        pos.set(size-1,i);
        pos.remove(size);
        res.set(size-1,res.get(size-1)+res.get(size));
        res.remove(size);
        size--;
      }
      size++;
//      for(int j = 0 ; j < size ; j++){
//        System.out.print("size="+size);
//        System.out.print("(max:"+max.get(j)+",pos:"+pos.get(j)+",len:"+res.get(j)+")");
//      }
//      System.out.println("");
    }
    return res;
  }

  public List<Integer> partitionLabels(String S) {
    int count = 0;
    List<Integer> res = new ArrayList<Integer>();//存放之前一段分区的长度
    int len = S.length();
    Map<Character,Integer> map = new HashMap<Character,Integer>();
    int [] left = new int[26];
    int [] right = new int[26];
    int size=0;
    for(int i = 0 ; i <len ; i++){
      char c = S.charAt(i);
      if(map.containsKey(c)){//如果该字符已经出现过
        int st = map.get(c);//得到该字符的出现位置
        while(size>0&&st<=right[size-1]){
          right[--size]=i;
        }
      }else{
        map.put(c,i);//记录字符第一次出现的位置
        left[size]=i;//字符第一次出现的时候，记为一个partition
        right[size]=i;//该partition起止位置
      }
      size++;
    }
    res.add(right[0]+1);
    for(int i = 1 ; i < size ; i++){
      res.add(right[i]-right[i-1]);
    }
    return res;
  }
  public static void main(String[] args) {
    p763_Partition_Labels pl = new p763_Partition_Labels();
    List<Integer> res = pl.partitionLabels("ababcbacadefegdehijhklij");
    for(int i = 0 ; i < res.size() ; i++){
      System.out.println(res.get(i));
    }
  }
}
