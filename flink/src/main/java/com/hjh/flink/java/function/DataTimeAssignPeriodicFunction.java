package com.hjh.flink.java.function;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: hjh
 * @Create: 2019/4/4
 * @Description:
 */
public class DataTimeAssignPeriodicFunction implements AssignerWithPeriodicWatermarks<Tuple2<String,String>> {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    final Long maxDealy = 10000L;
    private Long extractedTimestamp = 0L;
    String name = Thread.currentThread().getName();

    @Nullable
    @Override
    public Watermark getCurrentWatermark() {
        System.out.println(name+":\n\t获得水印时间:"+extractedTimestamp+","+sdf.format(new Date(extractedTimestamp)));
        return new Watermark(extractedTimestamp);
    }

    @Override
    public long extractTimestamp(Tuple2<String,String> element, long previousElementTimestamp) {
        long time = 0L;
        if(element != null){
            try {
                time = sdf.parse(element.f0).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        System.out.println(name+":\n\t获得extractTimestamp:"+time+","+sdf.format(new Date(time)));
        extractedTimestamp = time - maxDealy;
        return time;
    }
}
