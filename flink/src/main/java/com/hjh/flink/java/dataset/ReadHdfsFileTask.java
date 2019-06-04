package com.hjh.flink.java.dataset;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class ReadHdfsFileTask {
    public static void main(String[] args) {
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        final ParameterTool params = ParameterTool.fromArgs(args);
        env.getConfig().setGlobalJobParameters(params);

//        DataSet<String> hdfslines=env.readTextFile("your hdfs path");

    }
}
