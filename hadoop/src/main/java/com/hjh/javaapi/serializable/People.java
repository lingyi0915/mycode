package com.hjh.javaapi.serializable;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-09-12
 * @Description:
序列化和反序列化的概念：
    序列化：把对象转换为字节序列的过程称为对象的序列化。
    反序列化：把字节序列恢复为对象的过程称为对象的反序列化。
什么情况下需要序列化:
    当你想把的内存中的对象状态保存到一个文件中或者数据库中时候；
    当你想用套接字在网络上传送对象的时候；
    当你想通过RMI传输对象的时候；
 */
public class People implements Serializable{
  private static final long serialVersionUID = 1L;
  private String name = null;
  private String sex = null;
  private int age = 0;

  @Override
  public String toString() {
    return "People{" +
            "serialVersionUID=" + serialVersionUID +
            ", name='" + name + '\'' +
            ", sex='" + sex + '\'' +
            ", age=" + age +
            '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSex() {
    return sex;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

}
