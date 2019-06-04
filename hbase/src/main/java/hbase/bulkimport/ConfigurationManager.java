package hbase.bulkimport;

import org.apache.hadoop.conf.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-20
 * @Description:
 */
public class ConfigurationManager {
  public static Configuration conf =null;
  public static String userdir = System.getProperty("user.dir") + File.separator;
  public static String confdir = userdir + "conf" + File.separator;
  public static Configuration getConf(){
    if(conf == null){
      conf = new Configuration(true);
      Properties prop = System.getProperties();
      String os = prop.getProperty("os.name");
      try{
        if(os!=null && os.toUpperCase().startsWith("WIN")){
          conf.addResource(new FileInputStream(new File(confdir+"core-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"hdfs-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"hive-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"mapred-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"yarn-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"hbase-site.xml")));
        }else{
          conf.addResource(new FileInputStream(new File(confdir+"core-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"hdfs-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"hive-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"mapred-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"yarn-site.xml")));
          conf.addResource(new FileInputStream(new File(confdir+"hbase-site.xml")));
        }
      }catch (FileNotFoundException e){
        e.printStackTrace();
      }
    }
    return conf;
  }
}
