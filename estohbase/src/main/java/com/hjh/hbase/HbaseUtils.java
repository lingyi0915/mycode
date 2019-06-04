package com.hjh.hbase;

import com.hjh.elasticsearch.ElasticSearchUtil;
import com.hjh.entity.Doc;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-07-06
 * @Description:
 */
public class HbaseUtils {
  public static final Log LOG = LogFactory.getLog(HbaseUtils.class.getName());
  private Connection conn = null;
  // 用HBaseconfiguration初始化配置信息是会自动加载当前应用的classpath下的hbase-site.xml
  public static Configuration conf;

  private static void init() throws IOException {
    // Default load from conf directory
    conf = HBaseConfiguration.create();
    String userdir = System.getProperty("user.dir") + File.separator + "conf" + File.separator;
    conf.addResource(new Path(userdir + "hbase-site.xml"));
  }

  public HbaseUtils() throws Exception{
    init();
    this.conn = ConnectionFactory.createConnection(conf);
    System.out.println(conn.getClass().getName());
  }

  /**
   * 创建一张表
   * @param tablename
   * @param columnFamilyName
   * @throws Exception
   */
  public void createTable(String tablename,String columnFamilyName){
    LOG.info("Entering testCreateTable.");
    TableName tableName = TableName.valueOf(tablename);
    // Specify the table descriptor.
    HTableDescriptor htd = new HTableDescriptor(tableName);
    // Set the column family name to info.
    HColumnDescriptor hcd = new HColumnDescriptor(columnFamilyName);
    //设置编码算法，HBase提供了DIFF，FAST_DIFF，PREFIX和PREFIX_TREE四种编码算法
    hcd.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
    //设置文件压缩方式，HBase默认提供了GZ和SNAPPY两种压缩算法
    //其中GZ的压缩率高，但压缩和解压性能低，适用于冷数据
    //SNAPPY压缩率低，但压缩解压性能高，适用于热数据
    //建议默认开启SNAPPY压缩
    hcd.setCompressionType(Compression.Algorithm.SNAPPY);
    htd.addFamily(hcd);
    Admin admin = null;
    try {
      // Instantiate an Admin object.
      admin = conn.getAdmin();
      if (!admin.tableExists(tableName)) {
        LOG.info("Creating table...");
        admin.createTable(htd);
        LOG.info(admin.getClusterStatus());
        LOG.info(admin.listNamespaceDescriptors());
        LOG.info("Table created successfully.");
      } else {
        LOG.warn("table already exists");
      }
    } catch (IOException e) {
      LOG.error("Create table failed " ,e);
    } finally {
      if (admin != null) {
        try {
          // Close the Admin object.
          admin.close();
        } catch (IOException e) {
          LOG.error("Failed to close admin " ,e);
        }
      }
    }
    LOG.info("Exiting testCreateTable.");
  }

  /**
   * 创建一张表
   * @param tablename
   * @throws Exception
   */
  public void createTable(String tablename){
    createTable(tablename,"cf");
  }

  public void put(String tablename,String rowId,String columnName,String value){
    put(tablename,rowId,"cf",columnName,value);
  }

  public void put(String tablename,String rowId,String columnFamilyName,String columnName,String value){
    LOG.info("Entering testPut.");
    TableName tableName = TableName.valueOf(tablename);
    Table table = null;
    try{
      table = conn.getTable(tableName);
      Put put = new Put(Bytes.toBytes(rowId));
      put.addColumn(columnFamilyName.getBytes(),columnName.getBytes(),value.getBytes());
      table.put(put);
    }catch (IOException e) {
      LOG.error("Put failed " ,e);
    } finally {
      if (table != null) {
        try {
          // Close the HTable object.
          table.close();
        } catch (IOException e) {
          LOG.error("Close table failed " ,e);
        }
      }
    }
  }
  public void createIndex(String tablename) throws Exception {
    createIndex(tablename,"cf");
  }
  public void createIndex(String tablename,String columnFamilyName) throws Exception {
    List<Doc> arrayList = new ArrayList<>();
    File file = new File("C:\\Users\\Administrator\\Desktop\\doc1.txt");
    List<String> list = FileUtils.readLines(file,"UTF8");
    for(String line : list){
      System.out.println(line);
      Doc Doc = new Doc();
      String[] split = line.split("\t");
      System.out.print(split[0]);
      int parseInt = Integer.parseInt(split[0].trim());
      Doc.setId(parseInt);
      Doc.setTitle(split[1]);
      Doc.setAuthor(split[2]);
      Doc.setTime(split[3]);
      Doc.setContent(split[3]);
      arrayList.add(Doc);
    }
    HbaseUtils hbaseUtils = new HbaseUtils();
    ElasticSearchUtil Esutil = new ElasticSearchUtil();
    for (Doc Doc : arrayList) {
      try {
        //把数据插入hbase
        hbaseUtils.put(tablename, Doc.getId()+"", columnFamilyName, "title", Doc.getTitle());
        hbaseUtils.put(tablename, Doc.getId()+"", columnFamilyName, "author", Doc.getAuthor());
        hbaseUtils.put(tablename, Doc.getId()+"", columnFamilyName, "time", Doc.getTime());
        hbaseUtils.put(tablename, Doc.getId()+"", columnFamilyName, "content", Doc.getContent());
        //把数据插入es
        Esutil.addIndex("tfjt","doc", Doc);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
