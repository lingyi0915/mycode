package com.hjh.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.MapDriver;
import org.junit.Test;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-07
 * @Description:
 */
public class MaxTemperatureTest {
  @Test
  public void processValidRecord() {
    String path1 = "D:\\workspace\\workspace for idea\\es_hbase\\hadoopmapreduce\\src\\main\\resources\\input\\MaxTemperature\\1901";
    String path2 = "D:\\workspace\\workspace for idea\\es_hbase\\hadoopmapreduce\\src\\main\\resources\\input\\MaxTemperature\\1902";

    File[] file = new File[2];
    file[0] = new File(path1);
    file[1] = new File(path2);

    for(int i = 0 ; i < file.length ; i++){
      try {
        BufferedReader reader = new BufferedReader(new FileReader(file[i]));
        String line = null;
        while ( (line = reader.readLine()) != null) {
          Text value = new Text(line);
//          new MapDriver<LongWritable,Text,Text,IntWritable>()
//                  .withMapper(new MaxTemperatureMapper())
//                  .withInput(new LongWritable(0),value)
//                  .withOutput(new Text("1901"), new IntWritable(-11))
//                  .runTest();
        }
        reader.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }
}
