package com.hjh.mapreduce;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-06
 * @Description: 输入 /mapreduce/MaxTemperature/input
 * 输出 /mapreduce/MaxTemperature/output
 * 调用:hadoop jar hadoopmapreducer.jar com.hjh.mapreduce.MaxTemperature /mapreduce/MaxTemperature/input /mapreduce/MaxTemperature/output
 */
public class MaxTemperature {

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("必须输入 <input path> <outer path>");
    }
    try {
      Job job = Job.getInstance();
      job.setJarByClass(MaxTemperature.class);
      job.setJobName("MaxTemperature");
      job.setMapperClass(MaxTemperatureMapper.class);
      job.setCombinerClass(MaxTemperatureCombiner.class);
      job.setReducerClass(MaxTemperatureReducer.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(IntWritable.class);
      //默认是 TextInputFormat
      job.setInputFormatClass(TextInputFormat.class);
      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));
      // true 表示 打印详细mapreduce进度
      System.exit(job.waitForCompletion(true) ? 0 : 1);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}

class MaxTemperatureMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
  private static final  int MISSING = 9999;
  @Override
  protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    String line = value.toString();
    String year = line.substring(15,19);
    int temperature;
    if (line.charAt(87)== '+' ) {
      //正数不带 +
      temperature = Integer.parseInt(line.substring(88,92));
    } else {
      // - 负数
      temperature = Integer.parseInt(line.substring(87,92));
    }
    String quality = line.substring(92,93);
    if (temperature != MISSING && quality.matches("[01459]")) {
      context.write(new Text(year), new IntWritable(temperature));
    }
  }
}

class MaxTemperatureCombiner extends Reducer<Text,IntWritable,Text,IntWritable>{
  @Override
  protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
    int maxTemperature = Integer.MIN_VALUE;
    for (IntWritable t : values) {
      maxTemperature = Math.max(maxTemperature,t.get());
    }
    context.write(key, new IntWritable(maxTemperature));
  }
}

class MaxTemperatureReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
  @Override
  protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
    int maxTemperature = Integer.MIN_VALUE;
    for (IntWritable t : values) {
      maxTemperature = Math.max(maxTemperature,t.get());
    }
    context.write(key, new IntWritable(maxTemperature));
  }
}

