package com.hjh.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-08-06
 * @Description:
 */
public class world_alcohol {
  public static class worldAlcoholMapper extends Mapper<LongWritable,Text,Text,IntWritable> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      System.out.println(key);
      System.out.println(value);
      System.out.println(context);
    }
  }
}
