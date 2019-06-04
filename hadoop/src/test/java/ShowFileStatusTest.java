
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import com.hjh.hdfs.Constant;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-06
 * @Description:
 */
public class ShowFileStatusTest {

  private FileSystem fs = null;
  private String testPath = "/dir/file";

  @Before
  public void setUp() {
    Configuration conf = new Configuration();
    try {
      fs = FileSystem.get(Constant.getConf());
      if(fs.exists(new Path(testPath))){
        return ;
      }
      OutputStream out = fs.create(new Path(testPath));
      out.write("content".getBytes("utf-8"));
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test(expected = FileNotFoundException.class)
  public void throwsFileNotFoundForNonExistentFile() throws IOException{
    //这里默认去找 hdfs://hadoop:9000/user/Administrator(当前用户)/no-such-file 和linux的用户目录相同
    fs.getFileStatus(new Path("no-such-file"));
  }

  @Test
  public void fileStatusForFile() {
    Path file = new Path(testPath);
    try {
      FileStatus status = fs.getFileStatus(file);
      System.out.println(status.getPath());
      assertThat(status.getPath().toUri().getPath(), is("/dir/file"));
      assertThat(status.isDirectory(), is(false));
      assertThat(status.getLen(), is(7L));
//      assertThat(status.getModificationTime(),is(lessThanOrEqualTo(System.currentTimeMillis())));
      assertThat(status.getReplication(), is((short)1));
      assertThat(status.getBlockSize(),is(128 * 1024 * 1024L));
      assertThat(status.getOwner(), is("Administrator"));
      assertThat(status.getGroup(), is("supergroup"));
      assertThat(status.getPermission().toString(), is("rw-r--r--"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void fileStatusForDirectory () {
    Path dir = new Path("/dir");
    try {
      FileStatus status = fs.getFileStatus(dir);
      assertThat(status.getPath().toUri().getPath(), is("/dir"));
      assertThat(status.isDirectory(), is(true));
      assertThat(status.getLen(), is(0L));
//      assertThat(status.getModificationTime(),is(lessThanOrEqualTo(System.currentTimeMillis())));
      assertThat(status.getReplication(), is((short)0));
      assertThat(status.getBlockSize(),is(0L));
      assertThat(status.getOwner(), is("Administrator"));
      assertThat(status.getGroup(), is("supergroup"));
      assertThat(status.getPermission().toString(), is("rwxr-xr-x"));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @After
  public void tearDown() {
    if (fs != null) {
      try {
        fs.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    ShowFileStatusTest test = new ShowFileStatusTest();
    test.setUp();
    test.fileStatusForFile();
  }

}
