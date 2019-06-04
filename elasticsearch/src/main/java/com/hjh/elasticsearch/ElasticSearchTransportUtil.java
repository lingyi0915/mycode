package com.hjh.elasticsearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-28
 * @Description:
 */
public class ElasticSearchTransportUtil {
  private static final Log log = LogFactory.getLog(ElasticSearchTransportUtil.class);
  private static final String DEFAULT_ES_PORT = "9300";
  private static final String configFile = "hbase_elasticsearch.properties";
  private TransportClient client = null;
  private String esHost = null;
  private String esPort = null;
  private Properties prop = null;
  private static ElasticSearchTransportUtil helper = new ElasticSearchTransportUtil();
  /**
   * 私有化构造方法，单例对象 创建连接客户端
   */
  private ElasticSearchTransportUtil() {
    Settings esSettings = Settings.builder()
            //设置ES实例的名称
            .put("cluster.name", "hadoop-es")
            //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
            .put("client.transport.sniff", true)
            .build();
    InputStream in = null;
    /*
     * 这里的连接方式指的是没有安装x-pack插件,如果安装了x-pack则参考{@link ElasticsearchXPackClient}
     * 1. java客户端的方式是以tcp协议在9300端口上进行通信
     * 2. http客户端的方式是以http协议在9200端口上进行通信
     */
    try {
//      String configName = ElasticSearchRestUtil.class.getResource("/"+configFile)
//              .toString().replaceAll("file:/","").replaceAll("%20"," ");
      String configName = System.getProperty("user.dir")+"/conf/"+configFile;
      System.out.println(configName);
      prop = new Properties();
      in = new FileInputStream(configName);
      prop.load(in);
      esHost = prop.getProperty("es_node1_host");
      esPort = prop.getProperty("es_transport_port");
      if(esHost == null) {
        System.out.println("es连接节点ip不能为空");
        System.exit(-1);
      }
      if(esPort == null) {
        esPort = DEFAULT_ES_PORT;
      }
      client = new PreBuiltTransportClient(Settings.EMPTY)
              .addTransportAddress(new TransportAddress(InetAddress.getByName(esHost), Integer.parseInt(esPort)));
      System.out.println("ElasticsearchClient 连接成功");
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
  }

  /**
   * 单例对象
   * @return 返回单例对象
   */
  public static ElasticSearchTransportUtil getHelper(){
    return helper;
  }

  /**
   * 关闭连接
   */
  public void close(){
    if(client!=null) {
      client.close();
    }
  }

  public TransportClient getClient() {
    return client;
  }

  public void setClient(TransportClient client) {
    this.client = client;
  }

  public String getEsHost() {
    return esHost;
  }

  public void setEsHost(String esHost) {
    this.esHost = esHost;
  }

  public String getEsPort() {
    return esPort;
  }

  public void setEsPort(String esPort) {
    this.esPort = esPort;
  }

  public Properties getProp() {
    return prop;
  }

  public void setProp(Properties prop) {
    this.prop = prop;
  }

  public void deleteByQueryTest(){
    BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
            .filter(QueryBuilders.matchQuery("user", "kimchy2"))
            .source("posts")
            .get();
    long deleted = response.getDeleted();
  }

  public void deleteByQuery(String index, QueryBuilder filter){
    BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
            .filter(filter)
            .source(index)
            .get();
    long deleted = response.getDeleted();
  }

}
