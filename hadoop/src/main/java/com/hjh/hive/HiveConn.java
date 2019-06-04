package com.hjh.hive;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-03-02
 * @Description:
 */
public class HiveConn {
  private Connection connection;
  private PreparedStatement ps;
  private ResultSet rs;
  public void getConnection() {
    try {
      Class.forName("org.apache.hive.jdbc.HiveDriver");
      connection = DriverManager.getConnection("jdbc:hive2://106.75.9.92:10000/", "root", "root");
      System.out.println(connection);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  //关闭连接
  public void close() {
    try {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  public void find() {
    String sql = "select * from business.fact_click_ad_info limit 1";
    try {
      ps = connection.prepareStatement(sql);
      rs = ps.executeQuery();
      while (rs.next()) {
        System.out.println(rs.getString(1)+" "+rs.getString(2));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }finally{
      close();
    }

  }

  public boolean show_tables() {
    String sql = "SHOW TABLES";
    System.out.println("Running:" + sql);
    try {
      ps = connection.prepareStatement(sql);
      ResultSet res = ps.executeQuery();
      System.out.println("执行“+sql+运行结果:");
      while (res.next()) {
        System.out.println(res.getString(1));
      }
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static void main(String[] args) {
    HiveConn hc = new HiveConn();
    hc.getConnection();
    hc.show_tables();
    hc.find();
  }
}
