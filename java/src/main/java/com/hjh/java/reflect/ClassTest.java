package com.hjh.java.reflect;

import com.hjh.java.annotation.Public;
import org.junit.Test;

import java.io.Closeable;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: hjh
 * @Create: 2019/4/18
 * @Description:
 */
public class ClassTest {

    public static void printArgs(String msg,Object value){
        System.out.println(msg+" "+value);
    }

    public static void printArgs(String ... args){
        for(int i = 0 ; i < args.length ; i++){
            System.out.print(args+" ");
        }
        System.out.println();
    }

    public static void printSuperName(Class clazz){
        printArgs("getGenericSuperclass()=",clazz.getGenericSuperclass());
    }

    public static void printName(Class clazz){
        printArgs("getName()=",clazz.getName());
    }

    public static void printAllInfo(Class clazz){
        printArgs("getName()=",clazz.getName());
        printArgs("toString=",clazz.toString());
        printArgs("toGenericString=",clazz.toGenericString());
        printArgs("isInterface=",clazz.isInterface());
        printArgs("isArray=",clazz.isArray());
        printArgs("isPrimitive=",clazz.isPrimitive());
        printArgs("isAnnotation=",clazz.isAnnotation());
        printArgs("isSynthetic=",clazz.isSynthetic());
        printArgs("getClassLoader=",clazz.getClassLoader());
        printArgs("getTypeParameters=",clazz.getTypeParameters());
        printArgs("getSuperclass=",clazz.getSuperclass());
        printArgs("getGenericSuperclass=",clazz.getGenericSuperclass());
        printArgs("getPackage=",clazz.getPackage());
        printArgs("getInterfaces=",clazz.getInterfaces());
        printArgs("getGenericInterfaces=",clazz.getGenericInterfaces());
        printArgs("getComponentType=",clazz.getComponentType());
        printArgs("getModifiers=",clazz.getModifiers());
        printArgs("getSigners=",clazz.getSigners());
        printArgs("getEnclosingMethod=",clazz.getEnclosingMethod());
        printArgs("getEnclosingConstructor=",clazz.getEnclosingConstructor());
        printArgs("getDeclaringClass=",clazz.getDeclaringClass());
        printArgs("getEnclosingClass=",clazz.getEnclosingClass());
        printArgs("getSimpleName=",clazz.getSimpleName());
        printArgs("getTypeName=",clazz.getTypeName());
        printArgs("getCanonicalName=",clazz.getCanonicalName());
        printArgs("isAnonymousClass=",clazz.isAnonymousClass());
        printArgs("isLocalClass=",clazz.isLocalClass());
        printArgs("isMemberClass=",clazz.isMemberClass());
        printArgs("getClasses=",clazz.getClasses());
        printArgs("getFields=",clazz.getFields());
        printArgs("getMethods=",clazz.getMethods());
        printArgs("getConstructors=",clazz.getConstructors());
        printArgs("getDeclaredClasses=",clazz.getDeclaredClasses());
        printArgs("getDeclaredFields=",clazz.getDeclaredFields());
        printArgs("getDeclaredMethods=",clazz.getDeclaredMethods());
        printArgs("getDeclaredConstructors=",clazz.getDeclaredConstructors());
        printArgs("getProtectionDomain=",clazz.getProtectionDomain());
        printArgs("isEnum=",clazz.isEnum());
        printArgs("getEnumConstants=",clazz.getEnumConstants());
        printArgs("getAnnotations=",clazz.getAnnotations());
        printArgs("getDeclaredAnnotations=",clazz.getDeclaredAnnotations());
        printArgs("getAnnotatedSuperclass=",clazz.getAnnotatedSuperclass());
        printArgs("getAnnotatedInterfaces=",clazz.getAnnotatedInterfaces());
    }

    public static void print(Class clazz){
        printAllInfo(clazz);
//        printArgs("getSuperclass=",clazz.getSuperclass());
//        printArgs("getGenericSuperclass=",clazz.getGenericSuperclass());
    }

    public static void printTypeVariable(TypeVariable[] tv){
        for(int i = 0 ; i < tv.length ; i ++){
            System.out.print(tv[i]+",");
        }
        System.out.println();
    }

