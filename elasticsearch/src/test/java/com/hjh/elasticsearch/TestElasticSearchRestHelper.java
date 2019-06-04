package com.hjh.elasticsearch;

import com.hjh.elasticsearch.ElasticSearchRestHelper;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-28
 * @Description: 测试helper类
 */
public class TestElasticSearchRestHelper {
  private static ElasticSearchRestHelper helper = ElasticSearchRestHelper.getHelper();
  private static ElasticSearchTransportUtil transportUtil = ElasticSearchTransportUtil.getHelper();

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
  private String index = "test";
  private String type = "doc";
  private List<Map<String,String>> logData = new ArrayList<>();

  public void readData() {
    File file = new File("D:\\workspace\\测试数据\\pcac.access_2017-02-27.log");
    String dt = file.getName().substring(12,22);
    System.out.println(dt);
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      while((line = reader.readLine()) != null) {
        String[] fieldValues =line.split(" ");
        Map<String, String> jsonMap = new HashMap<>();
        for (int i = 0 ; i < fieldNames.length ; i++){
          if(fieldNames[i].equals("request")){
            fieldValues[i] = URLDecoder.decode(fieldValues[i],"UTF-8").replaceAll("\"","'");
          }
          jsonMap.put(fieldNames[i],fieldValues[i]);
        }
        jsonMap.put("dt",dt);
        logData.add(jsonMap);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public long indexStrTimeTest(int size){

    deleteByIndex(index);

    long st = System.currentTimeMillis();



    readData();
    int length = logData.size();
    System.out.println(logData.size());

    for(int i = 0 ; i < length ; i++){
      try {
        helper.put(index,type,i+"",logData.get(i));
      } catch (Exception e) {
        System.out.println(logData.get(i));
        e.printStackTrace();
        System.exit(-1);
      }
    }
    long et = System.currentTimeMillis();
    System.out.println("耗时:"+(et-st)+"ms");
    return et- st;
  }

  /**
   * 关闭连接
   */
  public void close(){
    if(helper !=null){
        helper.close();
    }
  }

  public void deleteByIndex(String index){
    try {
      transportUtil.deleteByQuery(index,QueryBuilders.matchAllQuery());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteByQuery(String index,String field,String value){
    try {
      transportUtil.deleteByQuery(index,QueryBuilders.matchQuery(field,value));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    TestElasticSearchRestHelper testHelper = new TestElasticSearchRestHelper();

    try {
      long st = System.currentTimeMillis();
//      testHelper.indexStrTimeTest(10);
      testHelper.deleteByQuery("test","dt","2017-02-27");
      long et = System.currentTimeMillis();
      System.out.println("总耗时:"+(et-st)+"ms");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      testHelper.close();
    }

  }
}
