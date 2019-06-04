package hbase.main;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-21
 * @Description:
 */
public class TestMail {
  public static void main(String[] args) {
    String[] name=  {
            "pcac.access_2018-09-01.log",
            "pcac.access_2018-09-02.log",
            "pcac.access_2018-09-03.log"
    };

    for (String str : name) {
      System.out.println(str.substring(12,22).replaceAll("-",""));
    }

  }
}
