package com.hjh.leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class p760_Find_anagram_mappings {
  public static Map<Integer,List<Integer>> mid = new HashMap<Integer,List<Integer>>();
  public int[] anagramMappings(int[] A, int[] B) {
    int len = A.length;
    int [] res = new int[len];
    for(int i = 0 ; i < len ; i++){
      if(mid.containsKey(B[i])){
        mid.get(B[i]).add(i);
      }else {
        List<Integer> list = new ArrayList<Integer>();
        list.add(i);
        mid.put(B[i],list);
      }
    }
    for(int i = 0 ; i < len ; i++){
      List<Integer> list = mid.get(A[i]);
      res[i]=list.get(0);
      list.remove(0);
    }
    return res;
  }
  public static void main(String[] args) {
    p760_Find_anagram_mappings fam = new p760_Find_anagram_mappings();

    int [] A = {12, 28, 46, 32, 12, 50};
    int [] B = {50, 12, 32, 46, 28, 12};
    int [] res = fam.anagramMappings(A,B);
    for(int i = 0 ; i < res.length ; i++) {
      System.out.println(res[i]);
    }
  }
}
