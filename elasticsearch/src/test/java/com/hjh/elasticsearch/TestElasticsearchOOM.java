package com.hjh.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: hjh
 * @Create: 2019/3/30
 * @Description:
 */
public class TestElasticsearchOOM {
    public static void main(String[] args) {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("hadoop",Integer.parseInt("9200"))));

        Runnable run = ()->{
            BulkRequest bulk = new BulkRequest();
            IndexRequest request;
            String str = Thread.currentThread().getName();
            int i=0;
            while(true){
                try {
                    request = new IndexRequest("test","doc");
                    request.source("{\"count\":\""+str+":第"+i+"条记录\"}"
                            , XContentType.JSON);
                    bulk.add(request);
                    i++;
                    if(i >= 300000){
                        if(i%10000 == 0) {
                            System.out.println("list.size:"+bulk.numberOfActions());
                            client.bulk(bulk, RequestOptions.DEFAULT);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(run).start();
        new Thread(run).start();
        new Thread(run).start();
        new Thread(run).start();
     }
}
