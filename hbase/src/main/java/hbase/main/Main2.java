package hbase.main;

import com.hjh.hbase.HBaseHelper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-20
 * @Description:
 */
public class Main2 {
  public static void main(String[] args) throws Exception{
    HBaseHelper helper = HBaseHelper.getHelper();
    helper.createTable("access_url","cf");
  }
}
