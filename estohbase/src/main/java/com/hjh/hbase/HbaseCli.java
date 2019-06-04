package com.hjh.hbase;

import org.apache.commons.cli.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-07-13
 * @Description:
 */
public class HbaseCli {
  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
  public static void main(String[] args) throws Exception{
    //功能参数列表
    Options opts = new Options();
    opts.addOption("h", "help", false, "获取帮助.");
    opts.addOption("st","starttime",true,"起始时间");
    opts.addOption("et","endtime",true,"终止时间");
    //命令行参数转换

    Parser parser = new BasicParser();
    Calendar st = Calendar.getInstance();
    Calendar et = Calendar.getInstance();
    CommandLine cli = parser.parse(opts, args);
    if (cli.hasOption("help")) {
      HelpFormatter hf = new HelpFormatter();
      hf.printHelp("options", opts);
      return ;
    }
    if (cli.hasOption("starttime")) {
      System.out.println(cli.getOptionValue("st"));
      st.setTime(sdf.parse(cli.getOptionValue("st")));
    }else{
      System.out.println("缺少起始时间");
      return ;
    }
    if (cli.hasOption("endtime")) {
      System.out.println(cli.getOptionValue("et"));
      et.setTime(sdf.parse(cli.getOptionValue("et")));
    }else{
      System.out.println("缺少结束时间");
      return ;
    }
    Calendar cal = Calendar.getInstance();
    for(cal.setTime(st.getTime()); cal.compareTo(et)<=0 ; cal.add(Calendar.DAY_OF_MONTH,1)){
      System.out.println(sdf.format(cal.getTime()));
    }
  }
}
