package com.hjh.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;

import java.io.*;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-06
 * @Description:通过fileSystem访问hdfs文件
 */
public class FileSystemUtil {

  public static FileSystem getFileSystem(String path){
    FileSystem fs = null;
    try {
      fs = FileSystem.get(URI.create(path),new Configuration());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return fs;
  }

  public static void readFile(String path){
    FileSystem fs = getFileSystem(path);
    InputStream in = null;
    try {
      in = fs.open(new Path(path));
      IOUtils.copyBytes(in,System.out,4096,false);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeStream(in);
    }
  }

  public static void doubleReadFile(String path) {
    FileSystem fs = getFileSystem(path);
    FSDataInputStream in = null;
    try {
      in = fs.open(new Path(path));
      IOUtils.copyBytes(in,System.out,4096,false);
      in.seek(0);
      IOUtils.copyBytes(in,System.out,4096,false);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeStream(in);
    }
  }

  public static void copyToLocal(String srcPath,String dstPath) {
    FileSystem fs = getFileSystem(srcPath);
    InputStream in = null;
    OutputStream out = null;
    try {
      in = fs.open(new Path(srcPath));
      out = new BufferedOutputStream(new FileOutputStream(dstPath));
      IOUtils.copyBytes(in,out,4096,false);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeStream(in);
      IOUtils.closeStream(out);
    }
  }

  public static void copyFormLocal(String srcPath,String dstPath) {
    FileSystem fs = getFileSystem(dstPath);
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new BufferedInputStream(new FileInputStream(srcPath));
      out = fs.create(new Path(dstPath), new Progressable() {
        @Override
        public void progress() {
          System.out.print(".");
        }
      });
      IOUtils.copyBytes(in,out,4096,false);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeStream(in);
      IOUtils.closeStream(out);
    }
  }

  public static void main(String[] args) {
    FileSystemUtil.readFile(Constant.filepath);
    FileSystemUtil.doubleReadFile(Constant.filepath);
    FileSystemUtil.copyToLocal(Constant.filepath,Constant.localOutPath);
    FileSystemUtil.copyFormLocal(Constant.localOutPath,Constant.filepath);
  }
}
