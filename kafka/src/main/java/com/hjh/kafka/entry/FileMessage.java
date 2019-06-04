package com.hjh.kafka.entry;

import java.util.ArrayList;
import java.util.List;

public class FileMessage {
    private String filePerfix;
    private List<String> fileList;

    public FileMessage(String filePerfix){
        this.filePerfix = filePerfix;
        this.fileList = new ArrayList<>();
    }

    public FileMessage(String filePerfix, List<String> fileList) {
        this.filePerfix = filePerfix;
        this.fileList = fileList;
    }

    public String getFilePerfix() {
        return filePerfix;
    }

    public List<String> getFileList() {
        return fileList;
    }

    public List<String> add(String filePath){
        this.fileList.add(filePath);
        return this.fileList;
    }

    public int getSize(){
        return this.fileList.size();
    }

    @Override
    public int hashCode() {
        return filePerfix.hashCode();
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "filePerfix='" + filePerfix + '\'' +
                ", fileList=" + fileList.toString() +
                '}';
    }
}
