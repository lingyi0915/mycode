package com.hjh.hbase.ch03;

import com.hjh.hbase.HBaseHelper;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-18
 * @Description:
 */
public class Example {
  protected HBaseHelper helper = null;
  protected Table table = null;
  protected final TableName tableName;

  public Example(String tablename){
    tableName = TableName.valueOf(tablename);
    try {
      helper = HBaseHelper.getHelper();
      table = helper.getTable(tableName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void dropAndCreateTable(String tableName) throws IOException {
    dropAndCreateTable(TableName.valueOf(tableName));
  }

  public void dropAndCreateTable(TableName tableName) throws IOException {
    helper.dropTable(tableName);
    helper.createTable(tableName,"cf");
  }

  public void close(){
    try {
      if(table != null) {
        table.close();
      }
      if(helper != null) {
        helper.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
