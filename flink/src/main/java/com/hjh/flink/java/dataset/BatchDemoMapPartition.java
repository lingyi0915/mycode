package com.hjh.flink.java.dataset;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.MapPartitionFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Author: hjh
 * @Create: 2019/3/24
 * @Description: 关联数据库等，每个partition都要做的事情，建议mapPartition完成
 */
public class BatchDemoMapPartition {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        List<String> list = new ArrayList();

        list.add("数据1");
        list.add("数据2");
        list.add("数据3");

        DataSource<String> dataSet = env.fromCollection(list);

//        DataSet<String> map = dataSet.map(new MapFunction<String, String>() {
//            @Override
//            public String map(String value) throws Exception {
//                //创建连接，
//                //写入数据
//                //关闭连接
//                value = value+","+value;
//                return value;
//            }
//        });

        DataSet<String> mapPartition =  dataSet.mapPartition(new MapPartitionFunction<String, String>() {
            @Override
            public void mapPartition(Iterable<String> values, Collector<String> out) throws Exception {
                //获取数据库连接  一个partition 获取一次连接
                // 处理数据
                //关闭连接
                Iterator<String> iter = values.iterator();

                while(iter.hasNext()){
                    String next = iter.next();
                    String id = next.substring(2,3);
                    out.collect(id);
                }
            }
        });

        mapPartition.print();
    }
}
