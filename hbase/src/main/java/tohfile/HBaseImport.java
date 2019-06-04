package tohfile;

import helper.FileHelper;
import org.apache.log4j.Logger;
import tohfile.jdbc.MysqlJDBC;
import tohfile.runnable.ToHFileRunnable;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HBaseImport {
    public static Logger LOG = Logger.getLogger(HBaseImport.class);

    public static String dirPath = "D:\\workspace\\hbaseimport";

    public static ThreadPoolExecutor executor =  new ThreadPoolExecutor(6,6,200, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(100));

    public static void excute(File dir) {

        //检索后缀符合的文件
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.endsWith(".log");
            }
        });

        // 没有符合条件的文件，就间隔性扫描
        if(files == null || files.length == 0){
            LOG.debug("扫描文件中...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LOG.error("线程等待被中断,继续扫描目录!");
            }
            return ;
        }
        //执行文件转HFile
        for(File file:files){
            ToHFileRunnable task = new ToHFileRunnable(file);
            executor.execute(task);
        }
    }





    public static void main(String[] args){
        //首先判断目标文件夹是否存在
        File dir = new File(dirPath);
        if(!FileHelper.dirExists(dir)){
            LOG.error(dirPath+"找不到或不是一个目录，退出程序!");
            System.exit(1);
        }
        //启动时清空tmp文件夹
        File tmpDir = new File(dirPath+File.separator+"tmp");
        File[] fileList = tmpDir.listFiles();
        for(File file : fileList) {
            file.delete();
        }
//        while(true){
//
//        }
        //执行入库
        HBaseImport.excute(dir);
    }
}
