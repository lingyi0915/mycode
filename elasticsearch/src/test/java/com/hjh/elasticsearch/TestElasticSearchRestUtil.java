package com.hjh.elasticsearch;

import com.hjh.elasticsearch.ElasticSearchRestUtil;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-26
 * @Description:
 */
public class TestElasticSearchRestUtil {
  public static void main(String[] args) {
    ElasticSearchRestUtil helper = ElasticSearchRestUtil.getHelper();

    String jsonString = "{" +
            "\"user\":\"kimchy2\"," +
            "\"postDate\":\"2013-03-30\"," +
            "\"message\":\"trying out Elasticsearch2\"" +
            "}";
//    helper.put("posts","doc","1",jsonString);

    helper.get("posts","doc","1");

//    helper.putRefreshTest("posts","doc","22",jsonString);

//    helper.updateTest2();

    helper.close();
  }
}
