package com.hjh.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Vector;

/**
 * @Author: hjh
 * @Create: 2019/3/28
 * @Description:
 *  实现mapreduce map join 加载一张表到内存，另一张表做连接
 */
public class CustomMapRedJoin  extends Configured implements Tool {

    private static String joinKeyDelimiter = "_";
    private static String fieldDelimiter = "\t";

    private static class CustomMapper extends Mapper<LongWritable, Text,Text,Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            //获得输入路径，区分数据是表A还是表B的
            String filePath = ((FileSplit)context.getInputSplit()).getPath().toString();

            if(value == null) return ;
            String line = value.toString();
            String [] fields = line.split(fieldDelimiter);
            String joinKey = "";
            String joinValue = "";
            //如果数据来源于表A
            if(filePath.contains("table_a")){
                if(fields.length<3){
                    return;
                }
                joinKey = fields[1]+joinKeyDelimiter+fields[2];
                joinValue = "a#"+fieldDelimiter+fields[0];
            }else if(filePath.contains("table_b")){
                if(fields.length<3){
                    return;
                }
                joinKey = fields[1]+joinKeyDelimiter+fields[2];
                joinValue = "b#"+fieldDelimiter+fields[0];
            }
            System.out.println("key:"+joinKey+",value:"+joinValue);
            context.write(new Text("left#"+joinKey),new Text(joinValue));
            context.write(new Text("right#"+joinKey),new Text(joinValue));
            context.write(new Text("full#"+joinKey),new Text(joinValue));
        }
    }

    private static class CustomPartiiton extends Partitioner<Text,Text>{

        @Override
        public int getPartition(Text text, Text text2, int i) {
            String line = text.toString();
            if(line.startsWith("left#")){
                return 0;
            }else if(line.startsWith("right#")){
                return 1;
            }else if(line.startsWith("full#")){
                return 2;
            }
            return 0;
        }
    }

    private static class CustomReduce extends Reducer<Text, Text,Text,Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            Vector<String> tableA = new Vector<>();
            Vector<String> tableB = new Vector<>();



            values.forEach((text)->{
                    String line = text.toString();
                    if(line.startsWith("a#")){
                        tableA.add(line.substring(2));
                    }else if(line.startsWith("b#")){
                        tableB.add(line.substring(2));
                    }
                });

            int lenA = tableA.size();
            int lenB = tableB.size();

            //两个表都有数据的 保留
            for(int i = 0 ; i < lenA ; i++){
                for(int j = 0 ; j < lenB ; j++){
                    System.out.println("join:"+tableA.get(i)+fieldDelimiter+tableB.get(j));
                    Text res = new Text(tableA.get(i)+fieldDelimiter+tableB.get(j));
                    context.write(key,res);
                }
            }

            String joinKey = key.toString();
            if(joinKey.startsWith("left#")){
                //左连接  tableB没有数据，但是tableA保留
                for(int i = 0 ; i < lenA ; i++){
                    System.out.println("left:"+tableA.get(i));
                    Text res = new Text(tableA.get(i));
                    context.write(key,res);
                }
            }else if(joinKey.startsWith("right#")){

                //右连接  tableA没有数据，但是tableB保留
                for(int j = 0 ; j < lenB ; j++){
                    System.out.println("right:"+fieldDelimiter+fieldDelimiter+tableB.get(j));
                    Text res = new Text(fieldDelimiter+fieldDelimiter+tableB.get(j));
                    context.write(key,res);
                }
            }else if(joinKey.startsWith("full#")){
                //左连接  tableB没有数据，但是tableA保留
                for(int i = 0 ; i < lenA ; i++){
                    System.out.println("full:"+tableA.get(i));
                    Text res = new Text(tableA.get(i));
                    context.write(key,res);
                }
                //右连接  tableA没有数据，但是tableB保留
                for(int j = 0 ; j < lenB ; j++){
                    System.out.println("full:"+fieldDelimiter+fieldDelimiter+tableB.get(j));
                    Text res = new Text(fieldDelimiter+fieldDelimiter+tableB.get(j));
                    context.write(key,res);
                }
            }
        }
    }

    @Override
    public int run(String[] strings) throws Exception {
        Configuration conf = getConf();
        Job job = Job.getInstance(conf,"map reduce join");

        job.setJarByClass(CustomMapRedJoin.class);

        job.setMapperClass(CustomMapper.class);
        job.setPartitionerClass(CustomPartiiton.class);
        job.setReducerClass(CustomReduce.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setNumReduceTasks(3);

        //，分割多个 路径
        FileInputFormat.addInputPaths(job,strings[0]);
//        for(int i = 0 ; i < strings.length - 1 ; i++){
//            //前面全是输入目录
//            FileInputFormat.addInputPath(job,new Path(strings[i]));
//        }
        //最后一个是输出目录
        FileOutputFormat.setOutputPath(job,new Path(strings[strings.length-1]));

        boolean res = job.waitForCompletion(true);

        return res ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            new IllegalArgumentException("Usage: <inpath> <outpath>");
            return;
        }
        ToolRunner.run(new Configuration(), new CustomMapRedJoin(), args);
    }
}
