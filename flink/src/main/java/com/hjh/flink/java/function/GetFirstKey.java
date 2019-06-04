package com.hjh.flink.java.function;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * @Author: hjh
 * @Create: 2019/4/4
 * @Description:
 */
public class GetFirstKey implements KeySelector<Tuple2<String, String>, String> {
    @Override
    public String getKey(Tuple2<String, String> value){
        return value.f0;
    }
}
