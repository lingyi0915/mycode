package com.hjh.java;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class DeleteFile {

    public static File[] lastFiles = new File[1001];
    public static int index = 0;
    public static int allIndex = 0;
    public static FileFilter fileter = (pathname) ->{return pathname.getName().endsWith("lastUpdated"); };
    public static File rootDir = new File("C:\\Users\\Administrator\\.m2\\repository");


    public static void recursionFilterFile(File root){
        //只能得到当前目录的子目录，不能递归
        File[] files = root.listFiles(fileter);
        System.out.println(files.length);
        for(File f : files){
            System.out.println(f.getName());
            f.delete();
        }
    }

    public void recursionRoot(){
        recursionGetFile(rootDir);
    }

    public static void recursionGetFile(File root){
        File[] files = root.listFiles();
        for(File f : files){
            if(f.isDirectory()){
                recursionGetFile(f);
            }
            if(f.isFile()){
                if(fileter.accept(f)){
                    lastFiles[index++]=f;
                    System.out.println(f.getName());
                    f.delete();
                }
                allIndex++;
            }
        }
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            try{
                String cmd = "cmd.exe /c start java -jar " + (new File(DeleteFile.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getAbsolutePath() + " cmd";
                System.out.println();
                Process p = Runtime.getRuntime().
                        exec(cmd);
            } catch (IOException e){
                e.printStackTrace();
            }
        } else {
            //code to be executed
            recursionGetFile(rootDir);
            System.out.println(allIndex);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
