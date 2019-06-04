package com.hjh.hdfs;

import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;
import org.apache.hadoop.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-06
 * @Description: 通过url访问hdfs文件
 */
public class URLFileSystemUtil {

  // url默认不识别 hdfs协议
  // 需要静态初始化Factory
  static {
    URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
  }


  public static void readFile(String path) {
    InputStream in = null;
    try {
      in = new URL(path).openStream();
      IOUtils.copyBytes(in,System.out,4096,false);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeStream(in);
    }
  }

  public static void main(String[] args) {
    URLFileSystemUtil.readFile(Constant.filepath);
  }
}
