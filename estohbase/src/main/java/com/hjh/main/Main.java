package com.hjh.main;

import com.hjh.hbase.HbaseUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MetaTableAccessor;
import org.apache.hadoop.hbase.client.MetaScanner;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFile;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.HFileContextBuilder;
import org.apache.hadoop.hbase.regionserver.HStore;
import org.apache.hadoop.hbase.regionserver.StoreFile;
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
    try {
      HbaseUtils hbaseUtil = new HbaseUtils();
//      hbaseUtil.createTable("doc","cf");
//      hbaseUtil.createIndex("doc","cf");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void writeHfile(HColumnDescriptor colDesc, FileSystem fs, Configuration conf, Path path,
                                KeyValue[] keys,int start,int stop){
    StoreFile.Writer writer = null;

    HFileContext hFileContext = new HFileContextBuilder()
            .withIncludesTags(HFile.getFormatVersion(conf)>= HFile.MIN_FORMAT_VERSION_WITH_TAGS)
            .withCompression(colDesc.getCompression())
            .withCompressTags(colDesc.isCompressTags())
            .withChecksumType(HStore.getChecksumType(conf))
            .withBytesPerCheckSum(HStore.getBytesPerChecksum(conf))
            .withBlockSize(colDesc.getBlocksize())
            .withHBaseCheckSum(true)
            .withDataBlockEncoding(colDesc.getDataBlockEncoding())
            .withCreateTime(EnvironmentEdgeManager.currentTime())
            .build();
    try{
      writer = new StoreFile.WriterBuilder(conf, new CacheConfig(conf),
              fs)
              .withFilePath(path)
              .withComparator(KeyValue.COMPARATOR)
              .withBloomType(colDesc.getBloomFilterType())
              .withFileContext(hFileContext)
              .build();
    }catch (IOException e){
      e.printStackTrace();
    }


  }
}
