package com.hjh.flink.java.function;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * @Author: hjh
 * @Create: 2019/3/27
 * @Description:
 */
public class DataMapFunction implements MapFunction<String, Tuple2<String, String>> {
    @Override
    public Tuple2<String, String> map(String value) throws Exception {
        String[] spt = value.split("\t");
        Tuple2<String,String> res = null;
        if(spt.length >= 2){
            res = new Tuple2<>(spt[0],spt[1]);
        } else if (spt.length == 1){
            res = new Tuple2<>(spt[0],null);
        } else if (spt.length == 0){
            res = null;
        }

        return res;
    }
}
