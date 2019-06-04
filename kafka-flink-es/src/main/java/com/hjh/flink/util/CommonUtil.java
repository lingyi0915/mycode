package com.hjh.flink.util;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @Author: hjh
 * @Create: 2019/3/30
 * @Description:
 */
public class CommonUtil {


    public static RestHighLevelClient getRestHighLevelClient(){

        RestClientBuilder builder = RestClient.builder(new HttpHost("hadoop",9200));

        RestHighLevelClient client = new RestHighLevelClient(builder);

        return client;
    }
}
