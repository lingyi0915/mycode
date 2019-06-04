package com.hjh.elasticsearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-26
 * @Description: elasticsearch 6.4.1 helper类
 */
public class ElasticSearchRestHelper {
  private static final Log log = LogFactory.getLog(ElasticSearchRestHelper.class);
  private static final String DEFAULT_ES_PORT = "9200";
  private static final String configFile = "config.properties";
  private static final String PUT = "PUT";
  private static final String POST = "POST";
  private static final String GET = "GET";
  private static final String HEAD = "HEAD";
  private static final String DELETE = "DELETE";

  private RestHighLevelClient client = null;
  private String esHost = null;
  private String esPort = null;
  private Properties prop = null;
  private static ElasticSearchRestHelper helper = new ElasticSearchRestHelper();

  private void init() {

  }

  /**
   * 私有化构造方法，单例对象 创建连接客户端
   */
  private ElasticSearchRestHelper() {
    InputStream in = null;
    /*
     * 这里的连接方式指的是没有安装x-pack插件,如果安装了x-pack则参考{@link ElasticsearchXPackClient}
     * 1. java客户端的方式是以tcp协议在9300端口上进行通信
     * 2. http客户端的方式是以http协议在9200端口上进行通信
     */
    try {
      String configName = ElasticSearchRestHelper.class.getResource("/"+configFile)
              .toString().replaceAll("file:/","").replaceAll("%20"," ");
      System.out.println(configName);
      prop = new Properties();
      in = new FileInputStream(configName);
      prop.load(in);
      esHost = prop.getProperty("es_host");
      esPort = prop.getProperty("es_rest_port");
      if(esHost == null) {
        System.out.println("es连接节点ip不能为空");
        System.exit(-1);
      }
      if(esPort == null) {
        esPort = DEFAULT_ES_PORT;
      }
      System.out.println("http://"+esHost+"："+esPort);
      client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost,Integer.parseInt(esPort))));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    } finally {
      try {
        if(in != null){
          in.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    System.out.println("ElasticsearchClient 连接成功");
  }

  public IndexResponse put(String index, String type, String id, String json) throws Exception {
    IndexRequest request = new IndexRequest(index, type, id);
    request.source(json, XContentType.JSON);
    return client.index(request, RequestOptions.DEFAULT);
  }

  public IndexResponse put(String index, String type, String id, Map json) throws Exception {
    IndexRequest request = new IndexRequest(index, type, id);
    request.source(json);
    return client.index(request, RequestOptions.DEFAULT);
  }

  public IndexResponse put(String index, String type, String id, XContentBuilder json) throws Exception {
    IndexRequest request = new IndexRequest(index, type, id);
    request.source(json);
    return client.index(request, RequestOptions.DEFAULT);
  }

  public GetResponse get(String index, String type, String id) throws Exception {
    // 创建请求
    GetRequest getRequest = new GetRequest(index,type,id);
    return client.get(getRequest, RequestOptions.DEFAULT);
  }

  public UpdateResponse upsert(String index, String type, String id, String json) throws Exception {
    UpdateRequest request = new UpdateRequest(index, type, id);
    request.upsert(json,XContentType.JSON);
    return client.update(request, RequestOptions.DEFAULT);
  }

  public UpdateResponse upsert(String index, String type, String id, Map json) throws Exception {
    UpdateRequest request = new UpdateRequest(index, type, id);
    request.upsert(json);
    return client.update(request, RequestOptions.DEFAULT);
  }

  public UpdateResponse upsert(String index, String type, String id, XContentBuilder json) throws Exception {
    UpdateRequest request = new UpdateRequest(index, type, id);
    request.upsert(json);
    return client.update(request, RequestOptions.DEFAULT);
  }

  public UpdateResponse update(String index, String type, String id, String json) throws Exception {
    UpdateRequest request = new UpdateRequest(index, type, id);
    request.doc(json,XContentType.JSON);
    return client.update(request, RequestOptions.DEFAULT);
  }

  public UpdateResponse update(String index, String type, String id, Map json) throws Exception {
    UpdateRequest request = new UpdateRequest(index, type, id);
    request.doc(json);
    return client.update(request, RequestOptions.DEFAULT);
  }

  public UpdateResponse update(String index, String type, String id, XContentBuilder json) throws Exception {
    UpdateRequest request = new UpdateRequest(index, type, id);
    request.doc(json);
    return client.update(request, RequestOptions.DEFAULT);
  }

  public DeleteResponse delete(String index, String type, String id) throws Exception {
    DeleteRequest request = new DeleteRequest(index,type,id);
    return client.delete(request, RequestOptions.DEFAULT);
  }

  public boolean exists(String index, String type, String id) throws Exception {
    GetRequest request = new GetRequest(index,type,id);
    return client.exists(request, RequestOptions.DEFAULT);
  }

  public BulkResponse bulk(DocWriteRequest[] requests) throws Exception {
    BulkRequest bulkRequest = new BulkRequest();
    bulkRequest.add(requests);
    return client.bulk(bulkRequest, RequestOptions.DEFAULT);
  }

  /**
   * 单例对象
   * @return 返回单例对象
   */
  public static ElasticSearchRestHelper getHelper(){
    return helper;
  }

  public String getEsHost() {
    return esHost;
  }

  public void setEsHost(String es_host) {
    this.esHost = es_host;
  }

  public String getEsPort() {
    return esPort;
  }

  public void setEsPort(String es_port) {
    this.esPort = es_port;
  }

  public Properties getProp() {
    return prop;
  }

  public void setProp(Properties prop) {
    this.prop = prop;
  }

  /**
   * 关闭连接
   */
  public void close(){
    if(client!=null){
      try {
        client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }




}
