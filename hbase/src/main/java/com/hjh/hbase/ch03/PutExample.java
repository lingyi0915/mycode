package com.hjh.hbase.ch03;

import com.hjh.hbase.HBaseHelper;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-11
 * @Description:
 */
public class PutExample extends Example{

  public PutExample(String tablename){
    super(tablename);
  }

  public void putExample(){
    try {
      Put put = new Put(Bytes.toBytes("20180911-1"));
      put.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("author"),Bytes.toBytes("tom"));
      put.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("sex"),Bytes.toBytes("man"));
      put.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("age"),Bytes.toBytes(23));
      table.put(put);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void putsExampleError1(){
    try {
      List<Put> puts = new ArrayList<Put>();
      for(int i = 0 ; i < 10 ; i++) {
        Put put = new Put(Bytes.toBytes("20180911-error-" + i));
        if (i == 5) {
          // 模拟一个错误的列族 错误行不写入，其他行写入
          // Column family af does not exist in region doc
          put.addColumn(Bytes.toBytes("af"), Bytes.toBytes("author"), Bytes.toBytes("tom" + i));
        }
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("author"), Bytes.toBytes("tom" + i));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("sex"), Bytes.toBytes(Math.random() % 2 == 0 ? "男" : "女"));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("age"), Bytes.toBytes(Math.random() % 10 + 20));
        puts.add(put);
      }
      table.put(puts);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void putsExampleError2(){
    try {
      List<Put> puts = new ArrayList<Put>();
      for(int i = 0 ; i < 10 ; i++) {
        Put put = new Put(Bytes.toBytes("20180911-error2-" + i));
        if (i == 5) {
          // 模拟一个空列,
          //table.put时报错 java.lang.IllegalArgumentException: No columns to insert
          puts.add(put);
          continue;
        }
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("author"), Bytes.toBytes("tom" + i));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("sex"), Bytes.toBytes(Math.random() % 2 == 0 ? "男" : "女"));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("age"), Bytes.toBytes(Math.random() % 10 + 20));
        puts.add(put);
      }
      table.put(puts);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void putsExampleError3(){
      List<Put> puts = new ArrayList<Put>();
      for(int i = 0 ; i < 10 ; i++) {
        Put put = new Put(Bytes.toBytes("20180911-error3-" + i));
        if(i == 4 || i == 3){
          put.addColumn(Bytes.toBytes("af"), Bytes.toBytes("author"), Bytes.toBytes("tom" + i));
          puts.add(put);
          continue;
        }
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("author"), Bytes.toBytes("tom" + i));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("sex"), Bytes.toBytes(Math.random() % 2 == 0 ? "男" : "女"));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("age"), Bytes.toBytes(Math.random() % 10 + 20));
        puts.add(put);
      }
    try {
      table.put(puts);
    } catch (RetriesExhaustedWithDetailsException e) {
      int numErrors = e.getNumExceptions();
      System.out.println("Number of exceptions: " + numErrors);
      for (int n = 0; n < numErrors; n++) {
        System.out.println("Cause[" + n + "]: " + e.getCause(n));
        System.out.println("Hostname[" + n + "]: " + e.getHostnamePort(n));
        System.out.println("Row[" + n + "]: " + e.getRow(n));
      }
      System.out.println("Cluster issues: " + e.mayHaveClusterIssues());
      System.out.println("Description: " + e.getExhaustiveDescription());
    } catch (IOException e) {
        e.printStackTrace();
    }
  }

  public void putsExample(){
    try {
      List<Put> puts = new ArrayList<Put>();
      for(int i = 0 ; i < 10 ; i++) {
        Put put = new Put(Bytes.toBytes("20180911-test-" + i));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("author"), Bytes.toBytes("tom" + i));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("sex"), Bytes.toBytes(Math.random() % 2 == 0 ? "男" : "女"));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("age"), Bytes.toBytes(Math.random() % 10 + 20));
        puts.add(put);
      }
      long st = System.currentTimeMillis();
      table.put(puts);
      long et = System.currentTimeMillis();
      System.out.println(et-st);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void checkAndPutTest(){
    Put put = new Put(Bytes.toBytes("20180911-check-1"));
    put.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("author"),Bytes.toBytes("tom1"));
    boolean res = false;
    try {
      //判断输入的4个参数的列是否存在，存在则put(put)修改数据，不存在(value = null) 则新增put  否则不put
      res = table.checkAndPut(Bytes.toBytes("20180911-test-1"),Bytes.toBytes("cf"),Bytes.toBytes("author"),Bytes.toBytes("tom2"),put);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(res);
  }

  public void putWriteBufferExample1(){
    Connection conn = helper.getConn();
    BufferedMutator mutator = null;
    try {
      mutator = conn.getBufferedMutator(tableName);
      for(int i = 0 ; i < 10 ; i++) {
        Put put = new Put(Bytes.toBytes("20180911-buffer-" + i));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("author"), Bytes.toBytes("tom" + i));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("sex"), Bytes.toBytes(Math.random() % 2 == 0 ? "男" : "女"));
        put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("age"), Bytes.toBytes(Math.random() % 10 + 20));
        mutator.mutate(put);
      }
      Get get = new Get(Bytes.toBytes("20180911-buffer-1"));
      Result res1 = table.get(get);
      System.out.println("Result: " + res1);
      // 这里触发一次rpc 刷写数据
      mutator.flush();
      Result res2 = table.get(get);
      byte[] quals = res2.getRow();
      System.out.println(Bytes.toString(quals));
      System.out.println(Bytes.toString(res2.getValue(Bytes.toBytes("cf"),Bytes.toBytes("age"))));
      System.out.println("Result: " + res2);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        mutator.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


  }

  public void runAll(){
    putExample();
    putsExample();
//    putsExampleError1();
//    putsExampleError2();
//    putsExampleError3();
    checkAndPutTest();
    putWriteBufferExample1();
  }

  public static void main(String[] args) {
    String tablename = "doc";
    PutExample example = null;
    try {
      example = new PutExample(tablename);
//      example.dropAndCreateTable(tablename);
      example.putWriteBufferExample1();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if(example != null) {
        example.close();
      }
    }
  }

}
