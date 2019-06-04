package com.hjh.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.Closeable;
import java.io.File;
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
public class HBaseHelper implements Closeable {
  private Configuration conf = null;
  private Connection conn = null;
  private Admin admin = null;

  protected HBaseHelper() throws IOException {
    this.conf = HBaseConfiguration.create();
    String userdir = System.getProperty("user.dir") + File.separator + "conf" + File.separator;
    System.out.println(userdir);
    this.conf.addResource(new Path(userdir + "hbase-site.xml"));
    this.conn = ConnectionFactory.createConnection(conf);
    this.admin = conn.getAdmin();
  }

  protected HBaseHelper(Configuration conf) throws IOException {
    this.conf = conf;
    String userdir = System.getProperty("user.dir") + File.separator + "conf" + File.separator;
    System.out.println(userdir);
    this.conf.addResource(new Path(userdir + "hbase-site.xml"));
    this.conn = ConnectionFactory.createConnection(conf);
    this.admin = conn.getAdmin();
  }

  public static HBaseHelper getHelper() throws IOException {
    return new HBaseHelper();
  }

  public static HBaseHelper getHelper(Configuration conf) throws IOException {
    return new HBaseHelper(conf);
  }

  public Configuration getConf() {
    return conf;
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public Connection getConn() {
    return conn;
  }

  public void setConn(Connection conn) {
    this.conn = conn;
  }

  public Admin getAdmin() {
    return admin;
  }

  public void setAdmin(Admin admin) {
    this.admin = admin;
  }

  public Table getTable(String tableName) throws IOException {
    return getTable(TableName.valueOf(tableName));
  }

  public Table getTable(TableName tableName) throws IOException {
    return conn.getTable(tableName);
  }

  public void createTable(String table, String... colfams)
          throws IOException {
    createTable(TableName.valueOf(table), 1, null, colfams);
  }

  public void createTable(TableName table, String... colfams)
          throws IOException {
    createTable(table, 1, null, colfams);
  }

  public void createTable(String table, int maxVersions, String... colfams)
          throws IOException {
    createTable(TableName.valueOf(table), maxVersions, null, colfams);
  }

  public void createTable(TableName table, int maxVersions, String... colfams)
          throws IOException {
    createTable(table, maxVersions, null, colfams);
  }

  public void createTable(String table, byte[][] splitKeys, String... colfams)
          throws IOException {
    createTable(TableName.valueOf(table), 1, splitKeys, colfams);
  }
  /**
   *
   * @param tableName 表名
   * @param maxVersions 最多版本号
   * @param splitKeys 预建region数
   * @param columnFamilys 变长列族
   */
  public void createTable(TableName tableName, int maxVersions, byte[][] splitKeys, String ...columnFamilys) throws IOException{
    //HTableDescriptor 包括hbase table的各种细节，所以先创建对象
    HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
    for (String cf : columnFamilys) {
      // 包含列细节
      HColumnDescriptor colDescriptor = new HColumnDescriptor(cf);
      // 列族最大版本数
      colDescriptor.setMaxVersions(maxVersions);
      //添加到表描述中
      tableDescriptor.addFamily(colDescriptor);
    }
    if (splitKeys != null) {
      // 预建分区
      admin.createTable(tableDescriptor, splitKeys);
    } else {
      // 自动扩分区
      admin.createTable(tableDescriptor);
    }
  }

  public void disableTable(String tableName) throws IOException{
    disableTable(TableName.valueOf(tableName));
  }

  public void disableTable(TableName tableName) throws IOException{
    admin.disableTable(tableName);
  }

  public void dropTable(String tableName) throws IOException{
    dropTable(TableName.valueOf(tableName));
  }

  public void dropTable(TableName tableName) throws IOException{
    if(existsTable(tableName)){
      if(admin.isTableEnabled(tableName)){
        disableTable(tableName);
      }
      admin.deleteTable(tableName);
    }
  }

  public boolean existsTable(String tableName) throws IOException{
    return existsTable(TableName.valueOf(tableName));
  }

  public boolean existsTable(TableName tableName) throws IOException{
    return admin.tableExists(tableName);
  }

  public void put(String tableName, String row, String fam, String qual,
                  String val) throws IOException {
    put(TableName.valueOf(tableName), row, fam, qual, val);
  }

  public void put(TableName table, String row, String fam, String qual,
                  String val) throws IOException {
    Table tbl = conn.getTable(table);
    Put put = new Put(Bytes.toBytes(row));
    put.addColumn(Bytes.toBytes(fam), Bytes.toBytes(qual), Bytes.toBytes(val));
    tbl.put(put);
    tbl.close();
  }

  public void put(String table, String row, String fam, String qual, long ts,
                  String val) throws IOException {
    put(TableName.valueOf(table), row, fam, qual, ts, val);
  }

  public void put(TableName table, String row, String fam, String qual, long ts,
                  String val) throws IOException {
    Table tbl = conn.getTable(table);
    Put put = new Put(Bytes.toBytes(row));
    put.addColumn(Bytes.toBytes(fam), Bytes.toBytes(qual), ts,
            Bytes.toBytes(val));
    tbl.put(put);
    tbl.close();
  }
  public void put(String table, String[] rows, String[] fams, String[] quals,String[] vals) throws IOException {
    put(TableName.valueOf(table), rows, fams, quals, vals);
  }

  /**
   * 取每个index相等的值，如果数组较短，取最后，较长舍弃
   * @param table 表名
   * @param rows  rowid
   * @param fams  列族
   * @param quals 列
   * @param vals  值 只支持字符串
   * @throws IOException
   */
  public void put(TableName table, String[] rows, String[] fams, String[] quals,String[] vals) throws IOException {
    Table tbl = conn.getTable(table);
    BufferedMutator mutator = conn.getBufferedMutator(table);
    for(int v = 0 ; v < rows.length ; v++) {
      String row = rows[v];
      Put put = new Put(Bytes.toBytes(rows[v]));
      String fam = fams[v < fams.length ? v : fams.length - 1];
      String qual = quals[v < quals.length ? v : quals.length - 1];
      String val = vals[v < vals.length ? v : vals.length - 1];
      System.out.println("Adding: " + row + " " + fam + " " + qual + " " + val);
      put.addColumn(Bytes.toBytes(fam), Bytes.toBytes(qual),
              Bytes.toBytes(val));
      mutator.mutate(put);
    }
    mutator.flush();// 批量刷新
    tbl.close();
  }

  public void put(String table, String[] rows, String[] fams, String[] quals,
                  long[] ts, String[] vals) throws IOException {
    put(TableName.valueOf(table), rows, fams, quals, ts, vals);
  }

  public void put(TableName table, String[] rows, String[] fams, String[] quals,
                  long[] ts, String[] vals) throws IOException {
    Table tbl = conn.getTable(table);
    for (String row : rows) {
      Put put = new Put(Bytes.toBytes(row));
      for (String fam : fams) {
        // 每个列族都put一遍数据
        int v = 0;
        for (String qual : quals) {
          // 解决长度不匹配的问题，取最后一个val做为补充数据 val长无所谓
          String val = vals[v < vals.length ? v : vals.length - 1];
          // 解决长度不匹配的问题，取最后一个val做为补充数据 val长无所谓
          long t = ts[v < ts.length ? v : ts.length - 1];
          System.out.println("Adding: " + row + " " + fam + " " + qual +
                  " " + t + " " + val);
          put.addColumn(Bytes.toBytes(fam), Bytes.toBytes(qual), t,
                  Bytes.toBytes(val));
          v++;
        }
      }
      tbl.put(put);
    }
    tbl.close();
  }

  public void dump(String table, String[] rows, String[] fams, String[] quals)
          throws IOException {
    dump(TableName.valueOf(table), rows, fams, quals);
  }

  public void dump(TableName table, String[] rows, String[] fams, String[] quals)
          throws IOException {
    Table tbl = conn.getTable(table);
    List<Get> gets = new ArrayList<Get>();
    for (String row : rows) {
      Get get = new Get(Bytes.toBytes(row));
      get.setMaxVersions();
      if (fams != null) {
        for (String fam : fams) {
          for (String qual : quals) {
            get.addColumn(Bytes.toBytes(fam), Bytes.toBytes(qual));
          }
        }
      }
      gets.add(get);
    }
    Result[] results = tbl.get(gets);
    for (Result result : results) {
      for (Cell cell : result.rawCells()) {
        System.out.println("Cell: " + cell +
                ", Value: " + Bytes.toString(cell.getValueArray(),
                cell.getValueOffset(), cell.getValueLength()));
      }
    }
    tbl.close();
  }

  public void dump(String table) throws IOException {
    dump(TableName.valueOf(table));
  }

  public void dump(TableName table) throws IOException {
    try (
            Table t = conn.getTable(table);
            ResultScanner scanner = t.getScanner(new Scan())
    ) {
      for (Result result : scanner) {
        dumpResult(result);
      }
    }
  }

  public void dumpResult(Result result) {
    for (Cell cell : result.rawCells()) {
      System.out.println("Cell: " + cell +
              ", Value: " + Bytes.toString(cell.getValueArray(),
              cell.getValueOffset(), cell.getValueLength()));
    }
  }

  @Override
  public void close() throws IOException {
    if(conn != null && !conn.isClosed()){
      conn.close();
    }
    if(admin != null) {
      admin.close();
    }
  }

}
