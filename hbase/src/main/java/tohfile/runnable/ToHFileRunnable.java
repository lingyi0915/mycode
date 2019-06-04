package tohfile.runnable;

import helper.FileHelper;
import tohfile.HBaseImport;
import tohfile.TxtToHFile;
import tohfile.jdbc.MysqlJDBC;

import java.io.File;
import java.util.LinkedHashMap;

public class ToHFileRunnable implements Runnable{

    private File srcFile;

    public ToHFileRunnable(File srcFile){
        this.srcFile = srcFile;
    }

    @Override
    public void run() {
        // 从文件名获取表名
        String tableName = getTableName(srcFile.getName());
        // 获得表元信息，字段，字段类型，是否索引等
        LinkedHashMap tableField = MysqlJDBC.getTableSchema(tableName);
        String tmpPath = HBaseImport.dirPath+File.separator+"tmp";
        if(FileHelper.createDir(tmpPath)){
            //获得生成的HFILE名
            File dstFile = getHFileName(tmpPath,tableName);
            //转换HFILE
            TxtToHFile.toHFile(srcFile,dstFile);
        }
    }

    public static File getHFileName(String tmpPath ,String tableName){
        int seq = 0;
        String dstPath;
        File dstFile;
        do {
            seq++;
            dstPath = tmpPath + File.separator + tableName + "_" + System.currentTimeMillis() + "_" + seq+".ing";
            dstFile = new File(dstPath);
        }while(dstFile.exists());
        return dstFile;
    }

    public static String getTableName(String fileName){
        return fileName.split("\\.")[0];
    }

    public void TxtToHFile(File txtFile){

    }

}
