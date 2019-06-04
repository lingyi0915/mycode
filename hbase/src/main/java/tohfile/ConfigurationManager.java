package tohfile;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.io.File;

public class ConfigurationManager {
    public static Configuration getConf(){
        String osname = System.getProperty("os.name");
        if(osname.contains("WIN")){
            return getConfFromConfDir();
        } else {
            return getConfFromLinux();
        }
    }

    public static Configuration getConfFromConfDir(){
        Configuration conf = new Configuration(true);
        String confDirPath = System.getProperty("user.dir")+ File.separator+"conf"+File.separator;
        conf.addResource(new Path(confDirPath+"hdfs-site.xml"));
        conf.addResource(new Path(confDirPath+"core-site.xml"));
        conf.addResource(new Path(confDirPath+"yarn-site.xml"));
        conf.addResource(new Path(confDirPath+"mapred-site.xml"));
        conf.addResource(new Path(confDirPath+"hbase-site.xml"));
        return conf;
    }

    public static Configuration getConfFromLinux(){
        return getConfFromConfDir();
    }
}
