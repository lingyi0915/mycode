package com.hjh.flink.java.function;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * @Author: hjh
 * @Create: 2019/3/29
 * @Description:
 */
public class StreamJoinFunction implements JoinFunction<String,String, Tuple3<String,String,String>> {
    @Override
    public Tuple3<String, String, String> join(String first, String second) throws Exception {
        String[] spt = first.split("\t");
        String[] spt2 = second.split("\t");
        return new Tuple3<>(spt[0],spt[1],spt2[2]);
    }
}
