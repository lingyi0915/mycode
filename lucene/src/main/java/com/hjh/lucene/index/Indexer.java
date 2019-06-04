package com.hjh.lucene.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;


public class Indexer {

    private IndexWriter writer;

    public Indexer(){

    }

    public Indexer(String indexDir) throws Exception {
        setWriter(indexDir);
    }

    public IndexWriter setWriter(String indexDir) throws Exception {
        // 指定index文件存放目录
        Directory indexFile = FSDirectory.open(Paths.get(indexDir));
        // 标准分词器
        Analyzer analyzer=new StandardAnalyzer();

        IndexWriterConfig iwc=new IndexWriterConfig(analyzer);
        writer = new IndexWriter(indexFile, iwc);
        return writer;
    }

    /**
     * 关闭写索引
     * @throws Exception
     */
    public void close()throws Exception{
        writer.close();
    }

    /**
     * 索引指定目录的所有文件
     * @param dataDir
     * @throws Exception
     */
    public int index(String dataDir)throws Exception{
        File[]files=new File(dataDir).listFiles();
        for(File f:files){
            indexFile(f);
        }
        return writer.numDocs();
    }

    /**
     * 索引指定文件
     * @param f
     */
    private void indexFile(File f) throws Exception{

        Document doc = getDocument(f);
        writer.addDocument(doc);// 缓存大的时候也会触发写index文件
    }

    /**
     * 获取文档，文档里再设置每个字段
     * @param f
     */
    private Document getDocument(File f)throws Exception {
        System.out.println("索引文件："+f.getCanonicalPath());

        String fileName =  f.getName();

        String dt = fileName.substring(12,22).replaceAll("-","");

        Document doc = new Document();

        doc.add(new TextField("dt",dt,Field.Store.YES));
//        doc.add(new TextField("content",new FileReader(f)));
        doc.add(new TextField("filename",fileName , Field.Store.YES));
        doc.add(new TextField("fullpath",f.getCanonicalPath(),Field.Store.YES));
        doc.add(new TextField("filetype","log",Field.Store.YES));

        return doc;
    }

    public static void indexDirFile(String indexDir,String dataDir){
        Indexer indexer = new Indexer();
        int numIndexed=0;
        long start=System.currentTimeMillis();
        try {
            indexer.setWriter(indexDir);
            numIndexed=indexer.index(dataDir);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                indexer.close();// 会触发flush
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        long end=System.currentTimeMillis();
        System.out.println("索引："+numIndexed+" 个文件 花费了"+(end-start)+" 毫秒");
    }

    public static void main(String[] args) {
        String indexDir = "D:\\workspace\\索引目录";
        String dataDir = "D:\\workspace\\测试数据";

        Indexer.indexDirFile(indexDir,dataDir);
    }
}
