package com.hjh.elasticsearch;

import com.hjh.entity.Doc;
import com.hjh.properties.PropertiesLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-07-19
 * @Description:
 */
public class ElasticSearchUtil {
  public static final Log LOG = LogFactory.getLog(ElasticSearchUtil.class.getName());
  protected static TransportClient client;
  private static String es_node1_host = "";
  private static String es_node1_port = "";
  static {
    String userdir = System.getProperty("user.dir") + File.separator + "conf" + File.separator;
    String configName = "file:/"+userdir + "hbase_elasticsearch.properties";
    PropertiesLoader propertiesLoader = new PropertiesLoader(configName);
    try {
      es_node1_host = propertiesLoader.getProperty("es_node1_host");
      es_node1_port = propertiesLoader.getProperty("es_node1_port");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

    public ElasticSearchUtil() {
      Settings esSettings = Settings.builder()
              .put("cluster.name", "hadoop-es") //设置ES实例的名称
              .put("client.transport.sniff", true) //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
              .build();
      /**
       * 这里的连接方式指的是没有安装x-pack插件,如果安装了x-pack则参考{@link ElasticsearchXPackClient}
       * 1. java客户端的方式是以tcp协议在9300端口上进行通信
       * 2. http客户端的方式是以http协议在9200端口上进行通信
       */
      try {
        client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new TransportAddress(InetAddress.getByName(es_node1_host), Integer.parseInt(es_node1_port)));
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
      System.out.println("ElasticsearchClient 连接成功");
    }

  public ElasticSearchUtil(String es_host,String es_port) {
    Settings esSettings = Settings.builder()
            .put("cluster.name", "hadoop-es") //设置ES实例的名称
            .put("client.transport.sniff", true) //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
            .build();
    try {
      client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new TransportAddress(InetAddress.getByName(es_host), Integer.parseInt(es_port)));
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    System.out.println("ElasticsearchClient 连接成功");
  }

  /**
   * 关闭连接
   */
  public void close(){
    if(client!=null){
      client.close();
    }
  }

  public String  addIndex(String index,String type,Doc doc){
    IndexResponse response = client.prepareIndex(index, type).setSource(doc.getHashMap()).execute().actionGet();
    return response.getId();
  }

  public static Map<String, Object> search(String key, String index, String type, int start, int row){
    HighlightBuilder highlightBuilder = new HighlightBuilder();
    //设置高亮前缀
    highlightBuilder.preTags("<font color='red' >");
    //设置高亮后缀
    highlightBuilder.postTags("</font>");
    //设置高亮字段名称
    highlightBuilder.field("title");
    highlightBuilder.field("time");

    SearchRequestBuilder builder = client.prepareSearch(index);
    builder.setTypes(type);
    builder.setFrom(start);
    builder.setSize(row);
    builder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
    if(StringUtils.isNotBlank(key)){
//			builder.setQuery(QueryBuilders.termQuery("title",key));
      builder.setQuery(QueryBuilders.multiMatchQuery(key, "title","time"));
    }
    builder.setExplain(true);
    SearchResponse searchResponse = builder.get();

    SearchHits hits = searchResponse.getHits();
    long total = hits.getTotalHits();
    Map<String, Object> map = new HashMap<>();
    SearchHit[] hits2 = hits.getHits();
    map.put("count", total);
    List<Map<String, Object>> list = new ArrayList<>();
    for (SearchHit searchHit : hits2) {
      Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
      HighlightField highlightField = highlightFields.get("title");
      Map<String, Object> source = searchHit.getSourceAsMap();
      if(highlightField!=null){
        Text[] fragments = highlightField.fragments();
        String title = "";
        for (Text text : fragments) {
          title+=text;
        }
        source.put("title", title);
      }
      HighlightField highlightField2 = highlightFields.get("time");
      if(highlightField2!=null){
        Text[] fragments = highlightField2.fragments();
        String time = "";
        for (Text text : fragments) {
          time+=text;
        }
        source.put("time", time);
      }
      list.add(source);
    }
    map.put("dataList", list);
    System.out.println(map);
    return map;
  }


  /**
   * 从索引库获取数据
   * @return void
   */
  public void getData(String index,String type,String id) {
    GetResponse getResponse = client.prepareGet(index,type, id).get();
    LOG.info("索引库的数据:" + getResponse.getSourceAsString());
  }

  public static void main(String[] args) {
    ElasticSearchUtil esUtil = new ElasticSearchUtil();
//    esUtil.getData("tfjt","doc","1");
    ElasticSearchUtil.search("maven","tfjt","doc",1,3);
  }
}
