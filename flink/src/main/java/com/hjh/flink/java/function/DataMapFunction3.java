package com.hjh.flink.java.function;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * @Author: hjh
 * @Create: 2019/3/27
 * @Description:
 */
public class DataMapFunction3 implements MapFunction<String, Tuple3<String, String,String>> {
    @Override
    public Tuple3<String, String, String> map(String value) throws Exception {
        String[] spt = value.split("\t");
        Tuple3<String,String,String> res = null;
        if(spt.length == 3){
            res = new Tuple3<>(spt[0],spt[1],spt[2]);
            System.out.println(res);
        }
        return res;
    }
}
