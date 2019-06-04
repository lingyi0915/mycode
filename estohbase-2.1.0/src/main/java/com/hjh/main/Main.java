package com.hjh.main;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MetaTableAccessor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFile;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.HFileContextBuilder;
import org.apache.hadoop.hbase.regionserver.HStore;
import org.apache.hadoop.hbase.regionserver.StoreFileWriter;
import org.apache.hadoop.hbase.tool.LoadIncrementalHFiles;
import org.apache.hadoop.hbase.util.EnvironmentEdgeManager;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-07-25
 * @Description:
 */
public class Main {
  public static void main(String[] args) {

      Configuration conf = HBaseConfiguration.create();
      System.out.println(HFile.getFormatVersion(conf));

      int n =  10;
      long st1 = System.currentTimeMillis();
      int j = 0 ;
    for(int i = 0 ; i < n ; i ++){
        if(i%1 == 0){
            j++;
        }
    }
      System.out.println("j="+j);
    j=0;
    long et1 = System.currentTimeMillis();
      for(int i = 0 ; i < n ; i ++){
          if(i%2 == 0){
              j++;
          }
//          if(i%2 == 1){
//              j++;
//          }
      }
      System.out.println("j="+j);
      long et2 = System.currentTimeMillis();

      j=0;
      for(int i = 0 ; i < n ; i ++){
          j++;
      }
      System.out.println("j="+j);
      long et3 = System.currentTimeMillis();

      System.out.println("分支预测优化耗时:"+(et1-st1));
      System.out.println("分支预测失败耗时:"+(et2-et1));
      System.out.println("纯累加耗时耗时:"+(et3-et2));

  }

  public static void writeHfile(ColumnFamilyDescriptor colDesc, FileSystem fs, Configuration conf, Path path,
                                KeyValue[] keys, int start, int stop) {
    StoreFileWriter writer = null;

    HFileContext hFileContext = new HFileContextBuilder()
            .withIncludesTags(HFile.getFormatVersion(conf) >= HFile.MIN_FORMAT_VERSION_WITH_TAGS)
            .withCompression(colDesc.getCompressionType())
            .withCompressTags(colDesc.isCompressTags())
            .withChecksumType(HStore.getChecksumType(conf))
            .withBytesPerCheckSum(HStore.getBytesPerChecksum(conf))
            .withBlockSize(colDesc.getBlocksize())
            .withHBaseCheckSum(true)
            .withDataBlockEncoding(colDesc.getDataBlockEncoding())
            .withCreateTime(EnvironmentEdgeManager.currentTime())
            .build();
    try {
      writer =new StoreFileWriter.Builder(conf, new CacheConfig(conf),
              fs)
              .withOutputDir(path)
              .withBloomType(colDesc.getBloomFilterType())
              .withMaxKeyCount(Integer.MAX_VALUE)
              .withFileContext(hFileContext)
              .build();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void bulkload(Path filePath, Admin admin, HTable table, RegionLocator locator){
//      new LoadIncrementalHFiles(table.getConfiguration()).doBulkLoad(filePath,table);
      try {
          new LoadIncrementalHFiles(table.getConfiguration()).doBulkLoad(filePath,admin,table,locator);
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
}
