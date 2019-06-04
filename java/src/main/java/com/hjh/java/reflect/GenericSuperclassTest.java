package com.hjh.java.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: hjh
 * @Create: 2019/4/18
 * @Description:
 */
public class GenericSuperclassTest {
    public static void main(String[] args) {

        HashMap<String,String> map = new HashMap<String,String>();

        Class cmap = map.getClass();

        //输出当前类的泛型类型的 只能得到K,V
        System.out.println(cmap.toGenericString());

        Hint<HashMap<String,String>> hint = new Hint<HashMap<String,String>>() {};

        Class chint = hint.getClass();

        // hint子类的父类，即hint带指定泛型的class
        Class baseClass = chint.getSuperclass();

        System.out.println(baseClass.toGenericString());

        // chint的父类带泛型类型的Type
        Type t = chint.getGenericSuperclass();
        // t是type的子类 具体类为 sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
        System.out.println(t.getClass().getName());

        //通过继承类，实现显示泛型类型的功能
        System.out.println(t.getTypeName());

        // 如果t 是 ParameterizedType 的实例 并且，父类等于t 的转化
        if (t instanceof ParameterizedType && baseClass.equals(((ParameterizedType) t).getRawType())) {
            //将 t 转为 ParameterizedType
            ParameterizedType baseClassChild = (ParameterizedType) t;
            //获得第一个类型 即泛型的实现类
            t = baseClassChild.getActualTypeArguments()[0];
        }
        // 这里就得到了 HashMap<String,String>
        System.out.println(t.getTypeName());

    }
}

abstract class Hint<T>{
    protected Hint(){

    }
}
