package hbase.bulkimport;

import com.hjh.hbase.HBaseHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-20
 * @Description:
 */
public class HfileRegionMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put>{
  private static final Log LOG = LogFactory.getLog(HfileRegionMapper.class.getName());
  private String[] fieldNames = {
          "remote_addr",
          "time_local",
          "time_zone",
          "http_method",
          "request",
          "http_version",
          "status",
          "http_referer",
          "body_bytes_sent",
          "remote_user"
  };
  private String[] fieldType = {
          "String",
          "String",
          "String",
          "String",
          "String",
          "String",
          "int",
          "String",
          "int",
          "String"
  };

  @Override
  protected void map(LongWritable key, Text value, Context context)
          throws IOException, InterruptedException {
    // TODO Auto-generated method stub

    String path = ((FileSplit) context.getInputSplit()).getPath().getName();
    String dt = path.substring(12,22).replaceAll("-","");

    String line = value.toString();
    String[] fieldValues =line.split(" ");

    String uuid = UUID.randomUUID().toString().replaceAll("-","");
    //创建HBase中的RowKey
    byte[] rowKey= Bytes.toBytes(dt+"-"+uuid);
    ImmutableBytesWritable rowKeyWritable=new ImmutableBytesWritable(rowKey);
    byte[] family=Bytes.toBytes("cf");
    Put put=new Put(rowKey);
    for (int i = 0 ; i < fieldNames.length ; i++){
      byte[] qualifier=Bytes.toBytes(fieldNames[i]);
      byte[] fieldValue=Bytes.toBytes(fieldValues[i]);
      put.addColumn(family, qualifier, fieldValue);
    }
    context.write(rowKeyWritable, put);
  }

  public static void main(String[] args) {
    Configuration conf = ConfigurationManager.getConf();
    try {
      Job job = Job.getInstance(conf, "bulkimport");
      job.setJarByClass(HfileRegionMapper.class);
      job.setMapperClass(HfileRegionMapper.class);
      job.setMapOutputKeyClass(ImmutableBytesWritable.class);
      job.setMapOutputValueClass(Put.class);

      //输入的txt
      FileInputFormat.addInputPath(job,new Path(args[0]));
      // 输出的hfile目录
      FileOutputFormat.setOutputPath(job,new Path(args[1]));

      TableName tableName = TableName.valueOf("access_url");

      HBaseHelper helper = HBaseHelper.getHelper();
      Connection conn = helper.getConn();
      Table table = helper.getTable(tableName);
      RegionLocator locator =  conn.getRegionLocator(tableName);
      HFileOutputFormat2.configureIncrementalLoad(job,table,locator);

      int isSuccess = job.waitForCompletion(true)?0:1;

      //job结束之后，调用BulkLoad方式来将MR结果批量入库
      LoadIncrementalHFiles loader = new LoadIncrementalHFiles(helper.getConf());
      //第一个参数为第二个Job的输出目录即保存HFile的目录，第二个参数为目标表
      loader.doBulkLoad(new Path(args[1]),helper.getAdmin(),table,locator);

      //最后调用System.exit进行退出
      System.exit(isSuccess);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
