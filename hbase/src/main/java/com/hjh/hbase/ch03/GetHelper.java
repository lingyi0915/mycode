package com.hjh.hbase.ch03;

import com.hjh.hbase.HBaseHelper;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-17
 * @Description:
 */
public class GetHelper extends Example{

  public GetHelper(String tablename){
    super(tablename);
  }

  public void getExample(){
    try {
      Get get = new Get(Bytes.toBytes("20180911-1"));
//      get.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("author"));
      Result result = table.get(get);
      System.out.println("Result: " + result);
      byte[] val = result.getValue(Bytes.toBytes("cf"),
       Bytes.toBytes("author"));
      System.out.println("Value: " + Bytes.toString(val));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {
    String tablename = "doc";
    GetHelper example = null;
    try {
      example = new GetHelper(tablename);
      example.getExample();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if(example != null) {
        example.close();
      }
    }
  }
}
