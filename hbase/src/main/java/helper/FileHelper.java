package helper;

import java.io.File;

public class FileHelper {
    public static boolean dirExists(File dir){
        if(!dir.exists()){
            return false;
        }
        if(!dir.isDirectory()){
            return false;
        }
        return true;
    }

    public static boolean createDir(String path){
        File dir = new File(path);
        if(dir.exists() && dir.isDirectory()){
            return true;
        }else if(!dir.exists()){
            dir.mkdirs();
            return true;
        } else if(dir.isFile()){
            return false;
        }
        return false;
    }
}
