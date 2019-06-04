package com.hjh.flink.java.partition;

import org.apache.flink.api.common.functions.Partitioner;

/**
 * @Author: hjh
 * @Create: 2019/3/24
 * @Description:
 */
public class MyPartition implements Partitioner<Integer> {
    @Override
    public int partition(Integer key, int numPartitions) {
        System.out.println("分区总数:"+numPartitions);
        return key%2;
    }
}
