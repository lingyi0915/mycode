package com.hjh.elasticsearch;

import org.elasticsearch.index.query.QueryBuilders;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-28
 * @Description:
 */
public class TestElasticSearchTransportUtil {
  public static void main(String[] args) {
    ElasticSearchTransportUtil helper = ElasticSearchTransportUtil.getHelper();
//    helper.deleteByQueryTest();
    QueryBuilders.matchQuery("dt","2018-01-01");

    helper.close();

  }
}
