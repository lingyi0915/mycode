package com.hjh.elasticsearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.profile.ProfileResult;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.elasticsearch.search.profile.aggregation.AggregationProfileShardResult;
import org.elasticsearch.search.profile.query.CollectorResult;
import org.elasticsearch.search.profile.query.QueryProfileShardResult;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestion;
import org.w3c.dom.ranges.Range;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import static java.util.Collections.singletonMap;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-26
 * @Description:
 */
public class ElasticSearchRestUtil {
  private static final Log log = LogFactory.getLog(ElasticSearchRestUtil.class);
  private static final String DEFAULT_ES_PORT = "9200";
  private static final String configFile = "hbase_elasticsearch.properties";
  private static final String PUT = "PUT";
  private static final String POST = "POST";
  private static final String GET = "GET";
  private static final String HEAD = "HEAD";
  private static final String DELETE = "DELETE";

  private RestHighLevelClient client = null;
  private String esHost = null;
  private String esPort = null;
  private Properties prop = null;
  private static ElasticSearchRestUtil helper = new ElasticSearchRestUtil();

  /**
   * 私有化构造方法，单例对象 创建连接客户端
   */
  private ElasticSearchRestUtil() {
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
      esPort = prop.getProperty("es_node1_port");
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

  public void putRefreshTest(String index, String type, String id, String json){
    IndexRequest request = new IndexRequest(index, type, id);
    request.source(json, XContentType.JSON);
    request.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
    try {
      // 6.4.1 新增了RequestOptions
      IndexResponse response = client.index(request, RequestOptions.DEFAULT);
      GetRequest request1 = new GetRequest(index, type, id);

      GetResponse response1 = client.get(request1, RequestOptions.DEFAULT);

      if (response1.isExists()) {
        System.out.println(response1.getSourceAsString());
      }

    } catch (IOException e) {
      e.printStackTrace();
    }


  }

  public void put(String index, String type, String id, String json) {
    IndexRequest request = new IndexRequest(index, type, id);
    request.source(json, XContentType.JSON);
    DocWriteResponse indexResponse = null;
    try {
      indexResponse = client.index(request, RequestOptions.DEFAULT);
      // 返回结果response解析
      responseInfo(indexResponse);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void putTest(String index, String type, String id, String json){
    IndexRequest request = new IndexRequest(index, type, id);
    request.source(json, XContentType.JSON);

    /*   下列参数可选  */

    /*
    在ElaticSearch里面，路由功能算是一个高级用法，大多数时候我们用的都是系统默认的路由功能，
    我们知道一个es索引可以分多个shard和每个shard又可以有多个replia，那么现在思考一个问题，
    我们添加进去的数据，是如何分布在各个shard上面的，而查询时候它是又怎么找到特定的数据呢。

    默认情况下，索引数据的分片规则，是下面的公式：
    shard_num = hash(_routing) % num_primary_shards

    _routing字段的取值，默认是_id字段或者是_parent字段，
    这样的取值在hash之后再与有多少个shard的数量取模，
    最终得到这条数据应该在被分配在那个一个shard上，也就是说默认是基于hash的分片，
    保证在每个shard上数据量都近似平均，这样就不会出现负载不均衡的情况，
    然后在检索的时候，es默认会搜索所有shard上的数据，
    最后在master节点上汇聚在处理后，返回最终数据。
     */
    //路由值
    request.routing("routing");

    /*
    Parent-Child主要有下面的几个特点：

  （1） 父文档可以被更新，而无须重建所有的子文档

  （2）子文档的添加，修改，或者删除不影响它的父文档和其他的子文档，
  这尤其是在子文档数量巨大而且需要被添加和更新频繁的场景下Parent-Child能获取更好的性能

  （3）子文档可以被返回在搜索结果里面
   */
    // 父值？？？
    request.parent("parent");

    /* 超时时间的意义:
      指定timeout，就能在timeout时间范围内，将搜索到的部分数据（也可能全部都搜索到），
      直接返回给client，而不是所有数据搜索到再返回，可以为一些敏感的搜索应用提供良好的支持。
    */
    // 超时时间 通过方法设置
    request.timeout(TimeValue.timeValueSeconds(1));
    // 直接设置
    request.timeout("1s");

    /* 刷新策略分3种
      NONE("false"),// 不刷新 无
      IMMEDIATE("true"),立即刷新，
      WAIT_UNTIL("wait_for");  等待自动刷新
      区别是 假设当前集群有500条记录，通过request插入一条记录，在刷新时间未到以前，你获取记录数依然是500(应该是501)，
      设置为 立即刷新，返回的记录数为501，
      设置为 wait_for 等待刷新时间到了自动刷新

      elasticsearch.yml文件中
      index.refresh_interval:1s  刷新时间默认 1s

    */
    // 设置 刷新策略
    request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
    request.setRefreshPolicy("wait_for");

    /*
    ElasticSearch是分布式的，当文档创建、更新、删除时，新版本的文档必须复制到集群中其他节点，
    同时，ElasticSearch也是异步和并发的。这就意味着这些复制请求被并行发送，
    并且到达目的地时也许顺序是乱的(老版本可能在新版本之后到达)。
    ElasticSaerch需要一种方法确保文档的旧版本不会覆盖新的版本：
    ES利用_version (版本号)的方式来确保应用中相互冲突的变更不会导致数据丢失。
    需要修改数据时，需要指定想要修改文档的version号，如果该版本不是当前版本号，请求将会失败。
    */
    // 版本
    request.version(2);

    // 版本类型
    /**
     * INTERNAL(0)
     * EXTERNAL(1)
     * EXTERNAL_GTE(2)
     * FORCE(3) @Deprecated 强制 已经过时
     *
     */
    request.versionType(VersionType.EXTERNAL);

    // 操作类型
    // 如果不指定创建类型，则自动判断 新增是create，如果数据存在则为 update （版本号为变更）
    request.opType(DocWriteRequest.OpType.CREATE);
    request.opType("create");

    /* 该操作暂不清楚 */
    //  设置管道名称
    request.setPipeline("pipeline");

    /*   上述参数可选  */


      //  阻塞提交
    DocWriteResponse indexResponse = null;
    try {
      indexResponse = client.index(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // 如果存在版本冲突，将弹出一个弹性搜索异常： 指定版本已经存在，会报冲突异常
    IndexRequest req1 = new IndexRequest("posts", "doc", "1")
            .source("field", "value")
            .version(1);
    try {

      IndexResponse response = client.index(req1,RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    } catch(ElasticsearchException e) {
      if (e.status() == RestStatus.CONFLICT) {

      }
    }

    // 当指定类型为create  并且创建了一个index，type，id相同的数据的时候，报冲突异常
    IndexRequest req = new IndexRequest("posts", "doc", "1")
            .source("field", "value")
            .opType(DocWriteRequest.OpType.CREATE);
    try {
      IndexResponse response = client.index(req,RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    } catch(ElasticsearchException e) {
      if (e.status() == RestStatus.CONFLICT) {

      }
    }

    // 返回结果response解析
    responseInfo(indexResponse);

      // 异步提交监听方法
      ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
        @Override
        public void onResponse(IndexResponse indexResponse) {

        }

        @Override
        public void onFailure(Exception e) {

        }
      };
      // 异步提交 6.4.1 多一个参数 RequestOptions.DEFAULT
      client.indexAsync(request, RequestOptions.DEFAULT, listener);
  }

  // 这里包括 3个response  Index Update Detele
  public void responseInfo(DocWriteResponse response){
    // 返回内容response 解析
    // 获取index
    String index = response.getIndex();
    // 获取类型
    String type = response.getType();
    // 获取id
    String id = response.getId();
    // 获取version数
    long version = response.getVersion();

    // 获取结果类型，判断是否是 CREATED 或 DELETED 或 NOT_FOUND 或 UPDATED 或 NOOP
    if (response.getResult() == DocWriteResponse.Result.CREATED) {
      // 第一次创建文档的情况（如果需要的话）
      // 如果是创建  返回的是 indexResponse
    } else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
      // 处理（如果需要的话）文档已经被重写的情况，因为它已经存在
      // 如果是更新 返回的是 UpdateResponse
    }

    // 获取该数据存放的碎片信息
    ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();

    //  处理成功碎片数量少于碎片总数的情况
    if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

    }

    // 处理潜在的故障
    if (shardInfo.getFailed() > 0) {
      for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
        String reason = failure.reason();
      }
    }
  }

  public void get(String index, String type, String id) {
    // 创建请求
    GetRequest getRequest = new GetRequest(index,type,id);
    String[] includes = new String[]{"message", "*Date"};
    String[] excludes = Strings.EMPTY_ARRAY;
    FetchSourceContext fetchSourceContext =
            new FetchSourceContext(true, includes, excludes);
    getRequest.fetchSourceContext(fetchSourceContext);

    GetResponse getResponse = null;
    try {

      getResponse = client.get(getRequest,RequestOptions.DEFAULT);

      String index2 = getResponse.getIndex();
      String type2 = getResponse.getType();
      String id2 = getResponse.getId();
      if (getResponse.isExists()) {
        long version = getResponse.getVersion();
        String sourceAsString = getResponse.getSourceAsString();
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        byte[] sourceAsBytes = getResponse.getSourceAsBytes();

        for ( String field : sourceAsMap.keySet()) {
          System.out.println(sourceAsMap.get(field).getClass().getName());
          System.out.println(field+"："+sourceAsMap.get(field));
        }

      } else {

      }



    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void getTest(String index, String type, String id){
    // 创建请求
    GetRequest getRequest = new GetRequest(index,type,id);

    /*   下列参数可选  */

    // 禁用源检索，默认启用
    getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);

    getRequest.routing("routing");

    getRequest.parent("parent");

    // 偏爱值？
    getRequest.preference("preference");

    getRequest.realtime(false);

    getRequest.refresh(true);

    getRequest.version(2);

    getRequest.versionType(VersionType.EXTERNAL);

    /*   上述参数可选  */

    /* 为特定字段配置源排除 源包括  */
    String[] includes = new String[]{"message", "*Date"};
    String[] excludes = Strings.EMPTY_ARRAY;
    FetchSourceContext fetchSourceContext =
            new FetchSourceContext(true, includes, excludes);
    getRequest.fetchSourceContext(fetchSourceContext);


    // 为特定存储字段配置检索（需要在映射中单独存储字段）可以通过getField获取，否则获取不出来
    getRequest.storedFields("message");
    GetResponse getResponse = null;
    try {
      getResponse = client.get(getRequest,RequestOptions.DEFAULT);

      String index2 = getResponse.getIndex();
      String type2 = getResponse.getType();
      String id2 = getResponse.getId();
      if (getResponse.isExists()) {
        long version = getResponse.getVersion();
        String sourceAsString = getResponse.getSourceAsString();
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        byte[] sourceAsBytes = getResponse.getSourceAsBytes();

        // 检索消息存储字段（要求字段在映射中单独存储）
        DocumentField messageField = getResponse.getField("message");
        DocumentField dateField = getResponse.getField("postDate");

        System.out.println(messageField+":"+dateField.toString());

        String message = getResponse.getField("message").getValue();
        String date = getResponse.getField("postDate").getValue();
        System.out.println(message+","+date);

      } else {

      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ElasticsearchException e) {
      // not found
      if (e.status() == RestStatus.NOT_FOUND) {

      }
    }

    try {
      // 如果已请求特定文档版本，并且现有文档具有不同的版本号，则引发版本冲突：
      // 和最新版本号不同 返回冲突
      GetRequest request = new GetRequest("posts", "doc", "1").version(2);
      getResponse = client.get(request,RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ElasticsearchException exception) {
      if (exception.status() == RestStatus.CONFLICT) {

      }
    }

    String message = getResponse.getField("message").getValue();
    System.out.println(message);
  }

  public void existsTest(String index, String type, String id){
    // 创建请求
    GetRequest getRequest = new GetRequest(index,type,id);
    getRequest.fetchSourceContext(new FetchSourceContext(false));
    getRequest.storedFields("_none_");

    // 阻塞式
    try {
      boolean exists = client.exists(getRequest,RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // 匿名内部类 继承ActionListener
    ActionListener<Boolean> listener = new ActionListener<Boolean>() {
      @Override
      public void onResponse(Boolean exists) {

      }

      @Override
      public void onFailure(Exception e) {

      }
    };
    // 异步式，监听事件
    client.existsAsync(getRequest, RequestOptions.DEFAULT, listener);
  }

  public void deleteTest(String index, String type, String id){
    DeleteRequest request = new DeleteRequest(index,type,id);

    /*可选参数*/
    request.routing("routing");
    request.parent("parent");
    request.timeout(TimeValue.timeValueMinutes(2));
    request.timeout("2m");
    request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
    request.setRefreshPolicy("wait_for");
    request.version(2);
    request.versionType(VersionType.EXTERNAL);


    // 提交

    try {
      DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);


      // 异步
      ActionListener<DeleteResponse> listener = new ActionListener<DeleteResponse>() {
        @Override
        public void onResponse(DeleteResponse deleteResponse) {

        }

        @Override
        public void onFailure(Exception e) {

        }
      };

      client.deleteAsync(request, RequestOptions.DEFAULT, listener);


      String index2 = deleteResponse.getIndex();
      String type2 = deleteResponse.getType();
      String id2 = deleteResponse.getId();
      long version = deleteResponse.getVersion();
      ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
      if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

      }
      if (shardInfo.getFailed() > 0) {
        for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
          String reason = failure.reason();
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    // 判断not found
    DeleteRequest request2 = new DeleteRequest("posts", "doc", "does_not_exist");
    DeleteResponse deleteResponse = null;
    try {
      deleteResponse = client.delete(request2, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {

    }


    // 检测冲突
    try {
      DeleteRequest request3 = new DeleteRequest("posts", "doc", "1").version(2);
      DeleteResponse deleteResponse3 = client.delete(request3, RequestOptions.DEFAULT);
    } catch (ElasticsearchException exception) {
      if (exception.status() == RestStatus.CONFLICT) {

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public void updateTest2(){
    // 如果id不存在 会报Not Found错误
    UpdateRequest request2 = new UpdateRequest("posts", "doc", "no_exists");

    String jsonString = "{" +
            "\"postDate\":\"2017-02-01\"," +
            "\"reason\":\"daily update\"" +
            "}";
    // 部分更新doc
    request2.doc(jsonString, XContentType.JSON);

    // 多个doc 只有后面的生效
    // DATE()类型的展现形式 "2018-09-27T08:32:00.685Z"
    // key value 拼接形式
    request2.doc("updated2", new Date(),
            "reason2", "daily update");

    //如果数据不存在，插入新数据
    request2.upsert(jsonString,XContentType.JSON);
    try {
      UpdateResponse updateResponse = client.update(request2, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * elasticsearch 更新api
   */
  public void updateTest(){
    // 更新操作
    UpdateRequest request = new UpdateRequest(
            "posts",
            "doc",
            "1");

    // 单例map，map只有一个元素，且不能修改
    Map<String, Object> parameters = singletonMap("count", 4);

    // script 为对象Map提供的脚本参数
    // 使用painless语言( es 的脚本语言 )和以前的参数创建内联脚本
    Script inline = new Script(ScriptType.INLINE, "painless",
            "ctx._source.field += params.count", parameters);

    // 设置script
    request.script(inline);


    // painless语言中 increment-field
    Script stored =
            new Script(ScriptType.STORED, null, "increment-field", parameters);
    request.script(stored);


    UpdateRequest request2 = new UpdateRequest("posts", "doc", "1");
    String jsonString = "{" +
            "\"updated\":\"2017-01-01\"," +
            "\"reason\":\"daily update\"" +
            "}";
    // 部分更新doc
    request2.doc(jsonString, XContentType.JSON);

    // 使用map更新
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("updated", new Date());
    jsonMap.put("reason", "daily update");
    UpdateRequest request3 = new UpdateRequest("posts", "doc", "1")
            .doc(jsonMap);


    try {
      // 封装json更新
      XContentBuilder builder = XContentFactory.jsonBuilder();
      builder.startObject();
      {
        builder.timeField("updated", new Date());
        builder.field("reason", "daily update");
      }
      builder.endObject();
      UpdateRequest request4 = new UpdateRequest("posts", "doc", "1")
              .doc(builder);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // 连接形式的修改，拼接字段
    UpdateRequest request5 = new UpdateRequest("posts", "doc", "1")
            .doc("updated", new Date(),
                    "reason", "daily update");

    //
    String jsonString2 = "{\"created\":\"2017-01-01\"}";

    // 如果文档不存在，则可以使用upsert方法定义将json作为新文档插入的内容
    // 否则会报错
    request.upsert(jsonString2, XContentType.JSON);

    request.routing("routing");
    request.timeout(TimeValue.timeValueSeconds(1));
    request.timeout("1s");
    request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
    request.setRefreshPolicy("wait_for");

    // 冲突最大尝试次数
    request.retryOnConflict(3);

    // 过虑数据
    request.fetchSource(true);
    // 指定版本
    request.version(2);

    // 禁用无操作检测 如果未指定更新内容，这里做检测
    request.detectNoop(false);

    // 指示无论文档是否存在，脚本都必须运行（如果文档不存在，则脚本负责创建文档）。
    request.scriptedUpsert(true);

    //指示doc必须用作UpSert文档。
    request.docAsUpsert(true);

    String[] includes = new String[]{"updated", "r*"};
    String[] excludes = Strings.EMPTY_ARRAY;
    request.fetchSource(new FetchSourceContext(true, includes, excludes));

    String[] includes2 = Strings.EMPTY_ARRAY;
    String[] excludes2 = new String[]{"updated"};
    request.fetchSource(new FetchSourceContext(true, includes2, excludes2));

    //设置在更新操作之前必须活动的分片副本的数量。
    request.waitForActiveShards(2);
    request.waitForActiveShards(ActiveShardCount.ALL);

    try {
      UpdateResponse updateResponse = client.update(request5, RequestOptions.DEFAULT);


      ActionListener<UpdateResponse> listener = new ActionListener<UpdateResponse>() {
        @Override
        public void onResponse(UpdateResponse updateResponse) {

        }

        @Override
        public void onFailure(Exception e) {

        }
      };

      // 异步
      client.updateAsync(request, RequestOptions.DEFAULT, listener);

      // response解析
      String index = updateResponse.getIndex();
      String type = updateResponse.getType();
      String id = updateResponse.getId();
      long version = updateResponse.getVersion();
      if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
        //如果是新增
      } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
        // 如果是更新
      } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
        // 如果是删除 ？ 什么情况会遇到删除的情况？
      } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
        //如果无操作
      }

      // 返回结果
      GetResult result = updateResponse.getGetResult();
      // 新的index
      if (result.isExists()) {
        String sourceAsString = result.sourceAsString();
        Map<String, Object> sourceAsMap = result.sourceAsMap();
        byte[] sourceAsBytes = result.source();
      } else {

      }



      // 分片信息
      ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();
      // 如果分片数 != 总分片数
      if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

      }
      // 获取失败的数量
      if (shardInfo.getFailed() > 0) {
        for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
          // 找到失败原因
          String reason = failure.reason();
        }
      }


      UpdateRequest request6 = new UpdateRequest("posts", "type", "does_not_exist")
              .doc("field", "value");
      try {
        UpdateResponse updateResponse2 = client.update(request6, RequestOptions.DEFAULT);
      } catch (ElasticsearchException e) {
        // 未找到时报错
        if (e.status() == RestStatus.NOT_FOUND) {

        }
      }

      UpdateRequest request7 = new UpdateRequest("posts", "doc", "1")
              .doc("field", "value")
              .version(1);
      try {
        UpdateResponse updateResponse3 = client.update(request7, RequestOptions.DEFAULT);
      } catch(ElasticsearchException e) {
        // 指定版本更新，报冲突
        if (e.status() == RestStatus.CONFLICT) {

        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void bulkTest() throws Exception{

    /**
     * 批量API仅支持以JSON或 SMILE编码的文档。以任何其他格式提供文档将导致错误。
     * 批量API可以同时执行 index,update,delete操作
     */
    BulkRequest request = new BulkRequest();
    request.add(new IndexRequest("posts", "doc", "1")
            .source(XContentType.JSON,"field", "foo"));
    request.add(new IndexRequest("posts", "doc", "2")
            .source(XContentType.JSON,"field", "bar"));
    request.add(new IndexRequest("posts", "doc", "3")
            .source(XContentType.JSON,"field", "baz"));

    request.add(new DeleteRequest("posts", "doc", "3"));
    request.add(new UpdateRequest("posts", "doc", "2")
            .doc(XContentType.JSON,"other", "test"));
    request.add(new IndexRequest("posts", "doc", "4")
            .source(XContentType.JSON,"field", "baz"));

    /* 可选设置 */
    request.timeout(TimeValue.timeValueMinutes(2));
    request.timeout("2m");

    request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
    request.setRefreshPolicy("wait_for");

    request.waitForActiveShards(2);
    request.waitForActiveShards(ActiveShardCount.ALL);

    /* 提交 */
    BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);


    // 异步
    ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
      @Override
      public void onResponse(BulkResponse bulkResponse) {

      }

      @Override
      public void onFailure(Exception e) {

      }
    };
    client.bulkAsync(request, RequestOptions.DEFAULT, listener);


    // response处理
    for (BulkItemResponse bulkItemResponse : bulkResponse) {
      DocWriteResponse itemResponse = bulkItemResponse.getResponse();

      if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
              || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
        IndexResponse indexResponse = (IndexResponse) itemResponse;

      } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
        UpdateResponse updateResponse = (UpdateResponse) itemResponse;

      } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
      }
    }

    // 判断批量操作是否有失败
    if (bulkResponse.hasFailures()) {
      // 遍历结果 寻找失败的项目，
      for (BulkItemResponse bulkItemResponse : bulkResponse) {
        if (bulkItemResponse.isFailed()) {
          BulkItemResponse.Failure failure = bulkItemResponse.getFailure();

        }
      }
    }

    // 批量操作前后操作
    // 这个侦听器在每个BulkRequest 执行前后调用，或者在BulkRequest失败时调用。
    BulkProcessor.Listener bulkListener = new BulkProcessor.Listener() {
      @Override
      public void beforeBulk(long executionId, BulkRequest request) {
        // 前
      }

      @Override
      public void afterBulk(long executionId, BulkRequest request,
                            BulkResponse response) {
        // 后
      }

      @Override
      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        // 失败时调用
      }
    };

    BulkProcessor bulkProcessor =
            BulkProcessor.builder(client::bulkAsync, bulkListener).build();

    // BulkProcessor.Builder 设置
    BulkProcessor.Builder builder = BulkProcessor.builder(client::bulkAsync, bulkListener);
    // 设置时，根据当前添加的动作数量（默认为1000，使用-1禁用）刷新新的批量请求。
    builder.setBulkActions(500);
    // 设置时，根据当前添加的动作大小（默认为5MB，使用-1禁用它）刷新新的批量请求。
    builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
    // 设置允许执行的并发请求的数量（默认为1，使用0只允许执行一个请求）
    builder.setConcurrentRequests(0);
    // 如果间隔时间过多，设置刷新间隔刷新任何Bulk请求（默认为未设置）
    builder.setFlushInterval(TimeValue.timeValueSeconds(10L));
    // 设置一个常数退避策略，它最初等待1秒，重试3次。
    // 有关更多选项，请参见
    // BackoffPolicy.noBackoff()
    // BackoffPolicy.constantBackoff()
    // BackoffPolicy.exponentialBackoff()
    builder.setBackoffPolicy(BackoffPolicy
            .constantBackoff(TimeValue.timeValueSeconds(1L), 3));

    // 动态添加以外，可以批量添加request
    IndexRequest one = new IndexRequest("posts", "doc", "1").
            source(XContentType.JSON, "title",
                    "In which order are my Elasticsearch queries executed?");
    IndexRequest two = new IndexRequest("posts", "doc", "2")
            .source(XContentType.JSON, "title",
                    "Current status and upcoming changes in Elasticsearch");
    IndexRequest three = new IndexRequest("posts", "doc", "3")
            .source(XContentType.JSON, "title",
                    "The Future of Federated Search in Elasticsearch");

    bulkProcessor.add(one);
    bulkProcessor.add(two);
    bulkProcessor.add(three);

    BulkProcessor.Listener listener2 = new BulkProcessor.Listener() {
      @Override
      public void beforeBulk(long executionId, BulkRequest request) {
        int numberOfActions = request.numberOfActions();
        System.out.println("Executing bulk [{}] with {} requests"+
                executionId+numberOfActions);
      }

      @Override
      public void afterBulk(long executionId, BulkRequest request,
                            BulkResponse response) {
        if (response.hasFailures()) {
          System.out.println("Bulk [{}] executed with failures"+executionId);
        } else {
          System.out.println("Bulk [{}] completed in {} milliseconds"+
                  executionId+response.getTook().getMillis());
        }
      }

      @Override
      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        System.out.println("Failed to execute bulk"+ failure);
      }
    };

    // 如果所有批量请求都已完成，则该方法返回true；如果所有批量请求完成之前的等待时间已过，则返回false
    boolean terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);

    // close方法可用于立即关闭bulkProcessor
    bulkProcessor.close();
  }


  public void multiGet(){
    MultiGetRequest request = new MultiGetRequest();
    request.add(new MultiGetRequest.Item(
            "index",
            "type",
            "example_id"));
    request.add(new MultiGetRequest.Item("index", "type", "another_id"));
    request.add(new MultiGetRequest.Item("index", "type", "example_id")
            .fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE));

    String[] includes = new String[] {"foo", "*r"};
    String[] excludes = Strings.EMPTY_ARRAY;
    FetchSourceContext fetchSourceContext =
            new FetchSourceContext(true, includes, excludes);
    request.add(new MultiGetRequest.Item("index", "type", "example_id")
            .fetchSourceContext(fetchSourceContext));
    String[] includes2 = Strings.EMPTY_ARRAY;
    String[] excludes2 = new String[] {"foo", "*r"};
    FetchSourceContext fetchSourceContext2 =
            new FetchSourceContext(true, includes2, excludes2);
    request.add(new MultiGetRequest.Item("index", "type", "example_id")
            .fetchSourceContext(fetchSourceContext2));

    request.add(new MultiGetRequest.Item("index", "type", "example_id")
            .storedFields("foo"));
    MultiGetResponse response = null;
    try {
      response = client.mget(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    MultiGetItemResponse item = response.getResponses()[0];
    String value = item.getResponse().getField("foo").getValue();

    request.add(new MultiGetRequest.Item("index", "type", "with_routing")
            .routing("some_routing"));
    request.add(new MultiGetRequest.Item("index", "type", "with_parent")
            .parent("some_parent"));
    request.add(new MultiGetRequest.Item("index", "type", "with_version")
            .versionType(VersionType.EXTERNAL)
            .version(10123L));request.preference("some_preference");
    request.realtime(false);
    request.refresh(true);

    try {
      MultiGetResponse response2 = client.mget(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    ActionListener<MultiGetResponse> listener = new ActionListener<MultiGetResponse>() {
      @Override
      public void onResponse(MultiGetResponse response) {

      }

      @Override
      public void onFailure(Exception e) {

      }
    };
    client.mgetAsync(request, RequestOptions.DEFAULT, listener);

    MultiGetItemResponse firstItem = response.getResponses()[0];
//    assertNull(firstItem.getFailure());
    GetResponse firstGet = firstItem.getResponse();
    String index = firstItem.getIndex();
    String type = firstItem.getType();
    String id = firstItem.getId();
    if (firstGet.isExists()) {
      long version = firstGet.getVersion();
      String sourceAsString = firstGet.getSourceAsString();
      Map<String, Object> sourceAsMap = firstGet.getSourceAsMap();
      byte[] sourceAsBytes = firstGet.getSourceAsBytes();
    } else {

    }

//    assertNull(missingIndexItem.getResponse());
//    Exception e = missingIndexItem.getFailure().getFailure();
//    ElasticsearchException ee = (ElasticsearchException) e;
// TODO status is broken! fix in a followup
// assertEquals(RestStatus.NOT_FOUND, ee.status());
//    assertThat(e.getMessage(),containsString("reason=no such index"));


    MultiGetRequest request3 = new MultiGetRequest();
    request.add(new MultiGetRequest.Item("index", "type", "example_id")
            .version(1000L));
    try {
      MultiGetResponse response3 = client.mget(request3, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    MultiGetItemResponse item3 = response.getResponses()[0];
//    assertNull(item.getResponse());
    Exception e = item.getFailure().getFailure();
    ElasticsearchException ee = (ElasticsearchException) e;
// TODO status is broken! fix in a followup
// assertEquals(RestStatus.CONFLICT, ee.status());
//    assertThat(e.getMessage(),containsString("version conflict, current version [1] is "
//                    + "different than the one provided [1000]"));
  }

  /**
   * 单例对象
   * @return 返回单例对象
   */
  public static ElasticSearchRestUtil getHelper(){
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

  public void searchTest(){
    // 创建search请求
    SearchRequest searchRequest = new SearchRequest();
    // 创建search builder
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 设置query内容   查询全部
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    // 查询内容添加到search 请求中
    searchRequest.source(searchSourceBuilder);

    // 搜索 index = posts的
    SearchRequest searchRequest2 = new SearchRequest("posts");
    // 搜索 type = doc
    searchRequest2.types("doc");

    /*  可选参数 */
    // 路由 为毛这里还有路由？
    searchRequest2.routing("routing");

    // 索引操作
    // 设置 indicesOptions 控制 不可用的索引 以及 如何扩展通配符表达式。
    searchRequest2.indicesOptions(IndicesOptions.lenientExpandOpen());

    // 偏爱值
    // 使用偏好参数，例如执行搜索，以喜欢本地shards。默认是跨shards随机化。
    searchRequest2.preference("_local");

    // 使用 查询场景
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    // 设置查询。可以是任何类型的查询生成器 查询 user = kimchy的
    sourceBuilder.query(QueryBuilders.termQuery("user", "kimchy"));

    // 设置从结果选项开始搜索的OFF选项。默认值为0。
    sourceBuilder.from(0);
    // 设置大小选项，以确定要返回的搜索命中数量。默认值为10。
    sourceBuilder.size(5);
    // 设置一个可选的超时，控制搜索允许多长时间。
    sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));



    SearchRequest searchRequest3 = new SearchRequest();
    searchRequest3.indices("posts");
    searchRequest3.source(sourceBuilder);

    // 匹配查询条件
    MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("user", "kimchy");

    // 模糊匹配
    matchQueryBuilder.fuzziness(Fuzziness.AUTO);
    // 设置前缀长度选项
    matchQueryBuilder.prefixLength(3);
    // 设置最大扩展选项来控制查询的模糊过程
    matchQueryBuilder.maxExpansions(10);

    // 链接式声明
    QueryBuilder matchQueryBuilder2 = QueryBuilders.matchQuery("user", "kimchy")
            .fuzziness(Fuzziness.AUTO)
            .prefixLength(3)
            .maxExpansions(10);

    // 设置查询条件
    searchSourceBuilder.query(matchQueryBuilder);

    // 排序规则 默认按照_score排序
    sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
    // 字段排序
    sourceBuilder.sort(new FieldSortBuilder("_uid").order(SortOrder.ASC));

    // 过滤器
    sourceBuilder.fetchSource(false);
    // 包含字段
    String[] includeFields = new String[] {"title", "user", "innerObject.*"};
    // 排除字段
    String[] excludeFields = new String[] {"_type"};
    sourceBuilder.fetchSource(includeFields, excludeFields);

    // 高亮显示
    SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
    HighlightBuilder highlightBuilder = new HighlightBuilder();
    // 高亮字段
    HighlightBuilder.Field highlightTitle =
            new HighlightBuilder.Field("title");
    // 高亮类型 统一的
    highlightTitle.highlighterType("unified");
    // 设置到builder中
    highlightBuilder.field(highlightTitle);

    HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
    highlightBuilder.field(highlightUser);
    // 设置到searchSource中
    searchSourceBuilder2.highlighter(highlightBuilder);

    // 请求聚合
    SearchSourceBuilder searchSourceBuilder3 = new SearchSourceBuilder();
    // 聚合结果字段名   聚合字段
    TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_company")
            .field("company.keyword");
    // 平均值
    aggregation.subAggregation(AggregationBuilders.avg("average_age")
            .field("age"));

    searchSourceBuilder3.aggregation(aggregation);

    /**
     * 搜索建议是搜索的一个重要组成部分，
     * 一个搜索建议的实现通常需要考虑建议词的来源、匹配、排序、聚合、关联的文档数和拼写纠错等
     * 匹配：能够通过用户的输入进行前缀匹配；
     * 排序：根据建议词的优先级进行排序；
     * 聚合：能够根据建议词关联的商品进行聚合，比如聚合分类、聚合标签等；
     * 纠错：能够对用户的输入进行拼写纠错；
     *
     * 例如: 搜索卫衣 推荐卫衣男，卫衣女，卫衣套装，等联想关键字
     *
     */
    // 请求搜索建议
    SearchSourceBuilder searchSourceBuilder4 = new SearchSourceBuilder();
    // 建议 user kmichy
    SuggestionBuilder termSuggestionBuilder =
            SuggestBuilders.termSuggestion("user").text("kmichy");

    SuggestBuilder suggestBuilder = new SuggestBuilder();
    // 增加 搜索建议
    suggestBuilder.addSuggestion("suggest_user", termSuggestionBuilder);
    searchSourceBuilder4.suggest(suggestBuilder);


    SearchSourceBuilder searchSourceBuilder5 = new SearchSourceBuilder();
    // 包含分析结果。
    searchSourceBuilder5.profile(true);


    try {
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

      // 异步
      ActionListener<SearchResponse> listener = new ActionListener<SearchResponse>() {
        @Override
        public void onResponse(SearchResponse searchResponse) {

        }

        @Override
        public void onFailure(Exception e) {

        }
      };

      client.searchAsync(searchRequest, RequestOptions.DEFAULT, listener);

      // 结果处理
      /**
       * 通过执行搜索返回的SearchResponse提供了关于搜索执行本身的细节以及对返回的文档的访问。
       * 首先，存在关于请求执行本身的有用信息，
       * 如HTTP状态代码、执行时间或请求是否提前终止或超时：
       * 其次，SearchResponse还提供关于shard级别执行的信息，
       * 方法是提供有关受搜索影响的shard总数以及成功shard与失败shard的统计数据。
       * 可能的故障也可以通过在一个数组中通过ShardSearchFailure 来处理，如下面的例子：
       */
      RestStatus status = searchResponse.status();
      TimeValue took = searchResponse.getTook();
      Boolean terminatedEarly = searchResponse.isTerminatedEarly();
      boolean timedOut = searchResponse.isTimedOut();

      int totalShards = searchResponse.getTotalShards();
      int successfulShards = searchResponse.getSuccessfulShards();
      int failedShards = searchResponse.getFailedShards();
      for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
        // failures should be handled here
      }

      //为了获得对返回的文档的访问，我们需要首先获得响应中包含的搜索hit：
      SearchHits hits = searchResponse.getHits();
      // 获得总hit数
      long totalHits = hits.getTotalHits();
      float maxScore = hits.getMaxScore();

      // 遍历搜索结果：
      SearchHit[] searchHits = hits.getHits();
      for (SearchHit hit : searchHits) {
        // 提供了对每个搜索命中的基本信息（如 index, type, docId and score）的访问：
        String index = hit.getIndex();
        String type = hit.getType();
        String id = hit.getId();
        float score = hit.getScore();

        /**
         * 此外，它可以让您返回文档源，
         * 既可以是简单的JSON字符串，也可以是键/值对的映射。
         * 在这个映射中，规则字段由字段名键入，并包含字段值。
         * 多值字段被返回为对象列表，嵌套对象作为另一个键/值映射。
         */
        // 字符串
        String sourceAsString = hit.getSourceAsString();
        // 键值对
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        // 获取字段
        String documentTitle = (String) sourceAsMap.get("title");
        //
        List<Object> users = (List<Object>) sourceAsMap.get("user");
        // 嵌套对象
        Map<String, Object> innerObject =
                (Map<String, Object>) sourceAsMap.get("innerObject");


        // 获取高亮字段
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        HighlightField highlight = highlightFields.get("title");
        // 获取包含突出显示字段内容的一个或多个片段
        Text[] fragments = highlight.fragments();
        String fragmentString = fragments[0].string();


        // 获取聚合
        Aggregations aggregations = searchResponse.getAggregations();
        Terms byCompanyAggregation = aggregations.get("by_company");
        Bucket elasticBucket = byCompanyAggregation.getBucketByKey("Elastic");
        // 平均值
        Avg averageAge = elasticBucket.getAggregations().get("average_age");
        double avg = averageAge.getValue();


        // 注意，如果按名称访问聚合，则需要根据请求的聚合类型指定聚合接口，否则将引发ClassCastException：
        // 将引发一个异常
        Range range = aggregations.get("by_company");

        // map方式访问
        Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
        Terms companyAggregation = (Terms) aggregationMap.get("by_company");

        // list方式访问
        List<Aggregation> aggregationList = aggregations.asList();

        //
        for (Aggregation agg : aggregations) {
          String type2 = agg.getType();
          if (type.equals(TermsAggregationBuilder.NAME)) {
            elasticBucket = ((Terms) agg).getBucketByKey("Elastic");
            long numberOfDocs = elasticBucket.getDocCount();
          }
        }

        // 获取 搜索推荐
        Suggest suggest = searchResponse.getSuggest();
        TermSuggestion termSuggestion = suggest.getSuggestion("suggest_user");
        for (TermSuggestion.Entry entry : termSuggestion.getEntries()) {
          for (TermSuggestion.Entry.Option option : entry) {
            String suggestText = option.getText().string();
          }
        }

        // 获取分析结果
        Map<String, ProfileShardResult> profilingResults =
                searchResponse.getProfileResults();
        for (Map.Entry<String, ProfileShardResult> profilingResult : profilingResults.entrySet()) {
          String key = profilingResult.getKey();
          ProfileShardResult profileShardResult = profilingResult.getValue();

          List<QueryProfileShardResult> queryProfileShardResults =
                  profileShardResult.getQueryProfileResults();
          for (QueryProfileShardResult queryProfileResult : queryProfileShardResults) {

            // 分析结果
            for (ProfileResult profileResult : queryProfileResult.getQueryResults()) {
              String queryName = profileResult.getQueryName();
              long queryTimeInMillis = profileResult.getTime();
              List<ProfileResult> profiledChildren = profileResult.getProfiledChildren();
            }

            //  获取结果容器
            CollectorResult collectorResult = queryProfileResult.getCollectorResult();
            String collectorName = collectorResult.getName();
            Long collectorTimeInMillis = collectorResult.getTime();
            List<CollectorResult> profiledChildren = collectorResult.getProfiledChildren();
          }

          AggregationProfileShardResult aggsProfileResults =
                  profileShardResult.getAggregationProfileResults();
          for (ProfileResult profileResult : aggsProfileResults.getProfileResults()) {
            String aggName = profileResult.getQueryName();
            long aggTimeInMillis = profileResult.getTime();
            List<ProfileResult> profiledChildren = profileResult.getProfiledChildren();
          }
        }
      }


    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