    public static void printArray(Object [] objs){
        if(objs == null){
            System.out.println("null");
            return ;
        }
        for(int i = 0 ; i < objs.length ; i ++){
            System.out.print(objs[i].toString()+",");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        new ClassTest().classPropertiesDemo();
    }


    @Test
    public void classPropertiesDemo(){
        int[] arr = new int[10];
        HashMap<String,String> map = new HashMap<>();
        String str = new String("abc");

        Class carr = arr.getClass();
        Class cmap = map.getClass();
        Class cstr = str.getClass();

        /**
         * newInstance
         * 根据class获得实例，无法反射数组
         * 泛型对象只能反射类型擦除后的对象指针
         */
        System.out.println("------- newInstance -----------");
        try {
            //无法反射数组 抛出 InstantiationException
//            int [] arr2 = (int[])carr.newInstance();
            // 可以反射泛型，因为类型擦除 这里转为HashMap和任意HashMap<K,V>都可以
            HashMap map2 = (HashMap<String,Integer>) cmap.newInstance();
            System.out.println(map2.size());//0

            //可以反射string类 但是值和原值相同
            String str2 = (String)cstr.newInstance();
            System.out.println(str);// abc

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        /**
         * getClassLoader
         * 获得该class的类加载器
         */
        System.out.println("------- getClassLoader -----------");
        // 启动类加载器是 c++实现，所以返回null
        System.out.println(String.class.getClassLoader());//null
        // 应用类加载器是java实现，返回某个实例
        System.out.println(this.getClass().getClassLoader());//sun.misc.Launcher$AppClassLoader@18b4aac2

        /**
         * getTypeParameters
         * 获得泛型对象的泛型名称，无法获得泛型的具体类型
         * 补充一种可以获得泛型具体实例的办法
         * @see GenericSuperclassTest
         */
        System.out.println("------- getTypeParameters -----------");
        // 数组没有泛型
        printArray(carr.getTypeParameters());
        //输出hashmap的泛型名称K,V，但是不能获得具体是String,还是Objcet
        printArray( cmap.getTypeParameters());//K V
        // 字符串没有泛型
        printArray(cstr.getTypeParameters());

        /**
         * getSuperclass
         * 获得父类的class
         */
        System.out.println("------- getSuperclass -----------");
        System.out.println(carr.getSuperclass().getName());//java.lang.Object
        System.out.println(cmap.getSuperclass().getName());//java.util.AbstractMap
        System.out.println(cstr.getSuperclass().getName());//java.lang.Object

        /**
         * getGenericSuperclass 获得父类的类型
         * 补充一种可以获得泛型具体实例的办法
         * @see GenericSuperclassTest
         */
        System.out.println("------- getGenericSuperclass -----------");
        System.out.println(carr.getGenericSuperclass().getTypeName());//java.lang.Object
        System.out.println(cmap.getGenericSuperclass().getTypeName());//java.util.AbstractMap<K, V>
        System.out.println(cstr.getGenericSuperclass().getTypeName());//java.lang.Object

        /**
         * getPackage
         * 获得class的package
         */
        System.out.println("------- getPackage -----------");
        //基本类型数组没有package的概念，所以为null
        System.out.println(carr.getPackage());
        System.out.println(cmap.getPackage().getName());//java.util
        System.out.println(cstr.getPackage().getName());//java.lang

        /**
         * getInterfaces
         * 获得class 实现的interface的class
         */
        System.out.println("------- getInterfaces -----------");
        printArray(carr.getInterfaces());//interface java.lang.Cloneable,interface java.io.Serializable
        printArray(cmap.getInterfaces());//interface java.util.Map,interface java.lang.Cloneable,interface java.io.Serializable,
        printArray(cstr.getInterfaces());//interface java.io.Serializable,interface java.lang.Comparable,interface java.lang.CharSequence,

        /**
         * getGenericInterfaces
         * 获得class 实现的interface的Type
         */
        System.out.println("------- getGenericInterfaces -----------");
        printArray(carr.getGenericInterfaces());//interface java.lang.Cloneable,interface java.io.Serializable,
        printArray(cmap.getGenericInterfaces());//java.util.Map<K, V>,interface java.lang.Cloneable,interface java.io.Serializable,
        printArray(cstr.getGenericInterfaces());//interface java.io.Serializable,java.lang.Comparable<java.lang.String>,interface java.lang.CharSequence,

        /**
         * getModifiers
         * 获得一个描述修饰符的int值，利用modifier类去解析该int值，得到该class满足哪些修饰符
         */
        System.out.println("------- getModifiers -----------");
        int marr = carr.getModifiers();
        int mmap = cmap.getModifiers();
        int mstr = cstr.getModifiers();
        System.out.println(marr);//1041
        System.out.println(mmap);//1
        System.out.println(mstr);//17

        Modifier modifier = new Modifier();

        modifier.isPublic(marr);
        modifier.isPrivate(marr);
        modifier.isProtected(marr);

        /**
         * getSigners
         * 得到这个类的签名
         */
        //这个为null
        printArray(carr.getSigners());//null
        printArray(cmap.getSigners());//null
        printArray(cstr.getSigners());//null

    }



    @Test
    public void classNameMethodDemo(){
        Class cstr = String.class;
        Class cclose = Closeable.class;
        int[] is = new int[100];
        Class array =  is.getClass();
        HashMap<String,String> map = new HashMap<>();
        Class cmap = map.getClass();

        /**
         * getName 获得class的Name
         */
        System.out.println("------- getName -----------");
        //全称，带package名
        System.out.println(cstr.getName());//java.lang.String
        System.out.println(cclose.getName());//java.io.Closeable
        //基本类型数组比较特别
        System.out.println(array.getName());//[I
        System.out.println(cmap.getName());//java.util.HashMap


        /**
         * getSimpleName 获得简单ClassName，不带package的名称
         */

        System.out.println("------- getSimpleName -----------");

        //直接类名
        System.out.println(cstr.getSimpleName());//String
        System.out.println(cclose.getSimpleName());//Closeable
        System.out.println(array.getSimpleName());//int[]
        System.out.println(cmap.getSimpleName());//HashMap

        /**
         *toGenericString 生成一个全称的字符串，包括修饰符
         */
        System.out.println("-------- toGenericString ---------");
        // 定义语句
        System.out.println(cstr.toGenericString());//public final class java.lang.String
        System.out.println(cclose.toGenericString());//public abstract interface java.io.Closeable
        System.out.println(array.toGenericString());//public abstract final class [I
        System.out.println(cmap.toGenericString());//public class java.util.HashMap<K,V>

        /**
         * toString()方法
         * 分为 class 和 interface 两种开头
         */
        System.out.println("-------- toString ----------");

        System.out.println(cstr.toString());//class java.lang.String
        System.out.println(cclose.toString());//interface java.io.Closeable
        System.out.println(array.toString());//class [I
        System.out.println(cmap.toString());//class java.util.HashMap

        /**
         * getComponentType
         * 获得组成类型 针对数组而言
         * 非数组为null 数组为基本类型
         */
        System.out.println("--------- getComponentType ---------");
        System.out.println(cstr.getComponentType());//class java.lang.String
        System.out.println(cclose.getComponentType());//interface java.io.Closeable
        System.out.println(array.getComponentType());//int[]
        System.out.println(cmap.getComponentType());//java.util.HashMap

        /**
         * getTypeName 获得TypeName
         * 如果是数组 getComponentType加上[]，
         * 否则 getName()
         */
        System.out.println("-------- getTypeName ----------");
        System.out.println(cstr.getTypeName());//class java.lang.String
        System.out.println(cclose.getTypeName());//interface java.io.Closeable
        System.out.println(array.getTypeName());//int[]
        System.out.println(cmap.getTypeName());//java.util.HashMap

        /**
         * getCanonicalName 返回基础类的规范名称
         * 如果没有则返回null
         */
        System.out.println("-------- getCanonicalName ----------");
        System.out.println(cstr.getCanonicalName());//class java.lang.String
        System.out.println(cclose.getCanonicalName());//interface java.io.Closeable
        System.out.println(array.getCanonicalName());//int[]
        System.out.println(cmap.getCanonicalName());//java.util.HashMap



    }

    @Test
    public void classStaticMethodDemo(){
        try {
           Class clazz =  Class.forName("java.lang.String");
           System.out.println(clazz.getName());

           ClassLoader classLoader = this.getClass().getClassLoader();
           //指定类加载器加载class文件,完全不同的类加载器加载的同一个class不同
            Class clazz2 = Class.forName("com.hjh.java.reflect.ClassTest",true,classLoader);
            System.out.println(clazz2.getName());
            //应用类加载器,用于加载用户自己的类
            System.out.println(classLoader.getClass().getName());
            //扩展类加载器，用于加载%JAVA_HOME%/jre/lib/ext java.ext.dirs指定的类
            System.out.println(classLoader.getParent().getClass().getName());
            //启动类加载器，因为是用c++写的，所以这里报错，getParent()返回为null 用于加载Object,String等jre的运行java类
            System.out.println(classLoader.getParent().getParent().getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
