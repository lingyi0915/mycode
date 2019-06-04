package com.hjh.flink.java.function;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: hjh
 * @Create: 2019/3/27
 * @Description:
 */
public class DataTimeAssignFunction  implements AssignerWithPunctuatedWatermarks<Tuple2<String, String>> {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    String name = Thread.currentThread().getName();
    private long delay = 10*1000;

    /*
     * 再执行该函数，extractedTimestamp的值是extractTimestamp的返回值
     */
    @Nullable
    @Override
    public Watermark checkAndGetNextWatermark(Tuple2<String, String> lastElement, long extractedTimestamp) {
//        long time = 0L;
//        if(lastElement != null){
//            try {
//                time = sdf.parse(lastElement.f0).getTime();
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
        extractedTimestamp = extractedTimestamp - delay;
        System.out.println(name+":\n\t获得水印时间:"+extractedTimestamp+","+sdf.format(new Date(extractedTimestamp)));
        return new Watermark(extractedTimestamp);
    }

    /*
     * 先执行该函数，从element中提取时间戳
     * previousElementTimestamp 是当前的时间
     */
    @Override
    public long extractTimestamp(Tuple2<String, String> element, long previousElementTimestamp) {
        long time = 0L;
        if(element != null){
            try {
                time = sdf.parse(element.f0).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        System.out.println(name+":\n\t获得extractTimestamp:"+time+","+sdf.format(new Date(time)));
        return time;
    }
}