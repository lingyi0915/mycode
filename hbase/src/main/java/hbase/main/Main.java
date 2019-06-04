package hbase.main;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-20
 * @Description:
 */
public class Main {
  public static void main(String[] args) {
    File file = new File("D:\\workspace\\测试数据\\pcac.access_2018-03-06.log");
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String line = null;
      int i = 0 ;
      while((line = br.readLine()) != null) {
        i++;
//        System.out.println(line);
        String [] arr = line.split(" ");
        for(int j = 0 ; j < arr.length ; j ++){
          System.out.print("("+j+":"+arr[j]+")");
        }
        System.out.println("");
        if(i == 10){
          break;
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if(br != null) {
          br.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
