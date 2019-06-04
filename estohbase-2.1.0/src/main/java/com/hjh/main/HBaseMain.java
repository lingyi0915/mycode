package com.hjh.main;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * @Author: hjh
 * @Create: 2019/4/2
 * @Description:
 */
public class HBaseMain {
    public static void main(String[] args) {
        Configuration conf = HBaseConfiguration.create();
        System.out.println(conf);
    }
}
