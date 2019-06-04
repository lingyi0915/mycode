package com.hjh.java.sort;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileLineSort {

  public static File fileLineSort(String filepath,String resPath){
    File file = new File(filepath);
    File res = new File(resPath);
    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      List<Integer> list = new ArrayList<Integer>();
      String s = null;
      while((s=br.readLine())!=null){
        list.add(Integer.parseInt(s));
      }
      br.close();
      Collections.sort(list, new Comparator<Integer>(){
        public int compare(Integer s1, Integer s2) {
          return s1.compareTo(s2);
        }
      });
      Collections.sort(list, (Integer s1, Integer s2)->{
          return s1.compareTo(s2);
        });
      if(!res.exists()){
        res.createNewFile();
      }
      BufferedWriter bw = new BufferedWriter(new FileWriter(res));
      for(int i = 0 ; i < list.size() ; i++){
        bw.write(list.get(i));
        bw.newLine();
      }
      bw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e){
      e.printStackTrace();
    }
    return res;
  }

  public static void buildTestDataFile(String filepath,int num){
    File file = new File(filepath);
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      for(int i = 0 ; i < num ; i++){
        int r = (int)(Math.random()*num);
        bw.write(r);
        bw.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    FileLineSort.buildTestDataFile("d://1.txt",1000000);
    long starttime = System.currentTimeMillis();
    FileLineSort.fileLineSort("d://1.txt","d://2.txt");
    long endtime = System.currentTimeMillis();
    System.out.println(endtime-starttime);
  }
}
