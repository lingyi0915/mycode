package com.hjh.java.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-01-26
 * @Description:
 */
public class MethodTest {

  public int tmp = 0;

  public MethodTest(){
    System.out.println("父类的构造方法");
  }

  public void reflectMethod(String methodName){
    System.out.println("这个类实例是:"+this.getClass().getName());
    System.out.println("methodName:"+methodName);
    Class<?> clazz = this.getClass();
    try {

      Field field = clazz.getField("tmp");

      System.out.println("字段类型:"+field.getType());
      field.setInt(this,110);

      System.out.println("字段值:"+this.tmp);

        Method method =clazz.getMethod(methodName);
        method.invoke(this);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }catch (NoSuchMethodException e) {
      e.printStackTrace();
      System.out.println("没有这个方法!!!!");
    } catch (NoSuchFieldException e){
      e.printStackTrace();;
      System.err.println("没有这个字段");
    }
  }

  public static void main(String[] args) {
    MethodSon ms = new MethodSon();
  }
}

class MethodSon extends MethodTest{
  public MethodSon(){
    System.out.println("子类的构造方法");
    reflectMethod("print");
  }
  public void print(){
    System.out.println("hello world!");
  }
}
