package com.hjh.hdfs;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-06
 * @Description:
 */
public class ListStatus {

  public static void listStatus(String[] path) {
    try {
      FileSystem fs = FileSystem.get(Constant.getConf());
      Path[] paths = new Path[path.length];
      for (int i = 0 ; i < paths.length ; i++) {
        paths[i] = new Path(path[i]);
      }
      FileStatus[] fStatus = fs.listStatus(paths);
      //fStatus数组转成Path数组 绝对路径
      Path[] listedPaths = FileUtil.stat2Paths(fStatus);
      for (Path p : listedPaths) {
        System.out.println(p);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    String[] path = {
            "/",
            "/user/hive/warehouse",
            "/mapreduce/MaxTemperature/input"
    };
    ListStatus.listStatus(path);
  }
}
