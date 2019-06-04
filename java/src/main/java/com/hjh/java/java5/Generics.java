package com.hjh.java.java5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-04-20
 * @Description: java5特性 泛型 Generics:
 *  引用泛型之后，允许指定集合里元素的类型，免去了强制类型转换，并且能在编译时刻进行类型检查的好处。
 *  Parameterized Type作为参数和返回值，Generic是vararg、annotation、enumeration、collection的基石。
 */
public class Generics<T> {

  // T stands for "Type"
  private T t;
  public void set(T t) { this.t = t; }
  public T get() { return t; }

  /**
   * 通配符分类
   无界通配：?
   子类限定：? extends Object
   父类限定：? super Integer
   * @param list
   * @throws IOException
   */
  public void printList(List<?> list){
    for(Iterator<?> i = list.iterator(); i.hasNext();){
      System.out.println(i.next().toString());
    }
  }

    public static void main(String[] args) {
    Generics<String> g = new Generics<String>();
    g.set("abcdefg");
    System.out.println(g.get());
    List<String> list = new ArrayList<String>();
    list.add("1");
    g.printList(list);
  }
}
