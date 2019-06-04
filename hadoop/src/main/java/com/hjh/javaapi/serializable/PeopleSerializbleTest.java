package com.hjh.javaapi.serializable;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-12
 * @Description:
 */
public class PeopleSerializbleTest {
  public static void main(String[] args) throws Exception{
    People people = new People();
    People depeople = null;
    people.setName("黄俊辉2");
    people.setSex("男");
    people.setAge(23);

//    depeople = (People) deserializableFromFile(serializableToFile(people));

    depeople = (People) deserializableFromByteArray(serializableToByteArray(people));

//    depeople = (People) deserializableFromBuffered(serializableToBuffered(people));
    System.out.println(depeople);

  }

  /**
   * @param obj 对象
   * @return 返回序列化的文件路径
   */
  public static File serializableToFile(Object obj) throws Exception{
    String className = obj.getClass().getName();
    File file = new File("d:/"+className+".txt");
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
    oos.writeObject(obj);
    oos.close();
    System.out.println("类的实例序列化成功");
    return file;
  }

  /**
   * 序列化时保留 package 和 serialVersionUID 只要这两个值一样，增加删除变量，方法，依然可以反序列化成功
   * package 不对 java.lang.ClassNotFoundException: com.hjh.Java.serializable.People
   * serialVersionUID 不对 java.io.InvalidClassException: com.hjh.Java.serializable.People; local class incompatible: stream classdesc serialVersionUID = 1, local class serialVersionUID = -1
   * @param file 序列化文件
   * @return  反序列化的object
   */
  public static Object deserializableFromFile(File file) throws  Exception{
    ObjectInputStream oio = new ObjectInputStream(new FileInputStream(file));
    Object obj = oio.readObject();
    oio.close();
    System.out.println(file.getPath()+"反实例序列化成功");
    return obj;
  }

  public static byte[] serializableToByteArray(Object obj) throws Exception{
    ByteArrayOutputStream byteos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(byteos);
    oos.writeObject(obj);
    System.out.println("类的实例序列化成功");
    oos.close();
    return byteos.toByteArray();
  }

  public static Object deserializableFromByteArray(byte [] bytes) throws Exception{
    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    ObjectInputStream oio = new ObjectInputStream(is);
    Object obj = oio.readObject();
    oio.close();
    System.out.println("反实例序列化成功");
    return obj;
  }

  /**
   * Buffered 对 outputStream 增加一个缓存区，先写到缓存区，在写到outputStream里
   * @param obj
   * @return
   * @throws Exception
   */
  public static byte[] serializableToBuffered(Object obj) throws Exception{
    ByteArrayOutputStream byteos = new ByteArrayOutputStream();
    BufferedOutputStream buffer = new BufferedOutputStream(byteos);
    ObjectOutputStream oos = new ObjectOutputStream(buffer);
    oos.writeObject(obj);
    System.out.println("类的实例序列化成功");
    oos.close();
    return byteos.toByteArray();
  }

  public static Object deserializableFromBuffered(byte [] bytes) throws Exception{
    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    BufferedInputStream buffer = new BufferedInputStream(is);
    ObjectInputStream oio = new ObjectInputStream(buffer);
    Object obj = oio.readObject();
    oio.close();
    System.out.println("反实例序列化成功");
    return obj;
  }

}
