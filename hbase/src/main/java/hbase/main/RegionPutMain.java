package hbase.main;

import com.hjh.hbase.HBaseHelper;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-21
 * @Description:
 */
public class RegionPutMain {
  public static void main(String[] args) {
    HBaseHelper helper = null;
    try {
      helper = HBaseHelper.getHelper();
      helper.put("access_url",
              new String[]{"20180901-1","20180902-1","20180902-2","20180903-1"},
              new String[]{"cf"},
              new String[]{"url"},
              new String[] {"1-1","2-1","2-2","3-1"}
              );
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if(helper != null) {
          helper.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
