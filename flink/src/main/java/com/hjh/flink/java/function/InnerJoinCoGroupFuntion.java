package com.hjh.flink.java.function;

import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author: hjh
 * @Create: 2019/3/28
 * @Description:
 */
public class InnerJoinCoGroupFuntion implements CoGroupFunction<Tuple2<String, String>, Tuple2<String, String>, Tuple3<String,String,String>>{
    @Override
    public void coGroup(Iterable<Tuple2<String, String>> first, Iterable<Tuple2<String, String>> second, Collector<Tuple3<String, String, String>> out) throws Exception {

        List<Tuple3<String,String,String>> res = new ArrayList<>();

        int size1=0,size2=0;
        if (first instanceof Collection<?>) {
            size1 = ((Collection<?>)first).size();
        }
        if (second instanceof Collection<?>) {
            size2 = ((Collection<?>)second).size();
        }

        String name = Thread.currentThread().getName();
        System.out.println(name+"同一批次");
        first.forEach((t1)-> System.out.println(t1));
        second.forEach((t2)-> System.out.println(t2));
        System.out.println(name+"同一批次結束");

        if(size1 == 0 || size2 == 0){
            return;
        } else if(size1 != 0 && size2 != 0){
            first.forEach((t1)->{
                second.forEach((t2)->{
                    Tuple3<String,String,String> t3 = new Tuple3<>(t1.f0,t1.f1,t2.f1);
                    out.collect(t3);
                });
            });
        }
    }
}