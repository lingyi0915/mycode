package tohfile;

import com.hjh.hbase.HBaseHelper;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;

public class TxtToHFile {
    public static void toHFile(File srcFile){
        try {
            FileSystem fs = FileSystem.get(ConfigurationManager.getConf());
            FileStatus[] fileStatuses = fs.listStatus(new Path("/"));
            for(FileStatus fileStatus : fileStatuses){
                System.out.println(fileStatus.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean toHFile(File srcFile, File dstFile){
        System.out.println("srcPath:"+srcFile.getPath()+",dstPath:"+dstFile.getPath());
        try {
            dstFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
